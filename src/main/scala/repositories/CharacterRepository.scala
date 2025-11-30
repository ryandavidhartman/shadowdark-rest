package repositories

import models.Character
import org.mongodb.scala.bson.ObjectId
import zio._

// Abstraction over the Mongo "Character" collection.
trait CharacterRepository {
  def create(character: Character): Task[Character]
  def findById(id: ObjectId): Task[Option[Character]]
  def list(): Task[List[Character]]
  def update(character: Character): Task[Boolean] // returns true if a document was modified
  def delete(id: ObjectId): Task[Boolean] // returns true if a document was deleted
}

object CharacterRepository {
  // Accessor helpers
  def create(character: Character): RIO[CharacterRepository, Character] =
    ZIO.serviceWithZIO[CharacterRepository](_.create(character))

  def findById(id: ObjectId): RIO[CharacterRepository, Option[Character]] =
    ZIO.serviceWithZIO[CharacterRepository](_.findById(id))

  def list(): RIO[CharacterRepository, List[Character]] =
    ZIO.serviceWithZIO[CharacterRepository](_.list())

  def update(character: Character): RIO[CharacterRepository, Boolean] =
    ZIO.serviceWithZIO[CharacterRepository](_.update(character))

  def delete(id: ObjectId): RIO[CharacterRepository, Boolean] =
    ZIO.serviceWithZIO[CharacterRepository](_.delete(id))
}
