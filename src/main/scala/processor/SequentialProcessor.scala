package processor

import cats.effect.IO
import model.Result

class SequentialProcessor extends CsvProcessor {

  def execute(directoryName: String): IO[Result] =
    getInputFiles(directoryName).foldLeft(IO(Result())) {
      (either, file) => either.flatMap(processCsvFile(file, _))
    }
}