package servers

import models.StoredCharacterClass
import repositories.CharacterClassRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class CharacterClassServer(
  repo: CharacterClassRepository,
  cache: Cache[Unit, Throwable, List[StoredCharacterClass]],
) {
  def getClasses: Task[List[StoredCharacterClass]] = cache.get(())
}

object CharacterClassServer {
  val live: ZLayer[CharacterClassRepository, Throwable, CharacterClassServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[CharacterClassRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield CharacterClassServer(repo, cache)
    }
}
