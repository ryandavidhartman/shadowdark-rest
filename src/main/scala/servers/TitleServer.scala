package servers

import models.Title
import repositories.TitleRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class TitleServer(repo: TitleRepository, cache: Cache[Unit, Throwable, List[Title]]) {
  def getTitles: Task[List[Title]] = cache.get(())
}

object TitleServer {
  val live: ZLayer[TitleRepository, Throwable, TitleServer] =
    ZLayer.fromZIO {
      for {
        repo <- ZIO.service[TitleRepository]
        cache <- Cache.make(
          capacity = 1,
          timeToLive = 5.minutes,
          lookup = Lookup((_: Unit) => repo.list())
        )
      } yield TitleServer(repo, cache)
    }
}
