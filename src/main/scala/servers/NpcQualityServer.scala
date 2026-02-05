package servers

import models.NpcQuality
import repositories.NpcQualityRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class NpcQualityServer(repo: NpcQualityRepository, cache: Cache[Unit, Throwable, List[NpcQuality]]) {
  def getQualities: Task[List[NpcQuality]] = cache.get(())
}

object NpcQualityServer {
  val live: ZLayer[NpcQualityRepository, Throwable, NpcQualityServer] =
    ZLayer.fromZIO {
      for {
        repo <- ZIO.service[NpcQualityRepository]
        cache <- Cache.make(
          capacity = 1,
          timeToLive = 5.minutes,
          lookup = Lookup((_: Unit) => repo.list())
        )
      } yield NpcQualityServer(repo, cache)
    }
}
