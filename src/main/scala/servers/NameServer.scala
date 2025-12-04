package servers

import models.Name
import repositories.NameRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class NameServer(repo: NameRepository, cache: Cache[Unit, Throwable, List[Name]]) {
  def getNames: Task[List[Name]] = cache.get(())

  def createName(name: Name): Task[Name] =
    for {
      saved <- repo.create(name)
      _     <- cache.invalidate(())
    } yield saved
}

object NameServer {
  val live: ZLayer[NameRepository, Throwable, NameServer] =
    ZLayer.fromZIO {
      for {
        repo  <- ZIO.service[NameRepository]
        cache <- Cache.make(
                   capacity = 1,
                   timeToLive = 5.minutes,
                   lookup = Lookup((_: Unit) => repo.list()),
                 )
      } yield NameServer(repo, cache)
    }
}
