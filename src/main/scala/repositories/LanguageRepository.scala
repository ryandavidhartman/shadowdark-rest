package repositories

import models.LanguageEntry
import org.mongodb.scala.bson.ObjectId
import zio._

trait LanguageRepository {
  def create(language: LanguageEntry): Task[LanguageEntry]
  def findById(id: ObjectId): Task[Option[LanguageEntry]]
  def list(): Task[List[LanguageEntry]]
  def update(language: LanguageEntry): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object LanguageRepository {
  def create(language: LanguageEntry): RIO[LanguageRepository, LanguageEntry] =
    ZIO.serviceWithZIO[LanguageRepository](_.create(language))

  def findById(id: ObjectId): RIO[LanguageRepository, Option[LanguageEntry]] =
    ZIO.serviceWithZIO[LanguageRepository](_.findById(id))

  def list(): RIO[LanguageRepository, List[LanguageEntry]] =
    ZIO.serviceWithZIO[LanguageRepository](_.list())

  def update(language: LanguageEntry): RIO[LanguageRepository, Boolean] =
    ZIO.serviceWithZIO[LanguageRepository](_.update(language))

  def delete(id: ObjectId): RIO[LanguageRepository, Boolean] =
    ZIO.serviceWithZIO[LanguageRepository](_.delete(id))
}
