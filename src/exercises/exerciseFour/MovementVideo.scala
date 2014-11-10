package exerciseFour

import java.io.File

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{FlowMaterializer, MaterializerSettings}
import org.reactivestreams.{Publisher, Subscriber}
import video.Frame

object MovementVideo {

  /**
   * run:
   *   ./activator 'runMain exerciseFour.MovementVideo'
   *
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = FlowMaterializer()

    // ------------
    // EXERCISE 4.1
    // ------------
    // Fill in the code necessary to create a flow dsl and manipulate the video stream to be grayscale.

    // Given - The location of the mp4 we can display (note first few seconds are still frame).
    val mp4 = new File("goose.mp4")

    //Create a Producer from the file system
    val filePublisher: Publisher[Frame] = video.FFMpeg.readFile(mp4, system)
    val videoSubscriber: Subscriber[Frame] = video.Display.create(system)
  }
}