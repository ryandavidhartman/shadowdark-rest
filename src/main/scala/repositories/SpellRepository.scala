package repositories

import models.Spell
import org.mongodb.scala.bson.ObjectId
import zio._

trait SpellRepository {
  def create(spell: Spell): Task[Spell]
  def findById(id: ObjectId): Task[Option[Spell]]
  def list(): Task[List[Spell]]
  def update(spell: Spell): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object SpellRepository {
  def create(spell: Spell): RIO[SpellRepository, Spell] =
    ZIO.serviceWithZIO[SpellRepository](_.create(spell))

  def findById(id: ObjectId): RIO[SpellRepository, Option[Spell]] =
    ZIO.serviceWithZIO[SpellRepository](_.findById(id))

  def list(): RIO[SpellRepository, List[Spell]] =
    ZIO.serviceWithZIO[SpellRepository](_.list())

  def update(spell: Spell): RIO[SpellRepository, Boolean] =
    ZIO.serviceWithZIO[SpellRepository](_.update(spell))

  def delete(id: ObjectId): RIO[SpellRepository, Boolean] =
    ZIO.serviceWithZIO[SpellRepository](_.delete(id))
}
