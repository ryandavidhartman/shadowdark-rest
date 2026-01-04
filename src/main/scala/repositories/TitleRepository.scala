package repositories

import models.Title
import org.mongodb.scala.bson.ObjectId
import zio._

trait TitleRepository {
  def create(title: Title): Task[Title]
  def findById(id: ObjectId): Task[Option[Title]]
  def list(): Task[List[Title]]
  def update(title: Title): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object TitleRepository {
  def create(title: Title): RIO[TitleRepository, Title] =
    ZIO.serviceWithZIO[TitleRepository](_.create(title))

  def findById(id: ObjectId): RIO[TitleRepository, Option[Title]] =
    ZIO.serviceWithZIO[TitleRepository](_.findById(id))

  def list(): RIO[TitleRepository, List[Title]] =
    ZIO.serviceWithZIO[TitleRepository](_.list())

  def update(title: Title): RIO[TitleRepository, Boolean] =
    ZIO.serviceWithZIO[TitleRepository](_.update(title))

  def delete(id: ObjectId): RIO[TitleRepository, Boolean] =
    ZIO.serviceWithZIO[TitleRepository](_.delete(id))
}
