package processor

//import cats.effect.{ContextShift, IO}
import cats.implicits._
import model.{FailedAggregation, Result, ValidAggregation}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.io.IOException
import scala.collection.immutable.HashMap
import cats.effect.unsafe.implicits.global
//import scala.concurrent.ExecutionContext

class CsvProcessorSpec extends AnyFlatSpec with should.Matchers {

  //implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val directoryName = getClass.getResource("/valid").getPath

  val defaultResult: Result = Result(
    HashMap(
      "s1" -> ValidAggregation(10, 98, 108, 2),
      "s2" -> ValidAggregation(78, 88, 246, 3),
      "s3" -> FailedAggregation
    ), 2, 7, 2
  )

  private val types = Seq(
    "SequentialProcessor",
    "ParallelProcessor",
  )

  trait Processor {
    val processor: CsvProcessor = CsvProcessor("SequentialProcessor".some)
  }

  "Output of default test" should "be as specified in task.md including string representation" in new Processor {
    private val result = processor.execute(directoryName).unsafeRunSync()
    result should be(defaultResult)

    private def fixNewLine(s: String) = s.replaceAll("\\r\\n|\\r|\\n", "\\n")

    fixNewLine(result.toString) should be(
      fixNewLine(
        """Num of processed files: 2
          |Num of processed measurements: 7
          |Num of failed measurements: 2
          |
          |Sensors with highest avg humidity:
          |
          |sensor-id,min,avg,max
          |s2,78,82,88
          |s1,10,54,98
          |s3,NaN,NaN,NaN""".stripMargin))
  }

  it should "be the same for each processor type" in new Processor {
    types.foreach(typeName => {
      val anotherProcessor = CsvProcessor(typeName.some)
      val anotherResult = anotherProcessor.execute(directoryName).unsafeRunSync()
      anotherResult should be(defaultResult)
    })
  }

  it should "throw IllegalArgumentException for unknown CSV processor" in {
    assertThrows[IllegalArgumentException] {
      CsvProcessor("Unknown".some)
    }
  }

  it should "throw IOException for an invalid path" in new Processor {
    assertThrows[IOException] {
      val directoryName = getClass.getResource("/invalid").getPath
      processor.execute(directoryName).unsafeRunSync()
    }
  }

  "Output data" should "be empty" in {
    val directoryName = getClass.getResource("/empty").getPath

    types.foreach(typeName => {
      val anotherProcessor = CsvProcessor(typeName.some)
      val result = anotherProcessor.execute(directoryName).unsafeRunSync()
      result should be(Result())
    })
  }
}