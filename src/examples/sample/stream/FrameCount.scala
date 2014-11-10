package sample.stream

import akka.actor.ActorSystem
import akka.stream._
import video.Frame
import org.reactivestreams._
import java.io.File
import akka.stream.scaladsl._

object FrameCount {

  /**
   * run:
   *    ./activator 'runMain sample.stream.FrameCount file.mp4'
   *
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val flowMaterializer = FlowMaterializer()
    import scala.concurrent.ExecutionContext.Implicits.global

    val videoPublisher: Publisher[Frame] = video.FFMpeg.readFile(new File("goose.mp4"), system)
    val videoSource = Source(videoPublisher)
    videoSource.fold(0) { (count, frame) =>
      val nextCount = count + 1
      System.out.print(f"\rFRAME ${nextCount}%05d")
      nextCount
    }.onComplete {
      case _ =>
        system.shutdown()
        system.awaitTermination()
    }
  }
}