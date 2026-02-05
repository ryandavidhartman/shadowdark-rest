package routes

import models.{Dungeon, HexMap, Settlement}
import org.apache.pdfbox.Loader
import servers.{DungeonServer, HexMapServer}
import support.TestData
import zio.ZIO
import zio.http._
import zio.json._
import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

object RouteSmokeSpec extends ZIOSpecDefault {
  private def run(routes: Routes[Any, Response], request: Request): ZIO[Any, Nothing, Response] =
    ZIO.scoped(routes.runZIO(request))

  override def spec =
    suite("Routes")(
      test("GET /dungeons/random returns JSON") {
        val routes = DungeonRoute(DungeonServer()).routes
        val request = Request(method = Method.GET, url = URL.root / "dungeons" / "random")
        for {
          response <- run(routes, request)
          body <- response.body.asString
          decoded <- ZIO.fromEither(body.fromJson[Dungeon])
        } yield assertTrue(
          response.status == Status.Ok,
          response.headers.get("Content-Type").exists(_.contains("application/json")),
          decoded.rooms.nonEmpty
        )
      },
      test("GET /dungeons/random.pdf returns PDF") {
        val routes = DungeonRoute(DungeonServer()).routes
        val request = Request(method = Method.GET, url = URL.root / "dungeons" / "random.pdf")
        for {
          response <- run(routes, request)
          bytes <- response.body.asArray
          doc <- ZIO.attempt(Loader.loadPDF(bytes))
          pages <- ZIO.attempt(doc.getNumberOfPages)
          _ <- ZIO.attempt(doc.close())
        } yield assertTrue(
          response.status == Status.Ok,
          response.headers.get("Content-Type").contains("application/pdf"),
          new String(bytes.take(5), java.nio.charset.StandardCharsets.US_ASCII) == "%PDF-",
          pages >= 1
        )
      },
      test("GET /settlements/random returns JSON") {
        for {
          server <- TestData.settlementServer
          routes = SettlementRoute(server).routes
          request = Request(method = Method.GET, url = URL.root / "settlements" / "random")
          response <- run(routes, request)
          body <- response.body.asString
          decoded <- ZIO.fromEither(body.fromJson[Settlement])
        } yield assertTrue(
          response.status == Status.Ok,
          response.headers.get("Content-Type").exists(_.contains("application/json")),
          decoded.districts.nonEmpty
        )
      },
      test("GET /settlements/random.pdf returns PDF") {
        for {
          server <- TestData.settlementServer
          routes = SettlementRoute(server).routes
          request = Request(method = Method.GET, url = URL.root / "settlements" / "random.pdf")
          response <- run(routes, request)
          bytes <- response.body.asArray
          doc <- ZIO.attempt(Loader.loadPDF(bytes))
          pages <- ZIO.attempt(doc.getNumberOfPages)
          _ <- ZIO.attempt(doc.close())
        } yield assertTrue(
          response.status == Status.Ok,
          response.headers.get("Content-Type").contains("application/pdf"),
          new String(bytes.take(5), java.nio.charset.StandardCharsets.US_ASCII) == "%PDF-",
          pages >= 2
        )
      },
      test("GET /hexes/random returns JSON") {
        val routes = HexMapRoute(HexMapServer()).routes
        val request = Request(method = Method.GET, url = URL.root / "hexes" / "random")
        for {
          response <- run(routes, request)
          body <- response.body.asString
          decoded <- ZIO.fromEither(body.fromJson[HexMap])
        } yield assertTrue(
          response.status == Status.Ok,
          response.headers.get("Content-Type").exists(_.contains("application/json")),
          decoded.hexes.size == 7
        )
      },
      test("POST /hexes/render pdf returns PDF") {
        val server = HexMapServer()
        val routes = HexMapRoute(server).routes
        for {
          map <- server.randomMap
          payload = s"""{"map":${map.toJson},"type":"pdf"}"""
          request = Request(method = Method.POST, url = URL.root / "hexes" / "render", body = Body.fromString(payload))
          response <- run(routes, request)
          bytes <- response.body.asArray
          doc <- ZIO.attempt(Loader.loadPDF(bytes))
          pages <- ZIO.attempt(doc.getNumberOfPages)
          _ <- ZIO.attempt(doc.close())
        } yield assertTrue(
          response.status == Status.Ok,
          response.headers.get("Content-Type").contains("application/pdf"),
          new String(bytes.take(5), java.nio.charset.StandardCharsets.US_ASCII) == "%PDF-",
          pages >= 1
        )
      },
      test("POST /hexes/render png returns PNG") {
        val server = HexMapServer()
        val routes = HexMapRoute(server).routes
        for {
          map <- server.randomMap
          payload = s"""{"map":${map.toJson},"type":"png"}"""
          request = Request(method = Method.POST, url = URL.root / "hexes" / "render", body = Body.fromString(payload))
          response <- run(routes, request)
          bytes <- response.body.asArray
        } yield assertTrue(
          response.status == Status.Ok,
          response.headers.get("Content-Type").contains("image/png"),
          bytes.take(8).sameElements(Array(0x89.toByte, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))
        )
      }
    )
}
