package servers

import models.SettlementName
import repositories.SettlementNameRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class SettlementNameServer(repo: SettlementNameRepository, cache: Cache[Unit, Throwable, List[SettlementName]]) {
  def getSettlementNames: Task[List[SettlementName]] = cache.get(())
}

object SettlementNameServer {
  val live: ZLayer[SettlementNameRepository, Throwable, SettlementNameServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[SettlementNameRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield SettlementNameServer(repo, cache)
    }
}
