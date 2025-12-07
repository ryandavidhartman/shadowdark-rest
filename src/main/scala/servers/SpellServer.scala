package servers

import models.Spell
import repositories.SpellRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class SpellServer(repo: SpellRepository, cache: Cache[Unit, Throwable, List[Spell]]) {
  def getSpells: Task[List[Spell]] = cache.get(())
}

object SpellServer {
  val live: ZLayer[SpellRepository, Throwable, SpellServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[SpellRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield SpellServer(repo, cache)
    }
}
