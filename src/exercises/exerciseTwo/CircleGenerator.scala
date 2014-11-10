package exerciseTwo

import akka.actor.{Props, ActorSystem}
import video.imageUtils.ImageUtils

// ------------
// EXERCISE 2
// ------------
// Fill in the code necessary to handle receiving a new message to generate the
// properties for a random circle.  For testing just println when the message is received.
// See video.imageUtils.CircleProperties and video.imageUtils.ImageUtils

object CircleGenerator {

  case class GenerateCircle(width:Int, height:Int)

  /**
   * run:
   * ./activator 'runMain exerciseTwo.CircleGenerator'
   *
   */
  def main(args: Array[String]): Unit = {
    // ActorSystem represents the "engine" we run in, including threading configuration and concurrency semantics.
    val system = ActorSystem()
    val random = scala.util.Random

    // Fill in the code necessary to create the Actor in the ActorSystem and send it a message.
    // TODO - Your code here.
    
    system.shutdown()
    system.awaitTermination()
  }
}