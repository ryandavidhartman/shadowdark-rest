package repositories

import com.typesafe.config.ConfigFactory
import models.{Background, BackgroundRange}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.ReplaceOptions
import zio._

import scala.concurrent.ExecutionContext

object BackgroundRepositoryLive {

  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val codecRegistry =
    fromRegistries(
      fromProviders(
        Macros.createCodecProviderIgnoreNone[BackgroundRange],
        Macros.createCodecProviderIgnoreNone[Background]
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
    if (cfg.hasPath("mongo.background.collection")) cfg.getString("mongo.background.collection")
    else if (cfg.hasPath("mongodb.background.collection")) cfg.getString("mongodb.background.collection")
    else "Backgrounds"

  val layer: ZLayer[Any, Throwable, BackgroundRepository] = ZLayer.scoped {
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
          .getCollection[Background](collection)
      }
    } yield new BackgroundRepository {

      private def fromFuture[A](fa: => scala.concurrent.Future[A]): Task[A] =
        ZIO.fromFuture(_ => fa)

      override def create(background: Background): Task[Background] =
        fromFuture(coll.insertOne(background).toFuture()).as(background)

      override def findById(id: ObjectId): Task[Option[Background]] =
        fromFuture(coll.find(Filters.equal("_id", id)).headOption())

      override def list(): Task[List[Background]] =
        fromFuture(coll.find().toFuture()).map(_.toList)

      override def update(background: Background): Task[Boolean] =
        fromFuture(
          coll
            .replaceOne(Filters.equal("_id", background._id), background, ReplaceOptions().upsert(false))
            .toFuture()
        ).map(result => result.wasAcknowledged() && result.getModifiedCount > 0)

      override def delete(id: ObjectId): Task[Boolean] =
        fromFuture(coll.deleteOne(Filters.equal("_id", id)).toFuture())
          .map(result => result.wasAcknowledged() && result.getDeletedCount > 0)
    }
  }
}
