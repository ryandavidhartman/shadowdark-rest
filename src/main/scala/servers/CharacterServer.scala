package servers

import models._
import org.mongodb.scala.bson.ObjectId
import repositories.{CharacterClassRepository, CharacterRepository}
import zio._

import java.util.concurrent.ThreadLocalRandom
import scala.util.Random

final case class CharacterServer(
  characterRepository: CharacterRepository,
  nameServer: NameServer,
  raceServer: RaceServer,
  personalityServer: PersonalityServer,
  characterClassRepository: CharacterClassRepository,
) {

  private val backgrounds = List("Acolyte", "Commoner", "Outlander", "Scholar", "Soldier")
  private val alignments  = List("Lawful", "Neutral", "Chaotic")
  private val deities     = List("Arden", "Gloom", "Ithis", "Lunara", "Shadow")
  private val baseLanguages = Language.default
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

  private def roll2d6: UIO[Int] =
    ZIO.succeed(rng.nextInt(1, 7) + rng.nextInt(1, 7))

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

  private def pickDistinctFromPool(pool: List[String], count: Int, exclude: Set[String]): List[String] =
    Random.shuffle(pool.filterNot(exclude)).distinct.take(count)

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

  private def ensureClasses: Task[List[StoredCharacterClass]] =
    characterClassRepository.list()

  private def hitDieSides(characterClass: StoredCharacterClass): Int =
    characterClass.hitPointsPerLevel match {
      case hp if hp.startsWith("1d8") => 8
      case hp if hp.startsWith("1d6") => 6
      case hp if hp.startsWith("1d4") => 4
      case _                          => 8
    }

  private def pickDistinct[A](pool: List[A], count: Int): List[A] =
    Random.shuffle(pool).distinct.take(count)

  private val abilityNames = List("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma")

  private def normalizedAbilityPriority(pickedClass: Option[StoredCharacterClass]): List[String] = {
    val prioritized = pickedClass
      .map(_.abilityPriority.flatMap(a => abilityNames.find(_.equalsIgnoreCase(a))).distinct)
      .getOrElse(List.empty)

    val missing = abilityNames.filterNot(prioritized.contains)
    val merged  = prioritized ++ missing

    if (merged.nonEmpty) merged else abilityNames
  }

  private def orderedAbilityScores(rolled: List[Int], pickedClass: Option[StoredCharacterClass]): AbilityScores = {
    val priority    = normalizedAbilityPriority(pickedClass)
    val sortedRolls = rolled.sorted(Ordering[Int].reverse)
    val assigned    = priority.zip(sortedRolls).toMap

    AbilityScores(
      AbilityScore(assigned("strength")),
      AbilityScore(assigned("dexterity")),
      AbilityScore(assigned("constitution")),
      AbilityScore(assigned("intelligence")),
      AbilityScore(assigned("wisdom")),
      AbilityScore(assigned("charisma")),
    )
  }

  private def generateCharacter: Task[Character] =
    for {
      // fall back to defaults if name/race collections are empty; still fail on repo errors
      names         <- nameServer.getNames
      races         <- raceServer.getRaces
      classes       <- ensureClasses
      personalities <- personalityServer.getPersonalities
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
      languages       = {
                          val extraCommonPattern = "(?i)(\\d+) extra common language[s]?".r
                          val extraRarePattern   = "(?i)(\\d+) extra rare language[s]?".r

                          val racialRaw       = randomRace.map(_.languages).getOrElse(List.empty)
                          var extraCommonPick = 0
                          var extraRarePick   = 0

                          val racialLangs = racialRaw.flatMap {
                            case extraCommonPattern(n) =>
                              extraCommonPick += n.toInt
                              None
                            case extraRarePattern(n) =>
                              extraRarePick += n.toInt
                              None
                            case other => Some(other)
                          }

                          val classLangs = pickedClass
                            .flatMap(_.languages)
                            .map { lb =>
                              val chosen = pickDistinct(lb.choices, lb.choose)
                              extraCommonPick += lb.extraCommon
                              extraRarePick += lb.extraRare
                              chosen
                            }
                            .getOrElse(List.empty)

                          val known       = (List("Common") ++ racialLangs ++ classLangs).toSet
                          val extraCommon = pickDistinctFromPool(baseLanguages.common, extraCommonPick, known)
                          val knownPlus   = known ++ extraCommon
                          val extraRare   = pickDistinctFromPool(baseLanguages.rare, extraRarePick, knownPlus)

                          (known ++ extraCommon ++ extraRare).toSet
       }
      abilities      = orderedAbilityScores(
                         List(strScore, dexScore, conScore, intScore, wisScore, chaScore),
                         pickedClass,
                       )
      allowedPersonalityAlignments = pickedAlign
        .map(_.toLowerCase match {
          case "lawful"  => Set("lawful", "neutral")
          case "neutral" => Set("neutral")
          case "chaotic" => Set("chaotic", "neutral")
          case other      => Set(other.toLowerCase)
        })
        .getOrElse(Set("lawful", "neutral", "chaotic"))
      pickedPersonalities <- {
                               val eligible = personalities.filter(p => allowedPersonalityAlignments.contains(p.alignment.toLowerCase))
                               val pool     = if (eligible.nonEmpty) eligible else personalities
                               pickSome(pool.map(_.name), 3)
                             }
      conMod         = abilities.constitution.modifier
      hpSides        = pickedClass.map(hitDieSides).getOrElse(8)
      hpRoll         = rng.nextInt(1, hpSides + 1)
      hitPoints      = Math.max(1, hpRoll + conMod)
      armorClass     = 10 + abilities.dexterity.modifier
      attacks        = List(s"Weapon attack ${formatMod(abilities.strength.modifier)} (1d6)")
      level          = 1
      talentRolls    = (level + 1) / 2
      classTalents   <- pickedClass
                          .map { cls =>
                            ZIO
                              .foreach(List.fill(talentRolls)(())) { _ =>
                                roll2d6.map(roll => cls.talents.find(t => roll >= t.range.min && roll <= t.range.max))
                              }
                              .map(_.flatten.map(_.effect))
                          }
                          .getOrElse(ZIO.succeed(List.empty[String]))
      classFeatures   = pickedClass.map(_.features).getOrElse(List.empty)
      raceFeatures     = randomRace.map(r => ClassFeature(r.ability.name, r.ability.description)).toList
      classSpells     = pickedClass
                          .flatMap(_.spellcasting)
                          .map { sc =>
                            val known = sc.initialKnown.toList.sortBy(_._1).map { case (tier, count) =>
                              s"Tier $tier spells known: $count"
                            }
                            known ++ sc.notes
                          }
                          .getOrElse(List.empty)
    } yield Character(
      _id = new ObjectId(),
      name = randomName,
      ancestry = randomRace.map(_.race),
      characterClass = pickedClass.map(_.name),
      level = Some(level),
      xp = Some(0),
      title = None,
      alignment = pickedAlign,
      background = pickedBg,
      deity = pickedDeity,
      abilities = abilities,
      hitPoints = hitPoints,
      armorClass = armorClass,
      features = raceFeatures ++ classFeatures,
      talents = classTalents,
      spells = classSpells,
      attacks = attacks,
      gear = gearPicked,
      languages = languages,
      personalities = pickedPersonalities,
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
  val live: ZLayer[
    CharacterRepository with NameServer with RaceServer with PersonalityServer with CharacterClassRepository,
    Nothing,
    CharacterServer,
  ] =
    ZLayer.fromFunction(CharacterServer.apply _)
}
