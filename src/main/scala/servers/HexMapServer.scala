package servers

import java.awt.{BasicStroke, Color, Font, GradientPaint, Graphics2D, RenderingHints}
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom
import models.{HexCell, HexMap, HexMapLayout, HexPointOfInterest}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.PDPageContentStream
import zio.{Task, ZIO, ZLayer}

final case class HexMapServer() {
  private val rng: ThreadLocalRandom = ThreadLocalRandom.current()
  private val pageWidth = 1275
  private val pageHeight = 1650
  private val pageMargin = 60
  private val mapWidth = pageWidth - (pageMargin * 2)
  private val mapHeight = pageHeight - (pageMargin * 2)
  private val sqrt3 = Math.sqrt(3.0)
  private val terrainSteps = Vector(
    ("Desert", "Arctic"),
    ("Swamp", "Taiga"),
    ("Grassland", "Grassland"),
    ("Jungle", "Forest"),
    ("River/coast", "River/coast"),
    ("Ocean", "Ocean"),
    ("Mountain", "Mountain"),
  )
  private val poiLocations = Vector(
    "Small tower",
    "Fortified keep",
    "Natural landmark",
    "Natural landmark",
    "Temple",
    "Barrow mounds",
    "Village",
    "Village",
    "Town",
    "Town",
    "City/metropolis",
    "Ravine",
    "Monster nest",
    "Monster nest",
    "Hermit's abode",
    "Cave formation",
    "Cave formation",
    "Ancient dolmens",
    "Barbarian camp",
    "Holy shrine",
  )
  private val poiDevelopments = Vector(
    "Disaster! Roll on Cataclysm table",
    "Over/connected to a large tomb",
    "Being attacked by an invader",
    "Being attacked by an invader",
    "Home to an oracle",
    "Around/over a sleeping dragon",
    "Abandoned and in ruins",
    "Abandoned and in ruins",
    "Guarded by its current residents",
    "Guarded by its current residents",
    "Under siege by a warband",
    "Home to a religious cult",
    "Where a secret circle of wizards meets",
    "Where a secret circle of wizards meets",
    "Occupied by a self-titled king/queen",
    "Controlled by a malevolent sorcerer",
    "Controlled by a malevolent sorcerer",
    "Protected by an age-old guardian",
    "Hiding a great treasure",
    "With a door to another plane",
  )
  private val cataclysms = Vector(
    "Volcano",
    "Fire",
    "Earthquake",
    "Storm",
    "Flood",
    "War",
    "Pestilence",
    "Magical disaster",
  )

  def randomMap: Task[HexMap] =
    ZIO.attempt {
      val climate = climateForRoll(rollDie(2))
      val danger  = dangerForRoll(rollDie(6))
      val step    = terrainStepForRoll(roll2d6())
      val hex     = buildHex(0, 0, step, climate, 1, 1)
      HexMap(
        name = "Overland Hex Map",
        climate = climate,
        dangerLevel = danger,
        layout = HexMapLayout(columns = 1, rows = 1),
        hexes = List(hex),
      )
    }

  def nextMap(current: HexMap, fromColumn: Int, fromRow: Int, direction: String): Task[HexMap] =
    ZIO.attempt {
      val origin =
        current.hexes.find(h => h.column == fromColumn && h.row == fromRow)
          .getOrElse(throw new IllegalArgumentException("Origin hex not found in map"))
      val (targetCol, targetRow) = neighborCoords(fromColumn, fromRow, direction)
      val existing = current.hexes.find(h => h.column == targetCol && h.row == targetRow)
      existing match {
        case Some(_) => current.copy(layout = layoutFor(current.hexes))
        case None =>
          val step = nextTerrainStep(origin.terrainStep)
          val nextId = current.hexes.map(_.id).maxOption.getOrElse(0) + 1
          val nextPoiId = current.hexes.flatMap(_.pointOfInterest.map(_.id)).maxOption.getOrElse(0) + 1
          val nextHex = buildHex(targetCol, targetRow, step, current.climate, nextId, nextPoiId)
          val updatedHexes = current.hexes :+ nextHex
          current.copy(layout = layoutFor(updatedHexes), hexes = updatedHexes)
      }
    }

  def renderHexMapPdf(map: HexMap): Task[Array[Byte]] =
    ZIO.attempt {
      val image = renderHexMapImage(map)
      val pageSize = new PDRectangle(pageWidth.toFloat, pageHeight.toFloat)
      val document = new PDDocument()
      try {
        val page = new PDPage(pageSize)
        document.addPage(page)
        val xObject = LosslessFactory.createFromImage(document, image)
        val stream = new PDPageContentStream(document, page)
        try stream.drawImage(xObject, 0, 0, pageWidth, pageHeight)
        finally stream.close()
        val output = new ByteArrayOutputStream()
        try {
          document.save(output)
          output.toByteArray
        } finally output.close()
      } finally document.close()
    }

  private def rollDie(sides: Int): Int =
    rng.nextInt(1, sides + 1)

  private def roll2d6(): Int =
    rollDie(6) + rollDie(6)

  private def climateForRoll(roll: Int): String =
    if (roll == 1) "Warm" else "Cold"

  private def dangerForRoll(roll: Int): String =
    roll match {
      case 1 => "Safe"
      case 2 | 3 => "Unsafe"
      case 4 | 5 => "Risky"
      case _ => "Deadly"
    }

  private def terrainStepForRoll(roll: Int): Int =
    roll match {
      case 2 => 0
      case 3 => 1
      case 4 | 5 | 6 => 2
      case 7 | 8 => 3
      case 9 | 10 => 4
      case 11 => 5
      case _ => 6
    }

  private def nextTerrainStep(currentStep: Int): Int = {
    val roll = roll2d6()
    val stepCount = terrainSteps.size
    roll match {
      case 2 | 3 => (currentStep + 1) % stepCount
      case 4 | 5 | 6 | 7 | 8 => currentStep
      case 9 | 10 | 11 => (currentStep + 2) % stepCount
      case _ => terrainStepForRoll(roll2d6())
    }
  }

  private def terrainName(step: Int, climate: String): String = {
    val (warm, cold) = terrainSteps(step)
    if (climate == "Warm") warm else cold
  }

  private def buildHex(
    column: Int,
    row: Int,
    step: Int,
    climate: String,
    hexId: Int,
    nextPoiId: Int,
  ): HexCell = {
    val poi =
      if (rollDie(6) == 1) {
        val location = poiLocations(rollDie(20) - 1)
        val development = poiDevelopments(rollDie(20) - 1)
        val cataclysm =
          if (development.startsWith("Disaster!")) Some(cataclysms(rollDie(8) - 1)) else None
        Some(
          HexPointOfInterest(
            id = nextPoiId,
            location = location,
            development = development,
            cataclysm = cataclysm,
          ),
        )
      } else None
    HexCell(
      id = hexId,
      column = column,
      row = row,
      terrain = terrainName(step, climate),
      terrainStep = step,
      pointOfInterest = poi,
    )
  }

  private def layoutFor(hexes: List[HexCell]): HexMapLayout = {
    val columns = hexes.map(_.column)
    val rows    = hexes.map(_.row)
    val width   = columns.max - columns.min + 1
    val height  = rows.max - rows.min + 1
    HexMapLayout(columns = width, rows = height)
  }

  private def neighborCoords(column: Int, row: Int, direction: String): (Int, Int) = {
    val dir = direction.trim.toUpperCase
    val odd = row % 2 != 0
    (dir, odd) match {
      case ("N", _) => (column, row - 1)
      case ("S", _) => (column, row + 1)
      case ("NE", false) => (column + 1, row - 1)
      case ("SE", false) => (column + 1, row)
      case ("SW", false) => (column - 1, row)
      case ("NW", false) => (column - 1, row - 1)
      case ("NE", true) => (column + 1, row)
      case ("SE", true) => (column + 1, row + 1)
      case ("SW", true) => (column - 1, row + 1)
      case ("NW", true) => (column - 1, row)
      case _ => throw new IllegalArgumentException("Direction must be N, NE, SE, S, SW, or NW")
    }
  }

  private def renderHexMapImage(map: HexMap): BufferedImage = {
    val image = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBackground(g)
      val (minCol, maxCol, minRow, maxRow) = boundsFor(map.hexes)
      val columns = maxCol - minCol + 1
      val rows = maxRow - minRow + 1
      val size = hexSizeFor(columns, rows)
      val hexWidth = sqrt3 * size
      val mapPixelWidth = hexWidth * columns + hexWidth / 2.0
      val mapPixelHeight = (2.0 * size) + (rows - 1) * size * 1.5
      val startX = pageMargin + (mapWidth - mapPixelWidth) / 2.0
      val startY = pageMargin + (mapHeight - mapPixelHeight) / 2.0

      map.hexes.foreach { hex =>
        val colOffset = hex.column - minCol
        val rowOffset = hex.row - minRow
        val isOddRow = rowOffset % 2 != 0
        val centerX = startX + hexWidth * (colOffset + (if (isOddRow) 0.5 else 0.0)) + hexWidth / 2.0
        val centerY = startY + size * 1.5 * rowOffset + size
        val polygon = hexPolygon(centerX, centerY, size)
        g.setColor(terrainColor(hex.terrain))
        g.fill(polygon)
        drawTerrainPattern(g, polygon, hex.terrain, size)
        g.setColor(new Color(60, 55, 50))
        g.setStroke(new BasicStroke(2.0f))
        g.draw(polygon)
        drawHexIcon(g, hex, centerX, centerY, size)
      }

      drawHexMapLegend(g, map)
    } finally g.dispose()
    image
  }

  private def paintBackground(g: Graphics2D): Unit = {
    g.setPaint(new GradientPaint(0f, 0f, new Color(248, 243, 232), 0f, pageHeight.toFloat, new Color(233, 226, 214)))
    g.fillRect(0, 0, pageWidth, pageHeight)
  }

  private def boundsFor(hexes: List[HexCell]): (Int, Int, Int, Int) = {
    val cols = hexes.map(_.column)
    val rows = hexes.map(_.row)
    (cols.min, cols.max, rows.min, rows.max)
  }

  private def hexSizeFor(columns: Int, rows: Int): Double = {
    val sizeByWidth = mapWidth / (sqrt3 * (columns + 0.5))
    val sizeByHeight = mapHeight / (2.0 + (rows - 1) * 1.5)
    Math.max(24.0, Math.min(sizeByWidth, sizeByHeight))
  }

  private def hexPolygon(centerX: Double, centerY: Double, size: Double) = {
    val xs = new Array[Int](6)
    val ys = new Array[Int](6)
    var i = 0
    while (i < 6) {
      val angle = Math.toRadians(60.0 * i - 30.0)
      xs(i) = Math.round(centerX + size * Math.cos(angle)).toInt
      ys(i) = Math.round(centerY + size * Math.sin(angle)).toInt
      i += 1
    }
    new java.awt.Polygon(xs, ys, 6)
  }

  private def terrainColor(terrain: String): Color =
    terrain match {
      case "Desert" => new Color(222, 201, 140)
      case "Arctic" => new Color(210, 230, 238)
      case "Swamp" => new Color(116, 134, 92)
      case "Taiga" => new Color(150, 174, 130)
      case "Grassland" => new Color(164, 188, 115)
      case "Jungle" => new Color(88, 140, 84)
      case "Forest" => new Color(110, 160, 120)
      case "River/coast" => new Color(120, 170, 205)
      case "Ocean" => new Color(88, 130, 178)
      case "Mountain" => new Color(150, 150, 150)
      case _ => new Color(190, 190, 190)
    }

  private def drawHexIcon(g: Graphics2D, hex: HexCell, centerX: Double, centerY: Double, size: Double): Unit = {
    g.setColor(new Color(40, 35, 30))
    val iconSize = size * 0.6
    val x = centerX - iconSize / 2.0
    val y = centerY - iconSize / 2.0
    hex.terrain match {
      case "Mountain" =>
        val peak = new java.awt.Polygon(
          Array(centerX.toInt, (x + iconSize * 0.2).toInt, (x + iconSize * 0.8).toInt),
          Array((y + iconSize * 0.1).toInt, (y + iconSize * 0.9).toInt, (y + iconSize * 0.9).toInt),
          3,
        )
        g.setStroke(new BasicStroke(2.4f))
        g.draw(peak)
      case "Ocean" =>
        drawWaveIcon(g, x, y, iconSize)
      case "River/coast" =>
        drawWaveIcon(g, x, y, iconSize * 0.8)
      case "Desert" | "Arctic" =>
        g.setStroke(new BasicStroke(2.0f))
        g.drawArc((x + iconSize * 0.1).toInt, (y + iconSize * 0.4).toInt, (iconSize * 0.6).toInt, (iconSize * 0.4).toInt, 0, 180)
        g.drawArc((x + iconSize * 0.3).toInt, (y + iconSize * 0.2).toInt, (iconSize * 0.6).toInt, (iconSize * 0.4).toInt, 0, 180)
      case "Swamp" | "Taiga" =>
        g.setStroke(new BasicStroke(2.0f))
        val baseX = centerX - iconSize * 0.2
        g.drawLine(baseX.toInt, (y + iconSize * 0.8).toInt, baseX.toInt, (y + iconSize * 0.3).toInt)
        g.drawLine((baseX + iconSize * 0.2).toInt, (y + iconSize * 0.8).toInt, (baseX + iconSize * 0.2).toInt, (y + iconSize * 0.25).toInt)
        g.drawLine((baseX + iconSize * 0.4).toInt, (y + iconSize * 0.8).toInt, (baseX + iconSize * 0.4).toInt, (y + iconSize * 0.35).toInt)
      case "Grassland" =>
        g.setStroke(new BasicStroke(2.0f))
        g.drawLine((x + iconSize * 0.2).toInt, (y + iconSize * 0.8).toInt, (x + iconSize * 0.35).toInt, (y + iconSize * 0.4).toInt)
        g.drawLine((x + iconSize * 0.5).toInt, (y + iconSize * 0.85).toInt, (x + iconSize * 0.55).toInt, (y + iconSize * 0.35).toInt)
        g.drawLine((x + iconSize * 0.75).toInt, (y + iconSize * 0.8).toInt, (x + iconSize * 0.7).toInt, (y + iconSize * 0.3).toInt)
      case "Jungle" | "Forest" =>
        val canopy = new java.awt.geom.Ellipse2D.Double(x + iconSize * 0.15, y + iconSize * 0.2, iconSize * 0.7, iconSize * 0.5)
        g.setStroke(new BasicStroke(2.0f))
        g.draw(canopy)
        g.drawLine(centerX.toInt, (y + iconSize * 0.7).toInt, centerX.toInt, (y + iconSize * 0.9).toInt)
      case _ =>
        ()
    }

    hex.pointOfInterest.foreach { _ =>
      val poiSize = Math.max(10, (size * 0.22).toInt)
      g.setColor(new Color(150, 30, 30))
      g.fillOval((centerX - poiSize / 2.0).toInt, (centerY - size * 0.62).toInt, poiSize, poiSize)
      g.setColor(Color.WHITE)
      g.setFont(new Font("Serif", Font.BOLD, Math.max(10, poiSize - 2)))
      g.drawString("!", (centerX - 3).toFloat, (centerY - size * 0.62 + poiSize - 2).toFloat)
    }
  }

  private def drawTerrainPattern(g: Graphics2D, polygon: java.awt.Polygon, terrain: String, size: Double): Unit = {
    val bounds = polygon.getBounds
    val oldClip = g.getClip
    g.setClip(polygon)
    terrain match {
      case "Desert" =>
        g.setColor(new Color(150, 120, 70, 110))
        val step = Math.max(8, (size * 0.35).toInt)
        var x = bounds.x
        while (x < bounds.x + bounds.width) {
          var y = bounds.y
          while (y < bounds.y + bounds.height) {
            g.fillOval(x, y, 2, 2)
            y += step
          }
          x += step
        }
      case "Arctic" =>
        g.setColor(new Color(160, 180, 190, 110))
        g.setStroke(new BasicStroke(1.2f))
        val step = Math.max(10, (size * 0.45).toInt)
        var x = bounds.x - bounds.height
        while (x < bounds.x + bounds.width + bounds.height) {
          g.drawLine(x, bounds.y + bounds.height, x + bounds.height, bounds.y)
          x += step
        }
      case "Swamp" | "Taiga" =>
        g.setColor(new Color(80, 100, 70, 110))
        g.setStroke(new BasicStroke(1.3f))
        val step = Math.max(10, (size * 0.4).toInt)
        var x = bounds.x + 4
        while (x < bounds.x + bounds.width) {
          g.drawLine(x, bounds.y + 4, x, bounds.y + bounds.height - 4)
          x += step
        }
      case "Grassland" =>
        g.setColor(new Color(110, 130, 80, 110))
        g.setStroke(new BasicStroke(1.4f))
        val step = Math.max(9, (size * 0.35).toInt)
        var y = bounds.y + 6
        while (y < bounds.y + bounds.height) {
          g.drawLine(bounds.x + 6, y, bounds.x + bounds.width - 6, y)
          y += step
        }
      case "Jungle" | "Forest" =>
        g.setColor(new Color(70, 100, 70, 110))
        val step = Math.max(10, (size * 0.35).toInt)
        var x = bounds.x
        while (x < bounds.x + bounds.width) {
          var y = bounds.y
          while (y < bounds.y + bounds.height) {
            g.fillOval(x, y, 4, 4)
            y += step
          }
          x += step
        }
      case "River/coast" | "Ocean" =>
        g.setColor(new Color(70, 110, 150, 120))
        g.setStroke(new BasicStroke(1.4f))
        val step = Math.max(10, (size * 0.4).toInt)
        var y = bounds.y + 6
        while (y < bounds.y + bounds.height) {
          g.drawArc(bounds.x + 6, y, bounds.width - 12, step, 0, 180)
          y += step
        }
      case "Mountain" =>
        g.setColor(new Color(120, 120, 120, 140))
        g.setStroke(new BasicStroke(1.4f))
        val step = Math.max(10, (size * 0.4).toInt)
        var x = bounds.x + 4
        while (x < bounds.x + bounds.width) {
          g.drawLine(x, bounds.y + bounds.height - 6, x + step / 2, bounds.y + 6)
          x += step
        }
      case _ =>
        ()
    }
    g.setClip(oldClip)
  }

  private def drawWaveIcon(g: Graphics2D, x: Double, y: Double, size: Double): Unit = {
    g.setStroke(new BasicStroke(2.2f))
    val top = y + size * 0.35
    g.drawArc(x.toInt, top.toInt, (size * 0.9).toInt, (size * 0.35).toInt, 0, 180)
    g.drawArc((x + size * 0.1).toInt, (top + size * 0.2).toInt, (size * 0.8).toInt, (size * 0.35).toInt, 0, 180)
  }

  private def drawHexMapLegend(g: Graphics2D, map: HexMap): Unit = {
    val x = pageMargin
    val y = pageMargin
    val terrainNames = terrainNamesForClimate(map.climate)
    val rowHeight = 26
    val w = 320
    val h = 120 + terrainNames.size * rowHeight
    g.setColor(new Color(255, 255, 255, 220))
    g.fillRoundRect(x, y, w, h, 18, 18)
    g.setColor(new Color(70, 60, 50))
    g.setStroke(new BasicStroke(1.6f))
    g.drawRoundRect(x, y, w, h, 18, 18)
    g.setFont(new Font("Serif", Font.BOLD, 18))
    g.drawString(map.name, x + 16, y + 28)
    g.setFont(new Font("Serif", Font.PLAIN, 14))
    g.drawString(s"Climate: ${map.climate}", x + 16, y + 54)
    g.drawString(s"Danger: ${map.dangerLevel}", x + 16, y + 74)
    g.drawString("POI: red marker", x + 16, y + 94)
    val legendStartY = y + 120
    terrainNames.zipWithIndex.foreach { case (terrain, idx) =>
      val rowY = legendStartY + idx * rowHeight
      val hexCenterX = x + 28
      val hexCenterY = rowY + 8
      val polygon = hexPolygon(hexCenterX, hexCenterY, 10)
      g.setColor(terrainColor(terrain))
      g.fill(polygon)
      drawTerrainPattern(g, polygon, terrain, 10)
      g.setColor(new Color(60, 55, 50))
      g.draw(polygon)
      drawHexIcon(g, HexCell(0, 0, 0, terrain, 0, None), hexCenterX, hexCenterY, 10)
      g.setColor(new Color(40, 35, 30))
      g.setFont(new Font("Serif", Font.PLAIN, 13))
      g.drawString(terrain, x + 50, rowY + 12)
    }
  }

  private def terrainNamesForClimate(climate: String): List[String] = {
    val names = terrainSteps.map { case (warm, cold) => if (climate == "Warm") warm else cold }
    names.toList
  }
}

object HexMapServer {
  val live: ZLayer[Any, Nothing, HexMapServer] =
    ZLayer.succeed(HexMapServer())
}
