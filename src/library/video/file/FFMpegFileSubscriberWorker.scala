package video
package file

import java.io.File

import akka.actor.{ActorRefFactory, Props}
import akka.stream.actor._
import com.xuggle.mediatool.{IMediaWriter, ToolFactory}
import com.xuggle.xuggler.IRational
import org.reactivestreams._


private[video] class FFMpegFileSubscriberWorker(file: File,
                                                width: Int,
                                                height: Int,
                                                frameRate: IRational = IRational.make(3, 1))
  extends ActorSubscriber {

  override protected val requestStrategy = OneByOneRequestStrategy

  private val writer: IMediaWriter = ToolFactory.makeWriter(file.getAbsolutePath)

  writer.addVideoStream(0, 0, frameRate, width, height)

  override def receive: Receive = {
    case ActorSubscriberMessage.OnNext(frame: Frame) =>
      writer.encodeVideo(0, frame.image, frame.timeStamp, frame.timeUnit)
    case ActorSubscriberMessage.OnComplete =>
      writer.close()
    // Destroy bad files
    case ActorSubscriberMessage.OnError(e) =>
      writer.close()
      file.delete()
  }
}

private[video] object FFMpegFileSubscriberWorker {

  def apply(factory: ActorRefFactory,
            file: File,
            width: Int,
            height: Int,
            frameRate: IRational = IRational.make(3, 1)): Subscriber[Frame] = {

    ActorSubscriber(factory.actorOf(Props(new FFMpegFileSubscriberWorker(file, width, height, frameRate))))
  }
}