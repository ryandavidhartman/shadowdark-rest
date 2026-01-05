package servers

import models.Deity
import repositories.DeityRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class DeityServer(repo: DeityRepository, cache: Cache[Unit, Throwable, List[Deity]]) {
  def getDeities: Task[List[Deity]] = cache.get(())
}

object DeityServer {
  val live: ZLayer[DeityRepository, Throwable, DeityServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[DeityRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield DeityServer(repo, cache)
    }
}
