package processor
import cats.effect.IO
import cats.implicits._
import implicits._
import model.Result

import scala.collection.parallel.CollectionConverters._

/**
 * MapReduce.
 */
class ParallelProcessor extends CsvProcessor {

  def execute(directoryName: String): IO[Result] = getInputFiles(directoryName).par
    .map(file => processCsvFile(file))
    .fold(IO(Result()))(_ |+| _)
}

