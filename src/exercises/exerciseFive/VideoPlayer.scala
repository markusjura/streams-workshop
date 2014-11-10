package exerciseFive

import java.io.File

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.FlowMaterializer
import akka.stream.actor._
import org.reactivestreams._
import video._

object VideoPlayer {

  /**
   * run:
   * ./activator 'runMain exerciseFive.VideoPlayer'
   *
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("video-player")
    implicit val materializer = FlowMaterializer()

    // Here are the input + output streams for the player.
    val (ui: Publisher[UIControl], player: Subscriber[Frame]) =
      video.Display.createPlayer(system)

    // here is the file to read.
    val videoFile = new File("goose.mp4")

    val playEngineActor = system.actorOf(Props(new PlayerProcessorActor(videoFile)))

    // hook the actor in as a subscriber of UIControl.
    val playEngineSubscriber = ActorSubscriber[UIControl](playEngineActor)
    ui.subscribe(playEngineSubscriber)

    // Hook the actor in as a publisher of frames.
    val playEngineProducer = ActorPublisher[Frame](playEngineActor)
    playEngineProducer.subscribe(player)
  }

  case class PlayerRequestMore(elements: Long)

  case object PlayerDone

  /**
   * Takes UIControl, publishes Frames.
   *
   * @param file the file to play.
   */
  class PlayerProcessorActor(file: File) extends ActorPublisher[Frame] with ActorSubscriber /* [UIControl] */ {

    private var currentPlayer: Option[ActorRef] = None

    private var isPaused: Boolean = false

    // Buffering if we get overloaded on input frames.  The blocking here doesn't matter.
    private val buf = new java.util.concurrent.ArrayBlockingQueue[Frame](100)

    override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

    override def receive: Actor.Receive = {
      case ActorSubscriberMessage.OnNext(control: UIControl) =>
        control match {
          case Play =>
            // Update state and kick off the stream
            if (currentPlayer.isEmpty) {
              kickOffFileProducer()
            }
            isPaused = false
            requestMorePlayer()

          case Pause =>
            isPaused = true

          case Stop =>
            isPaused = false
            currentPlayer.foreach(_ ! Stop)
            currentPlayer = None
        }

      case ActorPublisherMessage.Request(elements) =>
        if (! isPaused) {
          if (tryEmptyBuffer()) {
            requestMorePlayer()
          }
        }

      case PlayerDone =>
        kickOffFileProducer()
        requestMorePlayer()

      case f: Frame =>
        if (totalDemand > 0) {
          onNext(f)
        } else {
          buffer(f)
        }
    }

    private def kickOffFileProducer(): Unit = {
      val publisher = video.FFMpeg.readFile(file, context)
      val consumerRef = context.actorOf(Props(new PlayerActor(self)))
      currentPlayer = Some(consumerRef)
      publisher.subscribe(ActorSubscriber(consumerRef))
    }

    private def requestMorePlayer(): Unit = {
      if (totalDemand > 0) currentPlayer match {
        case Some(player) =>
          player ! PlayerRequestMore(totalDemand)
        case None =>
          ()
      }
    }

    /** Buffers the given frame to be pushed to future clients later. */
    private def buffer(f: Frame): Unit = {
      buf.add(f)
    }

    private def tryEmptyBuffer(): Boolean = {
      while (!buf.isEmpty && totalDemand > 0) {
        onNext(buf.take())
      }
      buf.isEmpty
    }
  }

  /**
   * Takes frames from the file publisher and forwards them to the PlayerProcessor.
   *
   * @param frameSubscriber the player processor.
   */
  class PlayerActor(frameSubscriber: ActorRef) extends ActorSubscriber /* [Frame] */ {
    // All requests for more data handled by our 'consumer' actor.
    override val requestStrategy = ZeroRequestStrategy

    override def receive: Receive = {

      case PlayerRequestMore(e) =>
        request(e)

      case video.Stop =>
        cancel()

      // Just delegate back/forth with the controlling 'consumer' actor.
      case ActorSubscriberMessage.OnNext(frame: Frame) =>
        frameSubscriber ! frame

      case ActorSubscriberMessage.OnComplete =>
        frameSubscriber ! PlayerDone
    }
  }

}