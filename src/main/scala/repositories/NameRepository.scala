package repositories

import models.Name
import org.mongodb.scala.bson.ObjectId
import zio._

// Abstraction over the Mongo "Name" collection.
trait NameRepository {
  def create(name: Name): Task[Name]
  def findById(id: ObjectId): Task[Option[Name]]
  def list(): Task[List[Name]]
  def update(name: Name): Task[Boolean] // returns true if a document was modified
  def delete(id: ObjectId): Task[Boolean] // returns true if a document was deleted
}

object NameRepository {
  // Accessor helpers
  def create(name: Name): RIO[NameRepository, Name] =
    ZIO.serviceWithZIO[NameRepository](_.create(name))

  def findById(id: ObjectId): RIO[NameRepository, Option[Name]] =
    ZIO.serviceWithZIO[NameRepository](_.findById(id))

  def list(): RIO[NameRepository, List[Name]] =
    ZIO.serviceWithZIO[NameRepository](_.list())

  def update(name: Name): RIO[NameRepository, Boolean] =
    ZIO.serviceWithZIO[NameRepository](_.update(name))

  def delete(id: ObjectId): RIO[NameRepository, Boolean] =
    ZIO.serviceWithZIO[NameRepository](_.delete(id))
}
