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

  override def receive: Receive = {

    case ActorPublisherMessage.Request(elements) =>
      while (totalDemand > 0) {
        val circleProperties = generateCircleProperties(80, 80)
        val bufferedImage = video.imageUtils.ImageUtils.createBufferedImage(80, 80, circleProperties)
        val frame = Frame(image = bufferedImage, timeStamp = 1L, timeUnit = TimeUnit.SECONDS)

        onNext(frame)
      }

    case ActorPublisherMessage.Cancel =>
      context stop self
  }

  def generateCircleProperties(screenWidth:Int, screenHeight:Int): CircleProperties = {
    val width = ImageUtils.randWidth(screenWidth)
    val height = ImageUtils.randHeight(screenHeight)
    val randColor = ImageUtils.randColor
    CircleProperties(width = width, height = height, color = randColor)
  }

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

    // Fill in the code necessary to construct a UI to consume and display the Frames produced
    // by the Circle producer.

    val display = video.display(system)

    val circleProducer = system.actorOf(Props[CircleProducer], "circleProducer")
    ActorPublisher(circleProducer).subscribe(display)
  }
}
