package repositories

import models.Race
import org.mongodb.scala.bson.ObjectId
import zio._

// Abstraction over the Mongo "Race" collection.
trait RaceRepository {
  def create(race: Race): Task[Race]
  def findById(id: ObjectId): Task[Option[Race]]
  def list(): Task[List[Race]]
  def update(race: Race): Task[Boolean] // returns true if a document was modified
  def delete(id: ObjectId): Task[Boolean] // returns true if a document was deleted
}

object RaceRepository {
  // Accessor helpers
  def create(race: Race): RIO[RaceRepository, Race] =
    ZIO.serviceWithZIO[RaceRepository](_.create(race))

  def findById(id: ObjectId): RIO[RaceRepository, Option[Race]] =
    ZIO.serviceWithZIO[RaceRepository](_.findById(id))

  def list(): RIO[RaceRepository, List[Race]] =
    ZIO.serviceWithZIO[RaceRepository](_.list())

  def update(race: Race): RIO[RaceRepository, Boolean] =
    ZIO.serviceWithZIO[RaceRepository](_.update(race))

  def delete(id: ObjectId): RIO[RaceRepository, Boolean] =
    ZIO.serviceWithZIO[RaceRepository](_.delete(id))
}
