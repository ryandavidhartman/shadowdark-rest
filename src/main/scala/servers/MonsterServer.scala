package servers

import models.Monster
import repositories.MonsterRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class MonsterServer(repo: MonsterRepository, cache: Cache[Unit, Throwable, List[Monster]]) {
  def getMonsters: Task[List[Monster]] = cache.get(())
}

object MonsterServer {
  val live: ZLayer[MonsterRepository, Throwable, MonsterServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[MonsterRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield MonsterServer(repo, cache)
    }
}
