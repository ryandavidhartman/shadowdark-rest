package repositories

import models.Background
import org.mongodb.scala.bson.ObjectId
import zio._

trait BackgroundRepository {
  def create(background: Background): Task[Background]
  def findById(id: ObjectId): Task[Option[Background]]
  def list(): Task[List[Background]]
  def update(background: Background): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object BackgroundRepository {
  def create(background: Background): RIO[BackgroundRepository, Background] =
    ZIO.serviceWithZIO[BackgroundRepository](_.create(background))

  def findById(id: ObjectId): RIO[BackgroundRepository, Option[Background]] =
    ZIO.serviceWithZIO[BackgroundRepository](_.findById(id))

  def list(): RIO[BackgroundRepository, List[Background]] =
    ZIO.serviceWithZIO[BackgroundRepository](_.list())

  def update(background: Background): RIO[BackgroundRepository, Boolean] =
    ZIO.serviceWithZIO[BackgroundRepository](_.update(background))

  def delete(id: ObjectId): RIO[BackgroundRepository, Boolean] =
    ZIO.serviceWithZIO[BackgroundRepository](_.delete(id))
}
