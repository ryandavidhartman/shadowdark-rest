package servers

import org.apache.pdfbox.Loader
import support.TestData
import zio.ZIO
import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

object SettlementServerSpec extends ZIOSpecDefault {
  override def spec =
    suite("SettlementServer")(
      test("randomSettlement produces districts and a single seat of government") {
        for {
          server <- TestData.settlementServer
          settlement <- server.randomSettlement
        } yield {
          val seats = settlement.districts.filter(_.seatOfGovernment)
          val poiIds = settlement.districts.flatMap(_.pointsOfInterest.map(_.id))
          val poiIdsUnique = poiIds.distinct.size == poiIds.size
          val buildingIds = settlement.districts.flatMap(_.buildings.map(_.id)).toSet
          val poiBuildingRefsValid =
            settlement.districts
              .flatMap(_.pointsOfInterest.flatMap(_.buildingId))
              .forall(buildingIds.contains)
          assertTrue(
            settlement.districts.nonEmpty,
            seats.size == 1,
            seats.headOption.exists(_.id == settlement.seatOfGovernment),
            settlement.layout.outline.size >= 3,
            settlement.districts.forall(_.boundary.size >= 3),
            poiIdsUnique,
            poiBuildingRefsValid
          )
        }
      },
      test("renderSettlementPdf returns a non-empty PDF payload") {
        for {
          server <- TestData.settlementServer
          settlement <- server.randomSettlement
          pdf <- server.renderSettlementPdf(settlement)
          doc <- ZIO.attempt(Loader.loadPDF(pdf))
          pages <- ZIO.attempt(doc.getNumberOfPages)
          _ <- ZIO.attempt(doc.close())
        } yield assertTrue(
          pdf.nonEmpty,
          new String(pdf.take(5), java.nio.charset.StandardCharsets.US_ASCII) == "%PDF-",
          pages >= 2
        )
      }
    )
}
