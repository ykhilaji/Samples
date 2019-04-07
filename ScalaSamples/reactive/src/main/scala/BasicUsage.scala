import java.io.File
import java.util.{Timer, TimerTask}

import org.apache.commons.io.monitor.{FileAlterationListenerAdaptor, FileAlterationMonitor, FileAlterationObserver}
import rx.lang.scala.{Observable, Subscription}

import scala.util.Random

object RxHelloWorld extends App {
  Observable
    .from(Seq(1, 2, 3, 4, 5))
    .sliding(2, 1)
    .flatMap(_.sum)
    .foreach(next => println(next))
  // 3 - 5 - 7 - 9 - 5
}

object RxSubscribe extends App {
  Observable[Int](subscriber => {
    val timer = new Timer("timer", true)
    timer.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit = subscriber.onNext(Random.nextInt(100))
    }, 500, 500)
  })
    .subscribe(next => {
      println(s"Next: $next")
    }, error => {
      println(error.getLocalizedMessage)
    }, () => {
      println("complete")
    })

  Thread.sleep(5000)
}

object RxSubscribeFileMonitor extends App {
  val monitor = new FileAlterationMonitor(1000)
  val observer = new FileAlterationObserver("D:\\Program Files\\WORK\\Samples\\ScalaSamples\\reactive\\src\\main\\resources")
  monitor.addObserver(observer)
  monitor.start()

  val dirMonitor: Observable[String] = Observable[String](subscriber => {
    observer.addListener(new FileAlterationListenerAdaptor {
      override def onFileCreate(file: File): Unit = subscriber.onNext(file.getName)

      override def onDirectoryDelete(directory: File): Unit = subscriber.onCompleted()
    })
  })

  val allNewFiles: Subscription = dirMonitor.subscribe(next => {
    println(s"New created file name: $next")
  }, error => {
    println(error.getLocalizedMessage)
  }, () => {
    println("Dir was deleted")
  })

  val filteredNewFiles = dirMonitor.filter(_.contains("F"))
    .subscribe(next => {
      println(s"New filtered created file name: $next")
    }, error => {
      println(error.getLocalizedMessage)
    }, () => {
      println("Dir was deleted")
    })

  Thread.sleep(15000)
}

object Rx extends App {
//  Observable.interval()
}