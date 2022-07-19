package model

import cats.Semigroup
import cats.implicits._
import implicits._

import scala.collection.immutable.HashMap
import scala.util.Properties

/** Output data containing information about multiple sensors from multiple files
 *
 * @param sensors     data from sensors
 * @param failedCount number of failed measurements
 * @param totalCount  total number of measurements
 * @param fileCount   number of processed files
 */
case class Result(
                       sensors: Map[String, Aggregation] = HashMap().withDefaultValue(FailedAggregation),
                       failedCount: Int = 0,
                       totalCount: Int = 0,
                       fileCount: Int = 0
                     ) {
  /** Add a measurement to the output data.
   *
   * @param rhs value to add
   * @return new instance of output data, which includes rhs value.
   */
  def +(rhs: Measurement): Result = {
    Result(
      sensors + (rhs.id -> (sensors(rhs.id) + rhs.humidity)),
      failedCount + (if (rhs.humidity.isDefined) 0 else 1),
      totalCount + 1,
      fileCount
    )
  }

  /** Combine two instances of [[model.Result]].
   *
   * @param rhs output data to be combined
   * @return combined output data.
   */
  def |+|(rhs: Result): Result = {
    Result(
      sensors |+| rhs.sensors,
      failedCount + rhs.failedCount,
      totalCount + rhs.totalCount,
      fileCount + rhs.fileCount
    )
  }

  /** String representation of [[model.Result]] */
  override def toString: String = {
    // Reversed ordering by average.
    implicit val ordering: Ordering[(String, Aggregation)] =
      (x: (String, Aggregation), y: (String, Aggregation)) => (x._2.average, y._2.average) match {
        case (None, None) => 0
        case (None, _) => 1
        case (_, None) => -1
        case (Some(a), Some(b)) => b compare a
      }

    s"""Num of processed files: $fileCount
       |Num of processed measurements: $totalCount
       |Num of failed measurements: $failedCount
       |
       |Sensors with highest avg humidity:
       |
       |sensor-id,min,avg,max
       |${
      sensors.toSeq.sorted.map { case (id, data) => s"$id,$data" }.mkString(Properties.lineSeparator)
    }""".stripMargin
  }
}

/** Semigroup for [[model.Result]] */
trait OutputDataSemigroupImpl extends Semigroup[Result] {
  def combine(lhs: Result, rhs: Result): Result = lhs |+| rhs
}

/** Provide implicit semigroup for [[model.Result]] */
trait OutputDataSemigroup {

  implicit object outputDataSemigroupImpl extends OutputDataSemigroupImpl

}