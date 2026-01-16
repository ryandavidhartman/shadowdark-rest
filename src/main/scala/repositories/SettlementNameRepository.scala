package repositories

import models.SettlementName
import org.mongodb.scala.bson.ObjectId
import zio._

trait SettlementNameRepository {
  def create(settlementName: SettlementName): Task[SettlementName]
  def findById(id: ObjectId): Task[Option[SettlementName]]
  def list(): Task[List[SettlementName]]
  def update(settlementName: SettlementName): Task[Boolean]
  def delete(id: ObjectId): Task[Boolean]
}

object SettlementNameRepository {
  def create(settlementName: SettlementName): RIO[SettlementNameRepository, SettlementName] =
    ZIO.serviceWithZIO[SettlementNameRepository](_.create(settlementName))

  def findById(id: ObjectId): RIO[SettlementNameRepository, Option[SettlementName]] =
    ZIO.serviceWithZIO[SettlementNameRepository](_.findById(id))

  def list(): RIO[SettlementNameRepository, List[SettlementName]] =
    ZIO.serviceWithZIO[SettlementNameRepository](_.list())

  def update(settlementName: SettlementName): RIO[SettlementNameRepository, Boolean] =
    ZIO.serviceWithZIO[SettlementNameRepository](_.update(settlementName))

  def delete(id: ObjectId): RIO[SettlementNameRepository, Boolean] =
    ZIO.serviceWithZIO[SettlementNameRepository](_.delete(id))
}
