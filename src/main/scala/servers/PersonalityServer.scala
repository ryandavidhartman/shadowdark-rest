package servers

import models.Personality
import repositories.PersonalityRepository
import zio._
import zio.cache.{Cache, Lookup}
import zio.durationInt

final case class PersonalityServer(repo: PersonalityRepository, cache: Cache[Unit, Throwable, List[Personality]]) {
  def getPersonalities: Task[List[Personality]] = cache.get(())

  def createPersonality(personality: Personality): Task[Personality] =
    for {
      saved <- repo.create(personality)
      _ <- cache.invalidate(())
    } yield saved
}

object PersonalityServer {
  val live: ZLayer[PersonalityRepository, Throwable, PersonalityServer] =
    ZLayer.fromZIO {
      for {
        repo <- ZIO.service[PersonalityRepository]
        cache <- Cache.make(
          capacity = 1,
          timeToLive = 5.minutes,
          lookup = Lookup((_: Unit) => repo.list())
        )
      } yield PersonalityServer(repo, cache)
    }
}
