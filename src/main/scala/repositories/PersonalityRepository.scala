package repositories

import models.Personality
import org.mongodb.scala.bson.ObjectId
import zio._

trait PersonalityRepository {
  def create(personality: Personality): Task[Personality]
  def findById(id: ObjectId): Task[Option[Personality]]
  def list(): Task[List[Personality]]
  def update(personality: Personality): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object PersonalityRepository {
  def create(personality: Personality): RIO[PersonalityRepository, Personality] =
    ZIO.serviceWithZIO[PersonalityRepository](_.create(personality))

  def findById(id: ObjectId): RIO[PersonalityRepository, Option[Personality]] =
    ZIO.serviceWithZIO[PersonalityRepository](_.findById(id))

  def list(): RIO[PersonalityRepository, List[Personality]] =
    ZIO.serviceWithZIO[PersonalityRepository](_.list())

  def update(personality: Personality): RIO[PersonalityRepository, Boolean] =
    ZIO.serviceWithZIO[PersonalityRepository](_.update(personality))

  def delete(id: ObjectId): RIO[PersonalityRepository, Boolean] =
    ZIO.serviceWithZIO[PersonalityRepository](_.delete(id))
}
