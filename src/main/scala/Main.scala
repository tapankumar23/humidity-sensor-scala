import cats.effect._
import cats.implicits._
import processor.CsvProcessor

object Main extends IOApp {

  /**
   *
   * @param args command line arguments
   *             args(0) [optional] - directory name
   *             args(1) [optional] - processor type name
   * @return exit code
   */
  override def run(args: List[String]): IO[ExitCode] = {
    val ioExit = for {
      csvProcessor <- IO(CsvProcessor(args.get(1)))
      result <- csvProcessor.execute(args.headOption.getOrElse("."))
      _ <- IO(println(result))
    } yield ExitCode.Success

    ioExit.handleError(throwable => {
      println(throwable)
      ExitCode.Error
    })
  }
}