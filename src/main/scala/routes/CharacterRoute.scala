package routes

import models.{AbilityScore, Character}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import servers.CharacterServer
import zio._
import zio.http._
import zio.json._

import java.io.{ByteArrayOutputStream, InputStream}

final case class CharacterRoute(server: CharacterServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "random-character" ->
        Handler
          .fromZIO(server.randomCharacter.map(character => Response.json(character.toJson)))
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.GET / "random-character.pdf" ->
        Handler
          .fromZIO {
            for {
              character <- server.randomCharacter
              pdf       <- renderCharacterPdf(character)
            } yield Response(
              status = Status.Ok,
              headers = Headers(
                Header.Custom("Content-Type", "application/pdf"),
                Header.Custom("Content-Disposition", "inline; filename=\"random-character.pdf\""),
              ),
              body = Body.fromChunk(Chunk.fromArray(pdf)),
            )
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
    )

  private val templatePath = "/ShadowdarkSheet.pdf"

  private def renderCharacterPdf(character: Character): Task[Array[Byte]] =
    ZIO.attempt {
      val templateStream: InputStream =
        Option(getClass.getResourceAsStream(templatePath))
          .getOrElse(throw new IllegalStateException(s"Missing template at $templatePath"))

      val document = PDDocument.load(templateStream)
      try {
        val form =
          Option(document.getDocumentCatalog.getAcroForm)
            .getOrElse(throw new IllegalStateException("Template does not contain an AcroForm"))
        form.setNeedAppearances(true)

        def field(name: String): PDField =
          Option(form.getField(name)).getOrElse(throw new IllegalArgumentException(s"Missing field '$name' in PDF"))

        def setField(name: String, value: String): Unit =
          field(name).setValue(value)

        def abilityFields(label: String, ability: AbilityScore): Unit = {
          setField(s"$label Total", ability.score.toString)
          setField(s"$label Modifier", formatMod(ability.modifier))
        }

        setField("Name", character.name)
        setField("Race", character.ancestry.getOrElse(""))
        setField("Class", character.characterClass.getOrElse(""))
        setField("Level", character.level.map(_.toString).getOrElse(""))
        setField("XP Current", character.xp.map(_.toString).getOrElse("0"))
        setField("XP Target", "")
        setField("Title", character.title.getOrElse(""))
        setField("Alignment", character.alignment.getOrElse(""))
        setField("Background", character.background.getOrElse(""))
        setField("Deity", character.deity.getOrElse(""))

        val abilities = character.abilities
        abilityFields("Strength", abilities.strength)
        abilityFields("Dexterity", abilities.dexterity)
        abilityFields("Constitution", abilities.constitution)
        abilityFields("Intelligence", abilities.intelligence)
        abilityFields("Wisdom", abilities.wisdom)
        abilityFields("Charisma", abilities.charisma)

        setField("Hit Points", character.hitPoints.toString)
        setField("Armor Class", character.armorClass.toString)

        val attacksText =
          if (character.attacks.nonEmpty) character.attacks.mkString("\n") else "None recorded"
        setField("Attacks", attacksText)

        val talentsAndSpells = {
          val features = character.features.map(f => s"${f.name}: ${f.description}")
          val talents  = character.talents.map(t => s"Talent: $t")
          val spells   = character.spells.map(s => s"Spell: $s")
          (features ++ talents ++ spells) match {
            case Nil => "None recorded"
            case xs  => xs.mkString("\n")
          }
        }
        setField("Talents / Spells", talentsAndSpells)

        setField("Gold Pieces", character.goldPieces.toString)
        setField("Silver Pieces", character.silverPieces.toString)
        setField("Copper Pieces", character.copperPieces.toString)

        val gearLines        = character.gear
        val gearFieldCount   = 20
        val (primaryGear, extraGear) = gearLines.splitAt(gearFieldCount)
        primaryGear.zipWithIndex.foreach { case (item, idx) =>
          setField(s"Gear ${idx + 1}", item)
        }

        val freeToCarry = (extraGear ++ character.freeToCarry) match {
          case Nil => "None recorded"
          case xs  => xs.mkString("\n")
        }
        setField("Free To Carry", freeToCarry)

        form.refreshAppearances()
        form.flatten()

        val output = new ByteArrayOutputStream()
        try {
          document.save(output)
          output.toByteArray
        } finally output.close()
      } finally {
        document.close()
        templateStream.close()
      }
    }

  private def formatMod(mod: Int): String =
    if (mod >= 0) s"+$mod" else s"$mod"
}

object CharacterRoute {
  val live: ZLayer[CharacterServer, Nothing, CharacterRoute] =
    ZLayer.fromFunction(CharacterRoute.apply _)
}
