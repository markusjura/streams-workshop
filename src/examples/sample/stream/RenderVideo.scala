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

object RenderVideo {

  /**
   * run:
   *   activator 'runMain sample.stream.RenderVideo cat.mp4'
   *
   */
  def main(args: Array[String]): Unit = {
    val videoFileName = args(0)
    implicit val system = ActorSystem("rendervideo")
    implicit val flowMaterializer = FlowMaterializer()

    val file = new File(videoFileName)
    val actor: ActorRef = system.actorOf(Props(new FFMpegPublisher(file)))
    val filePublisher: Publisher[Frame] = ActorPublisher(actor)

    val source = PublisherSource(filePublisher)
    val videoSubscriber: Subscriber[Frame] = video.Display.create(system)
    val sink = SubscriberSink(videoSubscriber)

    source.runWith(sink)
  }
}