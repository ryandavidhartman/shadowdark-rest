package support

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
import servers.{
  BackgroundServer,
  NameServer,
  NpcQualityServer,
  PersonalityServer,
  RaceServer,
  SettlementNameServer,
  SettlementServer
}
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

object TestData {
  val names: List[Name] = List(
    Name(new ObjectId(), "Ava", "Human", firstName = Some(true)),
    Name(new ObjectId(), "Rowan", "Human", lastName = Some(true))
  )

  val races: List[Race] = List(
    Race(new ObjectId(), "Human", RaceAbility("Adaptable", "Test"), List("Common"), 1.0)
  )

  val personalities: List[Personality] = List(
    Personality(new ObjectId(), "Quiet", "Neutral")
  )

  val backgrounds: List[Background] = List(
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

  val qualities: List[NpcQuality] = List(
    NpcQuality(new ObjectId(), "appearance", "Unremarkable"),
    NpcQuality(new ObjectId(), "does", "Quiet"),
    NpcQuality(new ObjectId(), "secret", "Unknown"),
    NpcQuality(new ObjectId(), "age", "Adult"),
    NpcQuality(new ObjectId(), "wealth", "Standard")
  )

  val settlementNames: List[SettlementName] = List(
    SettlementName(new ObjectId(), "Teston", "Village")
  )

  def cacheFor[A](values: List[A]): ZIO[Any, Throwable, Cache[Unit, Throwable, List[A]]] =
    Cache.make(
      capacity = 1,
      timeToLive = 5.minutes,
      lookup = Lookup((_: Unit) => ZIO.succeed(values))
    )

  def nameRepo(values: List[Name]): NameRepository =
    new NameRepository {
      def create(name: Name): Task[Name] = ZIO.succeed(name)
      def findById(id: ObjectId): Task[Option[Name]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Name]] = ZIO.succeed(values)
      def update(name: Name): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  def raceRepo(values: List[Race]): RaceRepository =
    new RaceRepository {
      def create(race: Race): Task[Race] = ZIO.succeed(race)
      def findById(id: ObjectId): Task[Option[Race]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Race]] = ZIO.succeed(values)
      def update(race: Race): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  def personalityRepo(values: List[Personality]): PersonalityRepository =
    new PersonalityRepository {
      def create(personality: Personality): Task[Personality] = ZIO.succeed(personality)
      def findById(id: ObjectId): Task[Option[Personality]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Personality]] = ZIO.succeed(values)
      def update(personality: Personality): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  def backgroundRepo(values: List[Background]): BackgroundRepository =
    new BackgroundRepository {
      def create(background: Background): Task[Background] = ZIO.succeed(background)
      def findById(id: ObjectId): Task[Option[Background]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[Background]] = ZIO.succeed(values)
      def update(background: Background): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  def npcQualityRepo(values: List[NpcQuality]): NpcQualityRepository =
    new NpcQualityRepository {
      def create(quality: NpcQuality): Task[NpcQuality] = ZIO.succeed(quality)
      def findById(id: ObjectId): Task[Option[NpcQuality]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[NpcQuality]] = ZIO.succeed(values)
      def update(quality: NpcQuality): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  def settlementNameRepo(values: List[SettlementName]): SettlementNameRepository =
    new SettlementNameRepository {
      def create(name: SettlementName): Task[SettlementName] = ZIO.succeed(name)
      def findById(id: ObjectId): Task[Option[SettlementName]] = ZIO.succeed(values.find(_._id == id))
      def list(): Task[List[SettlementName]] = ZIO.succeed(values)
      def update(name: SettlementName): Task[Boolean] = ZIO.succeed(false)
      def delete(id: ObjectId): Task[Boolean] = ZIO.succeed(false)
    }

  def settlementServer: ZIO[Any, Throwable, SettlementServer] =
    for {
      nameCache <- cacheFor(names)
      raceCache <- cacheFor(races)
      personalityCache <- cacheFor(personalities)
      backgroundCache <- cacheFor(backgrounds)
      qualityCache <- cacheFor(qualities)
      settlementNameCache <- cacheFor(settlementNames)
    } yield SettlementServer(
      NameServer(nameRepo(names), nameCache),
      RaceServer(raceRepo(races), raceCache),
      PersonalityServer(personalityRepo(personalities), personalityCache),
      BackgroundServer(backgroundRepo(backgrounds), backgroundCache),
      NpcQualityServer(npcQualityRepo(qualities), qualityCache),
      SettlementNameServer(settlementNameRepo(settlementNames), settlementNameCache)
    )
}
