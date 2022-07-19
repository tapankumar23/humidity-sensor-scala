package processor

import java.io.{File, IOException}

//import cats.effect.{ContextShift, IO}
import cats.effect.IO
import cats.implicits._
import com.github.tototoshi.csv.CSVReader
import model.{Measurement, Result}

trait CsvProcessor {
  /** Read and aggregate data from a sequence of CSV-files.
   *
   * @param directoryName name of input directory
   * @return processed data from all input files.
   */
  def execute(directoryName: String): IO[Result]

  /** Retrieve list of CSV-files from specified directory.
   *
   * @param directoryName name of input directory
   * @return list of CSV-files.
   */
  def getInputFiles(directoryName: String): Seq[File] = {
    val inputDirectory = new File(directoryName)
    if (!inputDirectory.isDirectory) throw new IllegalArgumentException("Wrong input directory")
    inputDirectory.listFiles((_, name) => name.endsWith(".csv")).toIndexedSeq
  }

  /** Process a CSV-file.
   *
   * @param file   a file to process
   * @param result optional instance of [[model.Result]], which contains data from previously processed files.
   * @return a new [[model.Result]] instance containing results of current file processing and optionally
   *         from previously processed files.
   */
  protected def processCsvFile(file: File, result: Result = Result()): IO[Result] = {
    val inIO = IO(CSVReader.open(file))

    inIO.bracket { reader =>
      IO({
        val fileOutputData = reader.toStream.tail // Skip header
          .map { // Parse a line
            case id :: "NaN" :: Nil => Measurement(id, None)
            case id :: humidity :: Nil => Measurement(id, humidity.toInt.some)
            case _ => throw new IOException("Wrong format of file '%s'".format(file.getName))
          }
          .foldLeft(result)(_ + _)

        fileOutputData.copy(fileCount = fileOutputData.fileCount + 1)
      })
    } {
      // Releasing resources.
      in => IO(in.close()).handleErrorWith(_ => IO.unit).void
    }
  }
}

object CsvProcessor {
  private val defaultName: String = "ParallelProcessor"

  /**
   * Choose processor based on parameter
   * FACTORY method
   */
  //def apply(name: Option[String] = None)(implicit cs: ContextShift[IO]): CsvProcessor = name.getOrElse(defaultName) match {
  def apply(name: Option[String] = None): CsvProcessor = name.getOrElse(defaultName) match {
    case "SequentialProcessor" => new SequentialProcessor()
    case "ParallelProcessor" => new ParallelProcessor()
    case _ => throw new IllegalArgumentException("Processor name '%s' is not valid".format(name))
  }
}