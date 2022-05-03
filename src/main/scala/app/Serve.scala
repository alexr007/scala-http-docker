package app

import cats.effect.IO
import cats.effect.IOApp
import io.circe.Encoder
import io.circe.generic.AutoDerivation
import org.http4s.EntityEncoder.Pure
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

object Serve extends IOApp.Simple with Http4sDsl[IO] {
  val r = new Random
  implicit def ee[A: Encoder]: Pure[A] = jsonEncoderOf[A]

  case class Person(id: Int, name: String)
  object Person extends AutoDerivation

  def doTheBusiness1(name: String): Person = Person(r.nextInt(100), name.toUpperCase)
  def doTheBusiness2(name: String): Person = Person(r.nextInt(100), name.toLowerCase)

  /** http://localhost:8080/hello/Jim */
  /** http://localhost:8080/bye/BEN */
  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => doTheBusiness1(name).pipe(p => Ok(p))
    case GET -> Root / "bye" / name   => doTheBusiness2(name).pipe(p => Created(p))
  }

  override def run = BlazeServerBuilder[IO]
    .bindHttp(8080, "localhost")
    .withHttpApp(routes.orNotFound)
    .serve
    .compile
    .drain
}
