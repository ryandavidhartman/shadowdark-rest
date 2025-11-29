package servers

import repositories.RaceRepository
import zio._

final case class RaceServer(repo: RaceRepository) {
  def getRaces: Task[List[models.Race]] = repo.list()
}

object RaceServer {
  val live: ZLayer[RaceRepository, Nothing, RaceServer] =
    ZLayer.fromFunction(RaceServer.apply _)
}
