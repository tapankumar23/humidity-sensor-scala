package model

import cats.implicits._

/** Aggregated data from a sensor if at least one measurement is valid */
case class ValidAggregation(min: Int, max: Int, sum: Int, count: Int) extends Aggregation {
  /** Add an optional value to an aggregation.
   *
   * @param rhs value to add
   * @return new instance of aggregation, which includes rhs value.
   */
  def +(rhs: Option[Int]): Aggregation = rhs match {
    case None => this
    case Some(v) => ValidAggregation(math.min(min, v), math.max(max, v), sum + v, count + 1)
  }

  /** Combine two aggregations.
   *
   * @param rhs aggregation to be combined
   * @return combined aggregation.
   */
  def |+|(rhs: Aggregation): Aggregation = rhs match {
    case FailedAggregation => this
    case a: ValidAggregation => ValidAggregation(
      math.min(min, a.min),
      math.max(max, a.max),
      sum + a.sum,
      count + a.count
    )
  }

  /** String representation of [[model.ValidAggregation]] */
  override def toString: String = s"$min,${if (count == 0) "NaN" else average.get},$max"

  /** Average value */
  lazy val average: Option[Int] = (sum / count).some
}