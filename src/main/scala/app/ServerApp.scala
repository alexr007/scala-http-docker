package app

import cats.effect._
import io.circe.generic.AutoDerivation
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

object ServerApp extends IOApp.Simple with Http4sDsl[IO] {
  val r = new Random

  case class Person(id: Int, name: String)
  object Person extends AutoDerivation

  def doTheBusiness(name: String): Person = Person(r.nextInt(100), name.toUpperCase)

  val routes = HttpRoutes.of[IO] {
    case GET -> Root                  => IO(println(">>")) >> Ok("It works")
    case GET -> Root / "hello" / name => doTheBusiness(name).pipe(p => Ok(p))
  }

  override def run = BlazeServerBuilder[IO]
    .bindHttp(8080, "0.0.0.0")
    .withHttpApp(routes.orNotFound)
    .serve
    .compile
    .drain
}
