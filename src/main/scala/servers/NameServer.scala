package servers

import repositories.NameRepository
import zio._

final case class NameServer(repo: NameRepository) {
  def getNames: Task[List[models.Name]] = repo.list()
}

object NameServer {
  val live: ZLayer[NameRepository, Nothing, NameServer] =
    ZLayer.fromFunction(NameServer.apply _)
}
