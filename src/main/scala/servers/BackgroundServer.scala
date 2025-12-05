package servers

import models.Background
import repositories.BackgroundRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class BackgroundServer(repo: BackgroundRepository, cache: Cache[Unit, Throwable, List[Background]]) {
  def getBackgrounds: Task[List[Background]] = cache.get(())
}

object BackgroundServer {
  val live: ZLayer[BackgroundRepository, Throwable, BackgroundServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[BackgroundRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield BackgroundServer(repo, cache)
    }
}
