package routes

import models.{AbilityScore, Character}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.cos.COSName
import servers.CharacterServer
import zio._
import zio.http._
import zio.json._

import java.io.{ByteArrayOutputStream, InputStream}
import scala.jdk.CollectionConverters._

final case class CharacterRoute(server: CharacterServer) {
  val routes: Routes[Any, Response] =
    Routes(
      Method.GET / "random-character" ->
        Handler
          .fromFunctionZIO[Request] { req =>
            server
              .randomCharacter(zeroLevel = isZeroLevel(req))
              .map(character => Response.json(character.toJson))
          }
          .mapError(err => Response.internalServerError(err.getMessage)),
      Method.GET / "random-character.pdf" ->
        Handler
          .fromFunctionZIO[Request] { req =>
            for {
              character <- server.randomCharacter(zeroLevel = isZeroLevel(req))
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

  private def isZeroLevel(request: Request): Boolean =
    request.url.queryParams
      .getAll("zeroLevel")
      .headOption
      .exists(value => value.equalsIgnoreCase("true") || value == "1")

  private def renderCharacterPdf(character: Character): Task[Array[Byte]] =
    ZIO.attempt {
      val templateStream: InputStream =
        Option(getClass.getResourceAsStream(templatePath))
          .getOrElse(throw new IllegalStateException(s"Missing template at $templatePath"))

      val randomAccess = new RandomAccessReadBuffer(templateStream)
      val document     = Loader.loadPDF(randomAccess)
      try {
        val form =
          Option(document.getDocumentCatalog.getAcroForm)
            .getOrElse(throw new IllegalStateException("Template does not contain an AcroForm"))
        val defaultResources = Option(form.getDefaultResources).getOrElse(new PDResources())
        val defaultFontName  = COSName.getPDFName("Helv")
        if (defaultResources.getFont(defaultFontName) == null) {
          defaultResources.put(defaultFontName, new PDType1Font(Standard14Fonts.FontName.HELVETICA))
        }
        form.setDefaultResources(defaultResources)
        form.setDefaultAppearance("/Helv 10 Tf 0 g")
        form.setNeedAppearances(true)
        adjustGearTenField(form)
        setGearFieldFont(form, 9)

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

        val gearLines        = character.gear.map(stripGearSlotLabel)
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
        randomAccess.close()
        templateStream.close()
      }
    }

  private def formatMod(mod: Int): String =
    if (mod >= 0) s"+$mod" else s"$mod"

  private def stripGearSlotLabel(value: String): String =
    value.replaceFirst("^Slot\\s+\\d+:\\s*", "")

  private def adjustGearTenField(form: PDAcroForm): Unit =
    Option(form.getField("Gear 10")).foreach { field =>
      field.getWidgets.asScala.foreach { widget =>
        val rect = widget.getRectangle
        if (rect != null) {
          val shifted = new PDRectangle(rect.getLowerLeftX, rect.getLowerLeftY, rect.getWidth, rect.getHeight)
          val shift   = 3.2f
          shifted.setLowerLeftX(rect.getLowerLeftX - shift)
          shifted.setUpperRightX(rect.getUpperRightX - shift)
          widget.setRectangle(shifted)
        }
      }
    }

  private def setGearFieldFont(form: PDAcroForm, size: Int): Unit = {
    val appearance = s"/Helv $size Tf 0 g"
    val gearFields = (1 to 20).map(i => s"Gear $i") :+ "Free To Carry"
    gearFields.foreach { name =>
      Option(form.getField(name)).foreach {
        case vt: PDVariableText => vt.setDefaultAppearance(appearance)
        case _                  => ()
      }
    }
  }
}

object CharacterRoute {
  val live: ZLayer[CharacterServer, Nothing, CharacterRoute] =
    ZLayer.fromFunction(CharacterRoute.apply _)
}
