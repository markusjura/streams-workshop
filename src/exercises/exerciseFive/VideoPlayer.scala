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
  }

  case class PlayerRequestMore(elements: Long)

  case object PlayerDone

}