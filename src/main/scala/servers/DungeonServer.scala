package servers

import java.awt.{BasicStroke, Color, Font, GradientPaint, Graphics2D, RenderingHints}
import java.awt.geom.{Path2D, Rectangle2D}
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom
import java.util.Random
import javax.imageio.ImageIO
import models.{Dungeon, DungeonCorridor, DungeonLayout, DungeonRoom, DungeonSize, Point}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.PDPageContentStream
import zio.{Task, ZIO, ZLayer}

final case class DungeonServer() {
  private val pageWidth = 1275
  private val pageHeight = 1650
  private val pageMargin = 60
  private val mapPadding = 40
  private val mapWidth = pageWidth - (pageMargin * 2)
  private val mapHeight = pageHeight - (pageMargin * 2)
  private val gridSize = 30

  private val trapDetail1 = Vector("Crude", "Ranged", "Sturdy", "Sturdy", "Ancient", "Large")
  private val trapDetail2 = Vector("Ensnaring", "Toxic", "Mechanical", "Mechanical", "Magical", "Deadly")
  private val minorHazards = Vector(
    "Short fall",
    "Stuck or locked barrier",
    "Stuck or locked barrier",
    "Dense rubble",
    "Collapsing walls",
    "Enfeebling magic",
  )
  private val soloDetail1 = Vector("Sneaky", "Mighty", "Mighty", "Clever", "Clever", "Mutated")
  private val soloDetail2 = Vector("Ambusher", "Brute", "Brute", "Spellcaster", "Spellcaster", "Pariah")
  private val npcDetails = Vector("Hiding", "Captive", "Captive", "Wounded", "Wounded", "Rival crawlers")
  private val mobDetail1 = Vector("Stealthy", "Reckless", "Reckless", "Magical", "Primitive", "Organized")
  private val mobDetail2 = Vector("Outcasts", "Minions", "Minions", "Tricksters", "Vermin", "Warriors")
  private val majorHazards = Vector(
    "Long fall",
    "Long fall",
    "Toxic gas or vapors",
    "Entrapping terrain",
    "Antimagic zone",
    "Drowning hazard",
  )
  private val treasureDetails = Vector(
    "Hidden",
    "Hidden",
    "Guarded by monster",
    "Guarded by monster",
    "Protected by trap",
    "Protected by hazard",
  )
  private val bossDetails = Vector(
    "Physically strongest",
    "Religious leader",
    "Guarded by minions",
    "Guarded by minions",
    "Guarded by minions",
    "Supreme sorcerer",
  )

  private val rng: ThreadLocalRandom = ThreadLocalRandom.current()

  private def rollDie(sides: Int): Int =
    rng.nextInt(1, sides + 1)

  private def sizeForRoll(roll: Int): DungeonSize =
    roll match {
      case 1 | 2 => DungeonSize("Small", 5, 10)
      case 3 | 4 | 5 => DungeonSize("Medium", 8, 10)
      case _ => DungeonSize("Large", 12, 10)
    }

  private def siteTypeForRoll(roll: Int): String =
    roll match {
      case 1 | 2 => "Cave"
      case 3 => "Tomb"
      case 4 => "Deep tunnels"
      case _ => "Ruins"
    }

  private def dangerForRoll(roll: Int): String =
    roll match {
      case 1 | 2 | 3 => "Unsafe"
      case 4 | 5 => "Risky"
      case _ => "Deadly"
    }

  private def roomTypeForRoll(roll: Int): String =
    roll match {
      case 1 | 2 => "Empty"
      case 3 => "Trap"
      case 4 => "Minor hazard"
      case 5 => "Solo monster"
      case 6 => "NPC"
      case 7 => "Monster mob"
      case 8 => "Major hazard"
      case 9 => "Treasure"
      case _ => "Boss monster"
    }

  private def detailsForRoom(roomType: String): List[String] = {
    def d6 = rollDie(6) - 1
    roomType match {
      case "Trap" =>
        List(s"Detail 1: ${trapDetail1(d6)}", s"Detail 2: ${trapDetail2(d6)}")
      case "Minor hazard" =>
        List(minorHazards(d6))
      case "Solo monster" =>
        List(s"Detail 1: ${soloDetail1(d6)}", s"Detail 2: ${soloDetail2(d6)}")
      case "NPC" =>
        List(npcDetails(d6))
      case "Monster mob" =>
        List(s"Detail 1: ${mobDetail1(d6)}", s"Detail 2: ${mobDetail2(d6)}")
      case "Major hazard" =>
        List(majorHazards(d6))
      case "Treasure" =>
        List(treasureDetails(d6))
      case "Boss monster" =>
        List(bossDetails(d6))
      case _ =>
        List.empty
    }
  }

  private def roomSizeFor(siteType: String, rand: Random): (Int, Int) = {
    siteType match {
      case "Cave" =>
        val w = 60 + rand.nextInt(70)
        val h = 50 + rand.nextInt(70)
        (w, h)
      case "Tomb" =>
        val w = 50 + rand.nextInt(60)
        val h = 40 + rand.nextInt(50)
        (w, h)
      case "Deep tunnels" =>
        val w = 80 + rand.nextInt(90)
        val h = 30 + rand.nextInt(30)
        (w, h)
      case _ =>
        val w = 55 + rand.nextInt(70)
        val h = 45 + rand.nextInt(70)
        (w, h)
    }
  }

  private def placeRooms(
    count: Int,
    siteType: String,
    seed: Long,
    roomRolls: List[(Int, String, List[String])],
  ): List[DungeonRoom] = {
    val rand = new Random(seed)
    val placed = scala.collection.mutable.ListBuffer.empty[DungeonRoom]
    val padding = 12
    val minX = mapPadding + 30
    val minY = mapPadding + 30
    val maxX = mapWidth - mapPadding - 30
    val maxY = mapHeight - mapPadding - 30

    def overlaps(x: Int, y: Int, w: Int, h: Int): Boolean =
      placed.exists { room =>
        val dx = math.abs(room.position.x - x)
        val dy = math.abs(room.position.y - y)
        dx < (room.width + w) / 2 + padding && dy < (room.height + h) / 2 + padding
      }

    roomRolls.zipWithIndex.foreach { case ((roll, roomType, details), idx) =>
      val (w, h) = roomSizeFor(siteType, rand)
      var attempts = 0
      var chosenX = minX + rand.nextInt(math.max(1, maxX - minX))
      var chosenY = minY + rand.nextInt(math.max(1, maxY - minY))
      while (attempts < 60 && overlaps(chosenX, chosenY, w, h)) {
        attempts += 1
        chosenX = minX + rand.nextInt(math.max(1, maxX - minX))
        chosenY = minY + rand.nextInt(math.max(1, maxY - minY))
      }
      placed += DungeonRoom(
        id = idx + 1,
        roll = roll,
        roomType = roomType,
        details = details,
        position = Point(chosenX, chosenY),
        width = w,
        height = h,
        objectiveRoom = false,
      )
    }
    placed.toList
  }

  private def buildCorridors(rooms: List[DungeonRoom]): List[DungeonCorridor] = {
    if (rooms.size <= 1) return Nil
    val edges = scala.collection.mutable.Set.empty[(Int, Int)]
    rooms.foreach { room =>
      val nearest = rooms.filterNot(_.id == room.id).minBy(r => distance(room.position, r.position))
      val key = if (room.id < nearest.id) (room.id, nearest.id) else (nearest.id, room.id)
      edges += key
    }
    rooms.indices.foreach { idx =>
      if (idx % 3 == 0 && rooms.size >= 4) {
        val a = rooms(idx)
        val b = rooms((idx + 2) % rooms.size)
        val key = if (a.id < b.id) (a.id, b.id) else (b.id, a.id)
        edges += key
      }
    }
    edges.toList.flatMap { case (aId, bId) =>
      for {
        a <- rooms.find(_.id == aId)
        b <- rooms.find(_.id == bId)
      } yield DungeonCorridor(a.position, b.position)
    }
  }

  private def distance(a: Point, b: Point): Double =
    math.hypot((a.x - b.x).toDouble, (a.y - b.y).toDouble)

  private def convexHull(points: List[Point]): List[Point] = {
    if (points.size <= 2) points
    else {
      val sorted = points.sortBy(p => (p.x, p.y))
      def cross(o: Point, a: Point, b: Point): Long =
        (a.x - o.x).toLong * (b.y - o.y).toLong - (a.y - o.y).toLong * (b.x - o.x).toLong
      def build(points: List[Point]): List[Point] =
        points.foldLeft(List.empty[Point]) { (acc, p) =>
          var res = acc
          while (res.size >= 2 && cross(res(res.size - 2), res.last, p) <= 0) {
            res = res.dropRight(1)
          }
          res :+ p
        }
      val lower = build(sorted)
      val upper = build(sorted.reverse)
      (lower.dropRight(1) ++ upper.dropRight(1)).distinct
    }
  }

  private def expandHull(points: List[Point], padding: Int): List[Point] = {
    if (points.isEmpty) points
    else {
      val cx = points.map(_.x).sum / points.size.toDouble
      val cy = points.map(_.y).sum / points.size.toDouble
      points.map { p =>
        val dx = p.x - cx
        val dy = p.y - cy
        val dist = math.max(1.0, math.hypot(dx, dy))
        val nx = (p.x + dx / dist * padding).round.toInt
        val ny = (p.y + dy / dist * padding).round.toInt
        Point(
          math.max(mapPadding, math.min(mapWidth - mapPadding, nx)),
          math.max(mapPadding, math.min(mapHeight - mapPadding, ny)),
        )
      }
    }
  }

  def randomDungeon: Task[Dungeon] =
    ZIO.attempt {
      val seed = rng.nextLong()
      val rand = new Random(seed)
      val size = sizeForRoll(rollDie(6))
      val siteType = siteTypeForRoll(rollDie(6))
      val danger = dangerForRoll(rollDie(6))
      val roomRolls = (1 to size.diceCount).toList.map { _ =>
        val roll = rollDie(10)
        val roomType = roomTypeForRoll(roll)
        (roll, roomType, detailsForRoom(roomType))
      }
      val highestRoll = roomRolls.map(_._1).max
      val objectiveIndex = roomRolls.indexWhere(_._1 == highestRoll)
      val roomsPlaced = placeRooms(size.diceCount, siteType, seed, roomRolls).map { room =>
        if (room.id == objectiveIndex + 1) room.copy(objectiveRoom = true) else room
      }
      val corridors = buildCorridors(roomsPlaced)
      val outline = expandHull(convexHull(roomsPlaced.map(_.position)), 35)
      val name = s"${siteType} Dungeon"
      Dungeon(
        name = name,
        siteType = siteType,
        size = size,
        dangerLevel = danger,
        rooms = roomsPlaced,
        corridors = corridors,
        layout = DungeonLayout(
          width = mapWidth,
          height = mapHeight,
          gridSize = gridSize,
          outline = outline,
          seed = seed,
        ),
      )
    }

  def renderDungeonPdf(dungeon: Dungeon): Task[Array[Byte]] =
    ZIO.attempt {
      val mapImage = renderDungeonMapImage(dungeon)
      val legendImage = renderDungeonLegendImage(dungeon)
      val pageSize = new PDRectangle(pageWidth.toFloat, pageHeight.toFloat)
      val document = new PDDocument()
      try {
        val mapPage = new PDPage(pageSize)
        val legendPage = new PDPage(pageSize)
        document.addPage(mapPage)
        document.addPage(legendPage)
        val mapXObject = LosslessFactory.createFromImage(document, mapImage)
        val legendXObject = LosslessFactory.createFromImage(document, legendImage)
        val mapStream = new PDPageContentStream(document, mapPage)
        try mapStream.drawImage(mapXObject, 0, 0, pageWidth, pageHeight)
        finally mapStream.close()
        val legendStream = new PDPageContentStream(document, legendPage)
        try legendStream.drawImage(legendXObject, 0, 0, pageWidth, pageHeight)
        finally legendStream.close()
        val output = new ByteArrayOutputStream()
        try {
          document.save(output)
          output.toByteArray
        } finally output.close()
      } finally document.close()
    }

  private def renderDungeonMapImage(dungeon: Dungeon): BufferedImage = {
    val image = baseDungeonImage()
    val g = image.createGraphics()
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBackground(g)
      val mapX = pageMargin
      val mapY = pageMargin
      val outlinePoints =
        if (dungeon.layout.outline.size >= 3) dungeon.layout.outline
        else List(
          Point(mapPadding, mapPadding),
          Point(mapWidth - mapPadding, mapPadding),
          Point(mapWidth - mapPadding, mapHeight - mapPadding),
          Point(mapPadding, mapHeight - mapPadding),
        )
      val outlinePath = polygonPath(mapX, mapY, outlinePoints)

      g.setColor(new Color(234, 221, 196))
      g.fill(outlinePath)

      val oldClip = g.getClip
      g.setClip(outlinePath)
      drawGrid(g, mapX, mapY, mapWidth, mapHeight, dungeon.layout.gridSize)
      drawCorridors(g, mapX, mapY, dungeon)
      drawRooms(g, mapX, mapY, dungeon)
      g.setClip(oldClip)

      g.setColor(new Color(90, 70, 50))
      g.setStroke(new BasicStroke(2.6f))
      g.draw(outlinePath)
      drawDungeonTitle(g, dungeon)
    } finally g.dispose()
    image
  }

  private def renderDungeonLegendImage(dungeon: Dungeon): BufferedImage = {
    val image = baseDungeonImage()
    val g = image.createGraphics()
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBackground(g)
      val legendX = pageMargin
      val legendY = pageMargin
      val legendW = pageWidth - (pageMargin * 2)
      val legendH = pageHeight - (pageMargin * 2)
      drawDungeonLegend(g, dungeon, legendX, legendY, legendW, legendH)
      drawDungeonTitle(g, dungeon)
    } finally g.dispose()
    image
  }

  private def baseDungeonImage(): BufferedImage =
    new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB)

  private def paintBackground(g: Graphics2D): Unit = {
    val parchmentTop = new Color(245, 231, 204)
    val parchmentBottom = new Color(230, 214, 183)
    g.setPaint(new GradientPaint(0f, 0f, parchmentTop, 0f, pageHeight.toFloat, parchmentBottom))
    g.fillRect(0, 0, pageWidth, pageHeight)
  }

  private def dungeonStyle(siteType: String): DungeonStyle =
    siteType match {
      case "Cave" =>
        DungeonStyle(
          roomFill = new Color(210, 190, 165),
          roomStroke = new Color(80, 60, 45),
          corridorStroke = new Color(90, 70, 50),
          roomCorner = 24,
          roomStrokeWidth = 2.2f,
          corridorWidth = 4.2f,
          corridorDash = None,
        )
      case "Tomb" =>
        DungeonStyle(
          roomFill = new Color(212, 206, 196),
          roomStroke = new Color(70, 60, 55),
          corridorStroke = new Color(70, 60, 55),
          roomCorner = 4,
          roomStrokeWidth = 2.0f,
          corridorWidth = 3.2f,
          corridorDash = None,
        )
      case "Deep tunnels" =>
        DungeonStyle(
          roomFill = new Color(220, 205, 182),
          roomStroke = new Color(85, 70, 55),
          corridorStroke = new Color(85, 70, 55),
          roomCorner = 8,
          roomStrokeWidth = 1.8f,
          corridorWidth = 4.6f,
          corridorDash = None,
        )
      case _ =>
        DungeonStyle(
          roomFill = new Color(224, 210, 190),
          roomStroke = new Color(90, 75, 60),
          corridorStroke = new Color(90, 75, 60),
          roomCorner = 6,
          roomStrokeWidth = 2.0f,
          corridorWidth = 3.4f,
          corridorDash = Some(Array(6f, 4f)),
        )
    }

  private def drawGrid(g: Graphics2D, mapX: Int, mapY: Int, mapWidth: Int, mapHeight: Int, gridSize: Int): Unit = {
    g.setColor(new Color(185, 170, 140, 60))
    var x = mapX
    while (x <= mapX + mapWidth) {
      g.drawLine(x, mapY, x, mapY + mapHeight)
      x += gridSize
    }
    var y = mapY
    while (y <= mapY + mapHeight) {
      g.drawLine(mapX, y, mapX + mapWidth, y)
      y += gridSize
    }
  }

  private def drawCorridors(g: Graphics2D, mapX: Int, mapY: Int, dungeon: Dungeon): Unit = {
    val style = dungeonStyle(dungeon.siteType)
    val stroke = style.corridorDash match {
      case Some(dash) => new BasicStroke(style.corridorWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f)
      case None => new BasicStroke(style.corridorWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    }
    g.setStroke(stroke)
    g.setColor(style.corridorStroke)
    dungeon.corridors.foreach { corridor =>
      g.drawLine(mapX + corridor.start.x, mapY + corridor.start.y, mapX + corridor.end.x, mapY + corridor.end.y)
    }
  }

  private def drawRooms(g: Graphics2D, mapX: Int, mapY: Int, dungeon: Dungeon): Unit = {
    val style = dungeonStyle(dungeon.siteType)
    dungeon.rooms.foreach { room =>
      val x = mapX + room.position.x - room.width / 2
      val y = mapY + room.position.y - room.height / 2
      val shape =
        if (style.roomCorner > 0)
          new java.awt.geom.RoundRectangle2D.Double(x.toDouble, y.toDouble, room.width.toDouble, room.height.toDouble, style.roomCorner, style.roomCorner)
        else
          new Rectangle2D.Double(x.toDouble, y.toDouble, room.width.toDouble, room.height.toDouble)

      g.setColor(style.roomFill)
      g.fill(shape)
      g.setColor(style.roomStroke)
      g.setStroke(new BasicStroke(style.roomStrokeWidth))
      g.draw(shape)

      val label = room.id.toString
      val font = new Font("Serif", Font.BOLD, 16)
      g.setFont(font)
      g.setColor(new Color(60, 45, 30))
      val metrics = g.getFontMetrics
      val labelW = metrics.stringWidth(label)
      val labelX = x + (room.width - labelW) / 2
      val labelY = y + (room.height + metrics.getAscent) / 2 - 2
      g.drawString(label, labelX, labelY)
    }
  }

  private def drawDungeonLegend(
    g: Graphics2D,
    dungeon: Dungeon,
    legendX: Int,
    legendY: Int,
    legendW: Int,
    legendH: Int,
  ): Unit = {
    g.setColor(new Color(235, 222, 198))
    g.fillRect(legendX, legendY, legendW, legendH)
    g.setColor(new Color(90, 70, 50))
    g.setStroke(new BasicStroke(2f))
    g.drawRect(legendX, legendY, legendW, legendH)

    val titleFont = new Font("Serif", Font.BOLD, 18)
    val bodyFontSizes = List(14, 13, 12, 11)
    g.setFont(titleFont)
    g.drawString("Dungeon Key", legendX + 10, legendY + 24)

    val summary =
      s"Type: ${dungeon.siteType}  |  Size: ${dungeon.size.name} (${dungeon.size.diceCount}d${dungeon.size.dieSides})  |  Danger: ${dungeon.dangerLevel}"
    val bodyStartY = legendY + 40
    val maxWidth = legendW - 20
    val availableHeight = legendY + legendH - 12 - bodyStartY

    def roomLines(room: DungeonRoom): List[String] = {
      val objective = if (room.objectiveRoom) " (Objective)" else ""
      val baseLine = s"${room.id}. ${room.roomType}$objective"
      val detailLines = room.details.map(detail => s"  - $detail")
      baseLine :: detailLines
    }

    def estimateHeight(fontSize: Int): Int = {
      val font = new Font("Serif", Font.PLAIN, fontSize)
      val metrics = g.getFontMetrics(font)
      val lineHeight = metrics.getHeight
      val spacing = math.max(2, lineHeight / 3)
      val summaryLines = countWrappedLines(summary, metrics, maxWidth)
      val roomLineCount = dungeon.rooms.map(roomLines).map { lines =>
        lines.map(line => countWrappedLines(line, metrics, maxWidth)).sum
      }.sum
      (summaryLines + roomLineCount) * lineHeight + spacing * (dungeon.rooms.size + 1)
    }

    val chosenSize = bodyFontSizes.find(size => estimateHeight(size) <= availableHeight).getOrElse(bodyFontSizes.last)
    val bodyFont = new Font("Serif", Font.PLAIN, chosenSize)
    g.setFont(bodyFont)
    val metrics = g.getFontMetrics
    val lineHeight = metrics.getHeight
    val spacing = math.max(2, lineHeight / 3)
    var cursorY = bodyStartY
    cursorY = drawWrappedText(g, summary, legendX + 10, cursorY, maxWidth, lineHeight)
    cursorY += spacing

    dungeon.rooms.foreach { room =>
      if (cursorY <= legendY + legendH - 12) {
        roomLines(room).foreach { line =>
          cursorY = drawWrappedText(g, line, legendX + 10, cursorY, maxWidth, lineHeight)
        }
        cursorY += spacing
      }
    }
  }

  private def drawDungeonTitle(g: Graphics2D, dungeon: Dungeon): Unit = {
    val titleFont = new Font("Serif", Font.BOLD, 26)
    g.setFont(titleFont)
    g.setColor(new Color(60, 45, 30))
    val title = s"${dungeon.name} (${dungeon.dangerLevel})"
    val width = g.getFontMetrics.stringWidth(title)
    g.drawString(title, (pageWidth - width) / 2, pageMargin - 10)
  }

  private def drawWrappedText(
    g: Graphics2D,
    text: String,
    x: Int,
    y: Int,
    maxWidth: Int,
    lineHeight: Int,
  ): Int = {
    val words = text.split("\\s+").toList
    val metrics = g.getFontMetrics
    var line = ""
    var cursor = y
    words.foreach { word =>
      val test = if (line.isEmpty) word else s"$line $word"
      if (metrics.stringWidth(test) <= maxWidth) {
        line = test
      } else {
        g.drawString(line, x, cursor)
        cursor += lineHeight
        line = word
      }
    }
    if (line.nonEmpty) {
      g.drawString(line, x, cursor)
      cursor += lineHeight
    }
    cursor
  }

  private def countWrappedLines(text: String, metrics: java.awt.FontMetrics, maxWidth: Int): Int = {
    val words = text.split("\\s+").toList
    var line = ""
    var lines = 0
    words.foreach { word =>
      val test = if (line.isEmpty) word else s"$line $word"
      if (metrics.stringWidth(test) <= maxWidth) {
        line = test
      } else {
        lines += 1
        line = word
      }
    }
    if (line.nonEmpty) lines + 1 else lines
  }

  private def polygonPath(mapX: Int, mapY: Int, points: List[Point]): Path2D = {
    val path = new Path2D.Double()
    points.headOption.foreach { first =>
      path.moveTo(mapX + first.x, mapY + first.y)
      points.tail.foreach { p =>
        path.lineTo(mapX + p.x, mapY + p.y)
      }
      path.closePath()
    }
    path
  }

  private final case class DungeonStyle(
    roomFill: Color,
    roomStroke: Color,
    corridorStroke: Color,
    roomCorner: Int,
    roomStrokeWidth: Float,
    corridorWidth: Float,
    corridorDash: Option[Array[Float]],
  )
}

object DungeonServer {
  val live: ZLayer[Any, Nothing, DungeonServer] =
    ZLayer.succeed(DungeonServer())
}
