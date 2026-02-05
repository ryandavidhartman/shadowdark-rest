package servers

import models.Item
import repositories.ItemRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class ItemServer(repo: ItemRepository, cache: Cache[Unit, Throwable, List[Item]]) {
  def getItems: Task[List[Item]] = cache.get(())
}

object ItemServer {
  val live: ZLayer[ItemRepository, Throwable, ItemServer] =
    ZLayer.fromZIO {
      for {
        repo <- ZIO.service[ItemRepository]
        cache <- Cache.make(
          capacity = 1,
          timeToLive = 5.minutes,
          lookup = Lookup((_: Unit) => repo.list())
        )
      } yield ItemServer(repo, cache)
    }
}
