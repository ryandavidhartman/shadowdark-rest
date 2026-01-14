package repositories

import models.NpcQuality
import org.mongodb.scala.bson.ObjectId
import zio._

trait NpcQualityRepository {
  def create(quality: NpcQuality): Task[NpcQuality]
  def findById(id: ObjectId): Task[Option[NpcQuality]]
  def list(): Task[List[NpcQuality]]
  def update(quality: NpcQuality): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object NpcQualityRepository {
  def create(quality: NpcQuality): RIO[NpcQualityRepository, NpcQuality] =
    ZIO.serviceWithZIO[NpcQualityRepository](_.create(quality))

  def findById(id: ObjectId): RIO[NpcQualityRepository, Option[NpcQuality]] =
    ZIO.serviceWithZIO[NpcQualityRepository](_.findById(id))

  def list(): RIO[NpcQualityRepository, List[NpcQuality]] =
    ZIO.serviceWithZIO[NpcQualityRepository](_.list())

  def update(quality: NpcQuality): RIO[NpcQualityRepository, Boolean] =
    ZIO.serviceWithZIO[NpcQualityRepository](_.update(quality))

  def delete(id: ObjectId): RIO[NpcQualityRepository, Boolean] =
    ZIO.serviceWithZIO[NpcQualityRepository](_.delete(id))
}
