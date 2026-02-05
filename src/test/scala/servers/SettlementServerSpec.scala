package servers

import models.{Background, BackgroundRange, Name, NpcQuality, Personality, Race, RaceAbility, SettlementName}
import org.mongodb.scala.bson.ObjectId
import repositories.{
  BackgroundRepository,
  NameRepository,
  NpcQualityRepository,
  PersonalityRepository,
  RaceRepository,
  SettlementNameRepository
}
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt
import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

object SettlementServerSpec extends ZIOSpecDefault {
  private def cacheFor[A](values: List[A]): ZIO[Any, Throwable, Cache[Unit, Throwable, List[A]]] =
    Cache.make(
      capacity = 1,
      timeToLive = 5.minutes,
      lookup = Lookup((_: Unit) => ZIO.succeed(values))
    )

  private def nameRepo(values: List[Name]): NameRepository =
    new NameRepository {
      def create(name: Name): Task[Name] = ZIO.succeed(name)
      def findById(id: ObjectId): Task[Option[Name]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Name]] = ZIO.succeed(values)
      def update(name: Name): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  private def raceRepo(values: List[Race]): RaceRepository =
    new RaceRepository {
      def create(race: Race): Task[Race] = ZIO.succeed(race)
      def findById(id: ObjectId): Task[Option[Race]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Race]] = ZIO.succeed(values)
      def update(race: Race): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  private def personalityRepo(values: List[Personality]): PersonalityRepository =
    new PersonalityRepository {
      def create(personality: Personality): Task[Personality] = ZIO.succeed(personality)
      def findById(id: ObjectId): Task[Option[Personality]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Personality]] = ZIO.succeed(values)
      def update(personality: Personality): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  private def backgroundRepo(values: List[Background]): BackgroundRepository =
    new BackgroundRepository {
      def create(background: Background): Task[Background] = ZIO.succeed(background)
      def findById(id: ObjectId): Task[Option[Background]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Background]] = ZIO.succeed(values)
      def update(background: Background): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  private def npcQualityRepo(values: List[NpcQuality]): NpcQualityRepository =
    new NpcQualityRepository {
      def create(quality: NpcQuality): Task[NpcQuality] = ZIO.succeed(quality)
      def findById(id: ObjectId): Task[Option[NpcQuality]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[NpcQuality]] = ZIO.succeed(values)
      def update(quality: NpcQuality): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  private def settlementNameRepo(values: List[SettlementName]): SettlementNameRepository =
    new SettlementNameRepository {
      def create(name: SettlementName): Task[SettlementName] = ZIO.succeed(name)
      def findById(id: ObjectId): Task[Option[SettlementName]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[SettlementName]] = ZIO.succeed(values)
      def update(name: SettlementName): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  override def spec =
    suite("SettlementServer")(
      test("randomSettlement produces districts and a single seat of government") {
        val names = List(
          Name(new ObjectId(), "Ava", "Human", firstName = Some(true)),
          Name(new ObjectId(), "Rowan", "Human", lastName = Some(true))
        )
        val races = List(
          Race(new ObjectId(), "Human", RaceAbility("Adaptable", "Test"), List("Common"), 1.0)
        )
        val personalities = List(Personality(new ObjectId(), "Quiet", "Neutral"))
        val backgrounds = List(
          Background(
            new ObjectId(),
            "Baker",
            BackgroundRange(1, 1),
            "Bread",
            "Kneads dough",
            poiKinds = None,
            poiNames = None
          )
        )
        val qualities = List(
          NpcQuality(new ObjectId(), "appearance", "Unremarkable"),
          NpcQuality(new ObjectId(), "does", "Quiet"),
          NpcQuality(new ObjectId(), "secret", "Unknown"),
          NpcQuality(new ObjectId(), "age", "Adult"),
          NpcQuality(new ObjectId(), "wealth", "Standard")
        )
        val settlementNames = List(SettlementName(new ObjectId(), "Teston", "Village"))

        for {
          nameCache <- cacheFor(names)
          raceCache <- cacheFor(races)
          personalityCache <- cacheFor(personalities)
          backgroundCache <- cacheFor(backgrounds)
          qualityCache <- cacheFor(qualities)
          settlementNameCache <- cacheFor(settlementNames)
          server = SettlementServer(
            NameServer(nameRepo(names), nameCache),
            RaceServer(raceRepo(races), raceCache),
            PersonalityServer(personalityRepo(personalities), personalityCache),
            BackgroundServer(backgroundRepo(backgrounds), backgroundCache),
            NpcQualityServer(npcQualityRepo(qualities), qualityCache),
            SettlementNameServer(settlementNameRepo(settlementNames), settlementNameCache)
          )
          settlement <- server.randomSettlement
        } yield {
          val seats = settlement.districts.filter(_.seatOfGovernment)
          assertTrue(
            settlement.districts.nonEmpty,
            seats.size == 1,
            seats.headOption.exists(_.id == settlement.seatOfGovernment),
            settlement.layout.outline.size >= 3,
            settlement.districts.forall(_.boundary.size >= 3)
          )
        }
      },
      test("renderSettlementPdf returns a non-empty PDF payload") {
        val names = List(
          Name(new ObjectId(), "Ava", "Human", firstName = Some(true)),
          Name(new ObjectId(), "Rowan", "Human", lastName = Some(true))
        )
        val races = List(
          Race(new ObjectId(), "Human", RaceAbility("Adaptable", "Test"), List("Common"), 1.0)
        )
        val personalities = List(Personality(new ObjectId(), "Quiet", "Neutral"))
        val backgrounds = List(
          Background(
            new ObjectId(),
            "Baker",
            BackgroundRange(1, 1),
            "Bread",
            "Kneads dough",
            poiKinds = None,
            poiNames = None
          )
        )
        val qualities = List(
          NpcQuality(new ObjectId(), "appearance", "Unremarkable"),
          NpcQuality(new ObjectId(), "does", "Quiet"),
          NpcQuality(new ObjectId(), "secret", "Unknown"),
          NpcQuality(new ObjectId(), "age", "Adult"),
          NpcQuality(new ObjectId(), "wealth", "Standard")
        )
        val settlementNames = List(SettlementName(new ObjectId(), "Teston", "Village"))

        for {
          nameCache <- cacheFor(names)
          raceCache <- cacheFor(races)
          personalityCache <- cacheFor(personalities)
          backgroundCache <- cacheFor(backgrounds)
          qualityCache <- cacheFor(qualities)
          settlementNameCache <- cacheFor(settlementNames)
          server = SettlementServer(
            NameServer(nameRepo(names), nameCache),
            RaceServer(raceRepo(races), raceCache),
            PersonalityServer(personalityRepo(personalities), personalityCache),
            BackgroundServer(backgroundRepo(backgrounds), backgroundCache),
            NpcQualityServer(npcQualityRepo(qualities), qualityCache),
            SettlementNameServer(settlementNameRepo(settlementNames), settlementNameCache)
          )
          settlement <- server.randomSettlement
          pdf <- server.renderSettlementPdf(settlement)
        } yield assertTrue(pdf.nonEmpty)
      }
    )
}
