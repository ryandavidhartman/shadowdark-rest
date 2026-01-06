package repositories

import models.Monster
import org.mongodb.scala.bson.ObjectId
import zio._

trait MonsterRepository {
  def create(monster: Monster): Task[Monster]
  def findById(id: ObjectId): Task[Option[Monster]]
  def list(): Task[List[Monster]]
  def update(monster: Monster): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object MonsterRepository {
  def create(monster: Monster): RIO[MonsterRepository, Monster] =
    ZIO.serviceWithZIO[MonsterRepository](_.create(monster))

  def findById(id: ObjectId): RIO[MonsterRepository, Option[Monster]] =
    ZIO.serviceWithZIO[MonsterRepository](_.findById(id))

  def list(): RIO[MonsterRepository, List[Monster]] =
    ZIO.serviceWithZIO[MonsterRepository](_.list())

  def update(monster: Monster): RIO[MonsterRepository, Boolean] =
    ZIO.serviceWithZIO[MonsterRepository](_.update(monster))

  def delete(id: ObjectId): RIO[MonsterRepository, Boolean] =
    ZIO.serviceWithZIO[MonsterRepository](_.delete(id))
}
