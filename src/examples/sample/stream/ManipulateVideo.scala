package sample.stream

import java.io.File

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import org.reactivestreams._
import video.{ConvertImage, Frame}

object ManipulateVideo {

  /**
   * run:
   *   ./activator 'runMain sample.stream.ManipulateVideo goose.mp4'
   *
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val flowMaterializer = FlowMaterializer()

    val fileProducer: Publisher[Frame] = video.FFMpeg.readFile(new File(args(0)), system)
    val source = PublisherSource(fileProducer)
    val videoSubscriber: Subscriber[Frame] = video.Display.create(system)
    val sink = SubscriberSink(videoSubscriber)

    source.map {
      frame => Frame(ConvertImage.addWaterMark(frame.image), frame.timeStamp, frame.timeUnit)
    }.map {
      frame => Frame(ConvertImage.invertImage(frame.image), frame.timeStamp, frame.timeUnit)
    }.runWith(sink)
  }
}