package exerciseThree

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.stream.actor._
import video.Frame
import video.imageUtils.{CircleProperties, ImageUtils}

// ------------
// EXERCISE 3.1
// ------------
// Fill in the code necessary to produce random circles based on the requested demand.
// The properties of the circles should be retrieved from the the CircleGenerator actor
// which will return the random properties of a circle.  The random circles
// should then be drawn to a Buffered Image and used to create the Frame.
//
// When the Frame is ready it should be sent to the consumer using:
//      onNext(Frame ... )
//
// See video.imageUtils.ImageUtils.createBufferedImage
class CircleProducer extends ActorPublisher[Frame] {

  override def receive: Receive = ???

}


object CircleProducer {

  /**
   * run:
   *   ./activator 'runMain exerciseThree.CircleProducer'
   *
   */
  def main(args: Array[String]): Unit = {
    // ActorSystem represents the "engine" we run in, including threading configuration and concurrency semantics.
    val system = ActorSystem()

    val display = video.display(system)
    // TODO Fill in the code necessary to construct a UI to consume and display the Frames produced
    // by the Circle producer.
  }
}
