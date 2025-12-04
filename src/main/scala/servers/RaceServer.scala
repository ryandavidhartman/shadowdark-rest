package servers

import models.Race
import repositories.RaceRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class RaceServer(repo: RaceRepository, cache: Cache[Unit, Throwable, List[Race]]) {
  def getRaces: Task[List[Race]] = cache.get(())
}

object RaceServer {
  val live: ZLayer[RaceRepository, Throwable, RaceServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[RaceRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield RaceServer(repo, cache)
    }
}
