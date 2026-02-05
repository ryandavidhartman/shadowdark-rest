package repositories

import com.typesafe.config.ConfigFactory
import models._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.ReplaceOptions
import zio._

import scala.concurrent.ExecutionContext

object CharacterClassRepositoryLive {

  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val codecRegistry =
    fromRegistries(
      fromProviders(
        Macros.createCodecProviderIgnoreNone[LanguageBenefit],
        Macros.createCodecProviderIgnoreNone[ClassFeature],
        Macros.createCodecProviderIgnoreNone[SpellTiers],
        Macros.createCodecProviderIgnoreNone[SpellProgression],
        Macros.createCodecProviderIgnoreNone[Spellcasting],
        Macros.createCodecProviderIgnoreNone[DiceRange],
        Macros.createCodecProviderIgnoreNone[Talent],
        Macros.createCodecProviderIgnoreNone[LevelRange],
        Macros.createCodecProviderIgnoreNone[ClassTitle],
        Macros.createCodecProviderIgnoreNone[StoredCharacterClass]
      ),
      MongoClient.DEFAULT_CODEC_REGISTRY
    )

  private def loadConfig = ZIO.attempt(ConfigFactory.load())

  private def resolveUri(cfg: com.typesafe.config.Config): Task[String] =
    ZIO
      .fromOption(
        List("mongo.uri", "mongodb.uri").collectFirst {
          case path if cfg.hasPath(path) => cfg.getString(path)
        }
      )
      .orElseFail(new RuntimeException("Missing Mongo URI config (expected mongo.uri or mongodb.uri)"))

  private def resolveDatabase(cfg: com.typesafe.config.Config, uri: String): Task[String] =
    ZIO
      .succeed {
        if (cfg.hasPath("mongo.database")) Some(cfg.getString("mongo.database"))
        else if (cfg.hasPath("mongodb.database")) Some(cfg.getString("mongodb.database"))
        else {
          val cs = new ConnectionString(uri)
          Option(cs.getDatabase)
        }
      }
      .someOrElse("shadowdark")

  private def resolveCollection(cfg: com.typesafe.config.Config): String =
    if (cfg.hasPath("mongo.collection")) cfg.getString("mongo.collection")
    else if (cfg.hasPath("mongodb.collection")) cfg.getString("mongodb.collection")
    else "Classes"

  val layer: ZLayer[Any, Throwable, CharacterClassRepository] = ZLayer.scoped {
    for {
      cfg <- loadConfig
      uri <- resolveUri(cfg)
      dbName <- resolveDatabase(cfg, uri)
      collection <- ZIO.attempt(resolveCollection(cfg))
      client <- ZIO.fromAutoCloseable(ZIO.attempt(MongoClient(uri)))
      coll <- ZIO.attempt {
        client
          .getDatabase(dbName)
          .withCodecRegistry(codecRegistry)
          .getCollection[StoredCharacterClass](collection)
      }
    } yield new CharacterClassRepository {

      private def fromFuture[A](fa: => scala.concurrent.Future[A]): Task[A] =
        ZIO.fromFuture(_ => fa)

      private def replace(characterClass: StoredCharacterClass): Task[StoredCharacterClass] =
        fromFuture(
          coll
            .replaceOne(Filters.equal("_id", characterClass._id), characterClass, ReplaceOptions().upsert(false))
            .toFuture()
        ).as(characterClass)

      override def create(characterClass: StoredCharacterClass): Task[StoredCharacterClass] =
        fromFuture(coll.insertOne(characterClass).toFuture()).as(characterClass)

      override def upsertByName(characterClass: StoredCharacterClass): Task[StoredCharacterClass] =
        findByName(characterClass.name).flatMap {
          case Some(existing) => replace(characterClass.copy(_id = existing._id))
          case None           => create(characterClass)
        }

      override def findById(id: ObjectId): Task[Option[StoredCharacterClass]] =
        fromFuture(coll.find(Filters.equal("_id", id)).headOption())

      override def findByName(name: String): Task[Option[StoredCharacterClass]] =
        fromFuture(coll.find(Filters.equal("name", name)).headOption())

      override def list(): Task[List[StoredCharacterClass]] =
        fromFuture(coll.find().toFuture()).map(_.toList)

      override def delete(id: ObjectId): Task[Boolean] =
        fromFuture(coll.deleteOne(Filters.equal("_id", id)).toFuture())
          .map(result => result.wasAcknowledged() && result.getDeletedCount > 0)

      override def seedDefaults(defaults: List[StoredCharacterClass]): Task[List[StoredCharacterClass]] =
        ZIO.foreach(defaults)(upsertByName)
    }
  }
}
