package sample.stream

import java.io.File

import akka.actor.{Props, ActorRef, ActorRefFactory, ActorSystem}
import akka.stream.FlowMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import org.reactivestreams._
import video.{ConvertImage, Frame}
import akka.actor._
import video.file._
import scala.concurrent.duration._

object ManipulateVideo {

  /**
   * run:
   *   activator 'runMain sample.stream.ManipulateVideo cat.mp4'
   *
   */
  def main(args: Array[String]): Unit = {
    val videoFileName = args(0)
    implicit val system = ActorSystem("manipulatevideo")
    implicit val flowMaterializer = FlowMaterializer()

    val file = new File(videoFileName)
    val actor: ActorRef = system.actorOf(Props(new FFMpegPublisher(file)))
    val filePublisher: Publisher[Frame] = ActorPublisher(actor)

    val source = PublisherSource(filePublisher)
    val videoSubscriber: Subscriber[Frame] = video.Display.create(system)
    val sink = SubscriberSink(videoSubscriber)

    val sourceWithCuttingVideo = source.takeWithin(2.seconds)

    sourceWithCuttingVideo.map {
      frame =>
        val imageWithWatermark = ConvertImage.addWaterMark(frame.image)
        val imageWithGrayscale = ConvertImage.grayscale(imageWithWatermark)
        Frame(imageWithGrayscale, frame.timeStamp, frame.timeUnit)
    }.runWith(sink)
  }
}