package servers

import models.{AbilityScore, AbilityScores, Character, Name, Race}
import org.mongodb.scala.bson.ObjectId
import repositories.{CharacterRepository, NameRepository, RaceRepository}
import zio._

import java.util.concurrent.ThreadLocalRandom
import scala.util.Random

final case class CharacterServer(
  characterRepository: CharacterRepository,
  nameRepository: NameRepository,
  raceRepository: RaceRepository,
) {

  private val classes     = List("Cleric", "Fighter", "Rogue", "Wizard", "Ranger", "Champion")
  private val backgrounds = List("Acolyte", "Commoner", "Outlander", "Scholar", "Soldier")
  private val alignments  = List("Lawful", "Neutral", "Chaotic")
  private val deities     = List("Arden", "Gloom", "Ithis", "Lunara", "Shadow")
  private val gearChoices = List(
    "Backpack",
    "Bedroll",
    "Rations (3 days)",
    "Rope (50 ft)",
    "Torch (5)",
    "Waterskin",
    "Dagger",
    "Short sword",
    "Sling (10 bullets)",
    "Lantern",
  )

  private def rng: ThreadLocalRandom = ThreadLocalRandom.current()

  private def roll3d6: UIO[Int] =
    ZIO.succeed(rng.nextInt(1, 7) + rng.nextInt(1, 7) + rng.nextInt(1, 7))

  private def pickOne[A](values: List[A]): UIO[Option[A]] =
    ZIO.succeed {
      if (values.isEmpty) None
      else Some(values(rng.nextInt(values.size)))
    }

  private def pickSome[A](values: List[A], count: Int): UIO[List[A]] =
    ZIO.succeed {
      if (values.isEmpty) List.empty
      else Random.shuffle(values).take(count)
    }

  private def pickWeightedRace(races: List[Race]): UIO[Option[Race]] =
    ZIO.succeed {
      val totalWeight = races.map(_.chance).sum
      if (races.isEmpty || totalWeight <= 0) None
      else {
        val roll = rng.nextDouble() * totalWeight
        races
          .scanLeft((0.0, Option.empty[Race])) { case ((acc, _), race) =>
            (acc + race.chance, Some(race))
          }
          .collectFirst { case (acc, maybeRace) if roll <= acc => maybeRace }
          .flatten
      }
    }

  private def formatMod(mod: Int): String =
    if (mod >= 0) s"+$mod" else s"$mod"

  private def matchesRace(name: Name, race: Option[String]): Boolean =
    race.forall { r =>
      val target = r.toLowerCase
      val nameR  = name.race.toLowerCase
      target match {
        case "half-orc" => nameR == "orc" || nameR == "human" || nameR == "half-orc"
        case "half-elf" => nameR == "elf" || nameR == "human" || nameR == "half-elf"
        case other      => nameR == other
      }
    }

  private def matchesGender(name: Name, gender: Option[String]): Boolean =
    gender.forall(g => name.gender.exists(_.equalsIgnoreCase(g)))

  private def randomFullName(names: List[Name], race: Option[String], gender: Option[String]): UIO[String] = {
    val firstNames =
      names.filter(n => n.firstName.contains(true) && matchesRace(n, race) && matchesGender(n, gender))
    val lastNames =
      names.filter(n => n.lastName.contains(true) && matchesRace(n, race))

    for {
      first <- pickOne(firstNames)
      last  <- pickOne(lastNames)
    } yield (first.map(_.name), last.map(_.name)) match {
      case (Some(f), Some(l)) => s"$f $l"
      case (Some(f), None)    => f
      case (None, Some(l))    => l
      case _                  => "Nameless Wanderer"
    }
  }

  private def generateCharacter: Task[Character] =
    for {
      // fall back to defaults if name/race collections are empty; still fail on repo errors
      names         <- nameRepository.list()
      races         <- raceRepository.list()
      randomRace    <- pickWeightedRace(races).flatMap {
                         case some @ Some(_) => ZIO.succeed(some)
                         case None           => pickOne(races)
                       }
      randomGender  <- pickOne(List("male", "female"))
      randomName    <- randomFullName(names, randomRace.map(_.race), randomGender)

      strScore      <- roll3d6
      dexScore      <- roll3d6
      conScore      <- roll3d6
      intScore      <- roll3d6
      wisScore      <- roll3d6
      chaScore      <- roll3d6
      pickedClass   <- pickOne(classes)
      pickedAlign   <- pickOne(alignments)
      pickedBg      <- pickOne(backgrounds)
      pickedDeity   <- pickOne(deities)
      gearPicked    <- pickSome(gearChoices, 3)
      gold          <- ZIO.succeed(rng.nextInt(0, 21))
      silver        <- ZIO.succeed(rng.nextInt(0, 51))
      copper        <- ZIO.succeed(rng.nextInt(0, 101))
      abilities      = AbilityScores(
                         AbilityScore(strScore),
                         AbilityScore(dexScore),
                         AbilityScore(conScore),
                         AbilityScore(intScore),
                         AbilityScore(wisScore),
                         AbilityScore(chaScore),
                       )
      conMod         = abilities.constitution.modifier
      hpRoll         = rng.nextInt(1, 9)
      hitPoints      = Math.max(1, hpRoll + conMod)
      armorClass     = 10 + abilities.dexterity.modifier
      attacks        = List(s"Weapon attack ${formatMod(abilities.strength.modifier)} (1d6)")
    } yield Character(
      _id = new ObjectId(),
      name = randomName,
      ancestry = randomRace.map(_.race),
      characterClass = pickedClass,
      level = Some(1),
      xp = Some(0),
      title = None,
      alignment = pickedAlign,
      background = pickedBg,
      deity = pickedDeity,
      abilities = abilities,
      hitPoints = hitPoints,
      armorClass = armorClass,
      talentsOrSpells = List.empty,
      attacks = attacks,
      gear = gearPicked,
      gender = randomGender,
      goldPieces = gold,
      silverPieces = silver,
      copperPieces = copper,
      freeToCarry = Some(abilities.strength.score * 10),
    )

  def randomCharacter: Task[Character] =
    for {
      stored <- characterRepository.list()
      pick   <- pickOne(stored)
      result <- pick.map(c => ZIO.succeed(c)).getOrElse(generateCharacter)
    } yield result
}

object CharacterServer {
  val live: ZLayer[CharacterRepository with NameRepository with RaceRepository, Nothing, CharacterServer] =
    ZLayer.fromFunction(CharacterServer.apply _)
}
