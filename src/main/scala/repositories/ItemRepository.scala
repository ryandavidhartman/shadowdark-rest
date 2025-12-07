package repositories

import models.Item
import org.mongodb.scala.bson.ObjectId
import zio._

trait ItemRepository {
  def create(item: Item): Task[Item]
  def findById(id: ObjectId): Task[Option[Item]]
  def list(): Task[List[Item]]
  def update(item: Item): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object ItemRepository {
  def create(item: Item): RIO[ItemRepository, Item] =
    ZIO.serviceWithZIO[ItemRepository](_.create(item))

  def findById(id: ObjectId): RIO[ItemRepository, Option[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.findById(id))

  def list(): RIO[ItemRepository, List[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.list())

  def update(item: Item): RIO[ItemRepository, Boolean] =
    ZIO.serviceWithZIO[ItemRepository](_.update(item))

  def delete(id: ObjectId): RIO[ItemRepository, Boolean] =
    ZIO.serviceWithZIO[ItemRepository](_.delete(id))
}
