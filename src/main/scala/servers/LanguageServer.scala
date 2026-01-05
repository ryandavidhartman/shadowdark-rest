package servers

import models.LanguageEntry
import repositories.LanguageRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class LanguageServer(repo: LanguageRepository, cache: Cache[Unit, Throwable, List[LanguageEntry]]) {
  def getLanguages: Task[List[LanguageEntry]] = cache.get(())
}

object LanguageServer {
  val live: ZLayer[LanguageRepository, Throwable, LanguageServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[LanguageRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield LanguageServer(repo, cache)
    }
}
