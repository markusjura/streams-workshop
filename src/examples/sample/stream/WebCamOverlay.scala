package sample.stream

import java.io.File

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{SubscriberSink, PublisherSource, Flow}
import org.reactivestreams.{Subscriber, Publisher}
import video.Frame
import video.imageUtils.ImageOverlay


object WebcamOverlay {

  /**
   * run:
   *    ./activator 'runMain sample.stream.WebcamOverlay'
   *
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = FlowMaterializer()

    val overlay = new ImageOverlay(new File("crosshairs-overlay.jpg"))
    val webcam: Publisher[Frame] = video.WebCam.cameraStreams(system).last
    val source: PublisherSource[Frame] = PublisherSource(webcam)
    val render: Subscriber[Frame] = video.Display.create(system)
    val sink = SubscriberSink(render)

    source.map { frame =>
      overlay.overlayOnto(frame.image)
      frame
    }.runWith(sink)
  }
}