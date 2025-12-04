package repositories

import models.StoredCharacterClass
import org.mongodb.scala.bson.ObjectId
import zio._

trait CharacterClassRepository {
  def create(characterClass: StoredCharacterClass): Task[StoredCharacterClass]
  def upsertByName(characterClass: StoredCharacterClass): Task[StoredCharacterClass]
  def findById(id: ObjectId): Task[Option[StoredCharacterClass]]
  def findByName(name: String): Task[Option[StoredCharacterClass]]
  def list(): Task[List[StoredCharacterClass]]
  def delete(id: ObjectId): Task[Boolean] // returns true if a document was deleted
  def seedDefaults(defaults: List[StoredCharacterClass]): Task[List[StoredCharacterClass]]
}

object CharacterClassRepository {
  def create(characterClass: StoredCharacterClass): RIO[CharacterClassRepository, StoredCharacterClass] =
    ZIO.serviceWithZIO[CharacterClassRepository](_.create(characterClass))

  def upsertByName(characterClass: StoredCharacterClass): RIO[CharacterClassRepository, StoredCharacterClass] =
    ZIO.serviceWithZIO[CharacterClassRepository](_.upsertByName(characterClass))

  def findById(id: ObjectId): RIO[CharacterClassRepository, Option[StoredCharacterClass]] =
    ZIO.serviceWithZIO[CharacterClassRepository](_.findById(id))

  def findByName(name: String): RIO[CharacterClassRepository, Option[StoredCharacterClass]] =
    ZIO.serviceWithZIO[CharacterClassRepository](_.findByName(name))

  def list(): RIO[CharacterClassRepository, List[StoredCharacterClass]] =
    ZIO.serviceWithZIO[CharacterClassRepository](_.list())

  def delete(id: ObjectId): RIO[CharacterClassRepository, Boolean] =
    ZIO.serviceWithZIO[CharacterClassRepository](_.delete(id))

  def seedDefaults(defaults: List[StoredCharacterClass]): RIO[CharacterClassRepository, List[StoredCharacterClass]] =
    ZIO.serviceWithZIO[CharacterClassRepository](_.seedDefaults(defaults))
}
