package repositories

import models.Name
import org.mongodb.scala.bson.ObjectId
import zio.{Ref, Task, ZIO}
import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

object NameRepositorySpec extends ZIOSpecDefault {
  final case class InMemoryNameRepository(store: Ref[Map[ObjectId, Name]]) extends NameRepository {
    def create(name: Name): Task[Name] =
      store.updateAndGet(_ + (name._id -> name)).as(name)

    def findById(id: ObjectId): Task[Option[Name]] =
      store.get.map(_.get(id))

    def list(): Task[List[Name]] =
      store.get.map(_.values.toList)

    def update(name: Name): Task[Boolean] =
      store.modify { data =>
        if (data.contains(name._id)) (true, data.updated(name._id, name))
        else (false, data)
      }

    def delete(id: ObjectId): Task[Boolean] =
      store.modify { data =>
        if (data.contains(id)) (true, data - id)
        else (false, data)
      }
  }

  private def repo: ZIO[Any, Nothing, InMemoryNameRepository] =
    Ref.make(Map.empty[ObjectId, Name]).map(InMemoryNameRepository.apply)

  override def spec =
    suite("NameRepository")(
      test("create/find/list/update/delete round-trip") {
        val id = new ObjectId()
        val name = Name(id, "Ava", "Human", firstName = Some(true))
        val updated = name.copy(name = "Ava the Bold")
        for {
          repo <- repo
          _ <- repo.create(name)
          found <- repo.findById(id)
          listed <- repo.list()
          updatedResult <- repo.update(updated)
          foundUpdated <- repo.findById(id)
          deletedResult <- repo.delete(id)
          missing <- repo.findById(id)
        } yield assertTrue(
          found.contains(name),
          listed.contains(name),
          updatedResult,
          foundUpdated.contains(updated),
          deletedResult,
          missing.isEmpty
        )
      }
    )
}
