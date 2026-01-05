package repositories

import models.Deity
import org.mongodb.scala.bson.ObjectId
import zio._

trait DeityRepository {
  def create(deity: Deity): Task[Deity]
  def findById(id: ObjectId): Task[Option[Deity]]
  def list(): Task[List[Deity]]
  def update(deity: Deity): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object DeityRepository {
  def create(deity: Deity): RIO[DeityRepository, Deity] =
    ZIO.serviceWithZIO[DeityRepository](_.create(deity))

  def findById(id: ObjectId): RIO[DeityRepository, Option[Deity]] =
    ZIO.serviceWithZIO[DeityRepository](_.findById(id))

  def list(): RIO[DeityRepository, List[Deity]] =
    ZIO.serviceWithZIO[DeityRepository](_.list())

  def update(deity: Deity): RIO[DeityRepository, Boolean] =
    ZIO.serviceWithZIO[DeityRepository](_.update(deity))

  def delete(id: ObjectId): RIO[DeityRepository, Boolean] =
    ZIO.serviceWithZIO[DeityRepository](_.delete(id))
}
