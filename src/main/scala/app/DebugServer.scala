package app

import cats.effect._
import cats.implicits.catsSyntaxApplicativeId
import cats.implicits.toFunctorOps
import io.circe.Encoder
import io.circe.Json
import io.circe.JsonObject
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import org.http4s.Header
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.jsonDecoder
import org.http4s.dsl.Http4sDsl

object DebugServer extends IOApp.Simple with Http4sDsl[IO] {

  implicit val headerEncoder: Encoder[Header.Raw] = (h: Header.Raw) => s"${h.name} -> ${h.value}".asJson

  def mkBody(rq: Request[IO]) =
    rq.attemptAs[Json]
      .foldF(
        _ => rq.as[String].map(s => if (s.nonEmpty) s.asJson else Json.Null),
        _.pure[IO]
      )

  def mkParams(params: Map[String, collection.Seq[String]]) =
    JsonObject.fromMap(params.fmap(_.mkString(",").asJson)).asJson

  def mkDetails(rq: Request[IO]) = rq match {
    case method -> path :? params =>
      mkBody(rq)
        .map { body =>
          Map(
            "method"  -> method.name.asJson,
            "uri"     -> rq.uri.renderString.asJson,
            "query"   -> rq.uri.query.renderString.asJson,
            "path"    -> path.renderString.asJson,
            "params"  -> mkParams(params),
            "headers" -> rq.headers.headers.asJson,
            "body"    -> body
          )
        }
  }

  val routes = HttpRoutes.of[IO] { case rq =>
    mkDetails(rq)
      .flatTap(m => IO(m.foreach { case (k, v) => println(s"$k -> $v") }))
      .flatMap(Ok(_))
  }

  override def run = BlazeServerBuilder[IO]
    .bindHttp(8080, "0.0.0.0")
    .withHttpApp(routes.orNotFound)
    .serve
    .compile
    .drain
}
