package servers

import java.awt.{BasicStroke, Color, Font, GradientPaint, Graphics2D, RenderingHints}
import java.awt.geom.{Arc2D, Path2D, Rectangle2D, RoundRectangle2D}
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
  import DungeonServer._
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
    "Enfeebling magic"
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
    "Drowning hazard"
  )
  private val treasureDetails = Vector(
    "Hidden",
    "Hidden",
    "Guarded by monster",
    "Guarded by monster",
    "Protected by trap",
    "Protected by hazard"
  )
  private val bossDetails = Vector(
    "Physically strongest",
    "Religious leader",
    "Guarded by minions",
    "Guarded by minions",
    "Guarded by minions",
    "Supreme sorcerer"
  )

  private val rng: ThreadLocalRandom = ThreadLocalRandom.current()

  private def rollDie(sides: Int): Int =
    rng.nextInt(1, sides + 1)

  private def sizeForRoll(roll: Int): DungeonSize =
    roll match {
      case 1 | 2     => DungeonSize("Small", 5, 10)
      case 3 | 4 | 5 => DungeonSize("Medium", 8, 10)
      case _         => DungeonSize("Large", 12, 10)
    }

  private def siteTypeForRoll(roll: Int): String =
    roll match {
      case 1 | 2 => "Cave"
      case 3     => "Tomb"
      case 4     => "Deep tunnels"
      case _     => "Ruins"
    }

  private def dangerForRoll(roll: Int): String =
    roll match {
      case 1 | 2 | 3 => "Unsafe"
      case 4 | 5     => "Risky"
      case _         => "Deadly"
    }

  private def roomTypeForRoll(roll: Int): String =
    roll match {
      case 1 | 2 => "Empty"
      case 3     => "Trap"
      case 4     => "Minor hazard"
      case 5     => "Solo monster"
      case 6     => "NPC"
      case 7     => "Monster mob"
      case 8     => "Major hazard"
      case 9     => "Treasure"
      case _     => "Boss monster"
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
        val w = 80 + rand.nextInt(90)
        val h = 70 + rand.nextInt(80)
        (w, h)
      case "Deep tunnels" =>
        val w = 80 + rand.nextInt(90)
        val h = 30 + rand.nextInt(30)
        (w, h)
      case _ =>
        val w = 75 + rand.nextInt(85)
        val h = 60 + rand.nextInt(80)
        (w, h)
    }
  }

  private def floorPlanBounds(): RoomRect = {
    val margin = mapPadding + 20
    RoomRect(margin, margin, mapWidth - margin * 2, mapHeight - margin * 2)
  }

  private def floorPlanFootprint(rand: Random): (List[RoomRect], List[Point]) = {
    val bounds = floorPlanBounds()
    val cutW = (bounds.width * (0.28 + rand.nextDouble() * 0.18)).toInt
    val cutH = (bounds.height * (0.28 + rand.nextDouble() * 0.18)).toInt
    val x = bounds.x
    val y = bounds.y
    val w = bounds.width
    val h = bounds.height
    rand.nextInt(4) match {
      case 0 =>
        val rects = List(
          RoomRect(x, y, w - cutW, h),
          RoomRect(x + w - cutW, y, cutW, h - cutH)
        )
        val outline = List(
          Point(x, y),
          Point(x + w, y),
          Point(x + w, y + h - cutH),
          Point(x + w - cutW, y + h - cutH),
          Point(x + w - cutW, y + h),
          Point(x, y + h)
        )
        (rects, outline)
      case 1 =>
        val rects = List(
          RoomRect(x + cutW, y, w - cutW, h),
          RoomRect(x, y, cutW, h - cutH)
        )
        val outline = List(
          Point(x, y),
          Point(x + w, y),
          Point(x + w, y + h),
          Point(x + cutW, y + h),
          Point(x + cutW, y + h - cutH),
          Point(x, y + h - cutH)
        )
        (rects, outline)
      case 2 =>
        val rects = List(
          RoomRect(x, y, w - cutW, h),
          RoomRect(x + w - cutW, y + cutH, cutW, h - cutH)
        )
        val outline = List(
          Point(x, y),
          Point(x + w, y),
          Point(x + w, y + cutH),
          Point(x + w - cutW, y + cutH),
          Point(x + w - cutW, y + h),
          Point(x, y + h)
        )
        (rects, outline)
      case _ =>
        val rects = List(
          RoomRect(x + cutW, y, w - cutW, h),
          RoomRect(x, y + cutH, cutW, h - cutH)
        )
        val outline = List(
          Point(x, y),
          Point(x + w, y),
          Point(x + w, y + h - cutH),
          Point(x + cutW, y + h - cutH),
          Point(x + cutW, y + h),
          Point(x, y + h)
        )
        (rects, outline)
    }
  }

  private def splitRectOnce(rect: RoomRect, rand: Random, minSize: Int): Option[(RoomRect, RoomRect)] = {
    val canSplitHoriz = rect.height >= minSize * 2
    val canSplitVert = rect.width >= minSize * 2
    if (!canSplitHoriz && !canSplitVert) None
    else {
      val splitHoriz =
        if (canSplitHoriz && !canSplitVert) true
        else if (!canSplitHoriz && canSplitVert) false
        else if (rect.width > rect.height * 1.25) false
        else if (rect.height > rect.width * 1.25) true
        else rand.nextBoolean()

      if (splitHoriz) {
        val splitY = rect.y + minSize + rand.nextInt(math.max(1, rect.height - minSize * 2))
        val top = RoomRect(rect.x, rect.y, rect.width, splitY - rect.y)
        val bottom = RoomRect(rect.x, splitY, rect.width, rect.y + rect.height - splitY)
        Some((top, bottom))
      } else {
        val splitX = rect.x + minSize + rand.nextInt(math.max(1, rect.width - minSize * 2))
        val left = RoomRect(rect.x, rect.y, splitX - rect.x, rect.height)
        val right = RoomRect(splitX, rect.y, rect.x + rect.width - splitX, rect.height)
        Some((left, right))
      }
    }
  }

  private def splitToTargetCount(
      rects: List[RoomRect],
      rand: Random,
      minSize: Int,
      target: Int
  ): List[RoomRect] = {
    val rooms = scala.collection.mutable.ListBuffer(rects: _*)
    var guard = 0
    while (rooms.size < target && guard < 400) {
      guard += 1
      val candidates = rooms.zipWithIndex.filter { case (rect, _) =>
        rect.width >= minSize * 2 || rect.height >= minSize * 2
      }
      if (candidates.isEmpty) {
        guard = 999
      } else {
        val (rect, idx) = candidates.maxBy { case (r, _) => r.width * r.height }
        splitRectOnce(rect, rand, minSize) match {
          case Some((a, b)) =>
            rooms.remove(idx)
            rooms += a
            rooms += b
          case None =>
            guard = 999
        }
      }
    }
    rooms.toList
  }

  private def addHallways(
      rects: List[RoomRect],
      rand: Random,
      minRoom: Int,
      hallwayWidth: Int
  ): List[FloorCell] = {
    rects.flatMap { rect =>
      if (rand.nextDouble() > 0.35) List(FloorCell(rect, isHall = false))
      else {
        val canSplitVert = rect.width >= hallwayWidth + minRoom * 2
        val canSplitHoriz = rect.height >= hallwayWidth + minRoom * 2
        if (!canSplitVert && !canSplitHoriz) List(FloorCell(rect, isHall = false))
        else {
          val splitVert =
            if (canSplitVert && !canSplitHoriz) true
            else if (!canSplitVert && canSplitHoriz) false
            else rand.nextBoolean()

          if (splitVert) {
            val available = rect.width - hallwayWidth - minRoom * 2
            val hallX = rect.x + minRoom + rand.nextInt(math.max(1, available + 1))
            val left = RoomRect(rect.x, rect.y, hallX - rect.x, rect.height)
            val hall = RoomRect(hallX, rect.y, hallwayWidth, rect.height)
            val right =
              RoomRect(hallX + hallwayWidth, rect.y, rect.x + rect.width - (hallX + hallwayWidth), rect.height)
            List(
              FloorCell(left, isHall = false),
              FloorCell(hall, isHall = true),
              FloorCell(right, isHall = false)
            )
          } else {
            val available = rect.height - hallwayWidth - minRoom * 2
            val hallY = rect.y + minRoom + rand.nextInt(math.max(1, available + 1))
            val top = RoomRect(rect.x, rect.y, rect.width, hallY - rect.y)
            val hall = RoomRect(rect.x, hallY, rect.width, hallwayWidth)
            val bottom =
              RoomRect(rect.x, hallY + hallwayWidth, rect.width, rect.y + rect.height - (hallY + hallwayWidth))
            List(
              FloorCell(top, isHall = false),
              FloorCell(hall, isHall = true),
              FloorCell(bottom, isHall = false)
            )
          }
        }
      }
    }
  }

  private def floorPlanLayout(siteType: String, size: DungeonSize, seed: Long): (List[FloorCell], List[Point]) = {
    val rand = new Random(seed ^ 0x6a09e667L)
    val minSize = siteType match {
      case "Tomb" => 120
      case _      => 110
    }
    val baseRooms = size.name match {
      case "Small"  => 5
      case "Medium" => 8
      case _        => 12
    }
    val targetRooms = math.max(3, baseRooms + rand.nextInt(3) - 1)
    val (footprintRects, outline) = floorPlanFootprint(rand)
    val rects = splitToTargetCount(footprintRects, rand, minSize, targetRooms)
    val minRoom = siteType match {
      case "Tomb" => 70
      case _      => 60
    }
    val hallwayWidth = siteType match {
      case "Tomb" => 26
      case _      => 22
    }
    val cells = addHallways(rects, rand, minRoom, hallwayWidth)
    (cells, outline)
  }

  private def placeRooms(
      count: Int,
      siteType: String,
      seed: Long,
      roomRolls: List[(Int, String, List[String])]
  ): List[DungeonRoom] = {
    val rand = new Random(seed)
    val placed = scala.collection.mutable.ListBuffer.empty[DungeonRoom]
    val padding = siteType match {
      case "Tomb" | "Ruins" => 4
      case _                => 12
    }
    val maxAttempts = siteType match {
      case "Tomb" | "Ruins" => 120
      case _                => 60
    }
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
      while (attempts < maxAttempts && overlaps(chosenX, chosenY, w, h)) {
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
        objectiveRoom = false
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
          math.max(mapPadding, math.min(mapHeight - mapPadding, ny))
        )
      }
    }
  }

  private def defaultOutline(): List[Point] =
    List(
      Point(mapPadding, mapPadding),
      Point(mapWidth - mapPadding, mapPadding),
      Point(mapWidth - mapPadding, mapHeight - mapPadding),
      Point(mapPadding, mapHeight - mapPadding)
    )

  private def chooseEntrances(rooms: List[DungeonRoom], outline: List[Point], count: Int): List[Point] = {
    val outlinePoints = if (outline.size >= 3) outline else defaultOutline()
    val sortedRooms =
      rooms.sortBy(room => outlinePoints.map(p => distance(room.position, p)).minOption.getOrElse(Double.MaxValue))
    val needed = math.max(1, math.min(count, sortedRooms.size))
    sortedRooms.take(needed).flatMap { room =>
      outlinePoints.minByOption(p => distance(room.position, p))
    }
  }

  def randomDungeon: Task[Dungeon] =
    ZIO.attempt {
      val seed = rng.nextLong()
      val rand = new Random(seed)
      val size = sizeForRoll(rollDie(6))
      val siteType = siteTypeForRoll(rollDie(6))
      val danger = dangerForRoll(rollDie(6))
      val (roomsPlaced, outline, floorPlanCorridors) =
        if (siteType == "Tomb" || siteType == "Ruins") {
          val (cells, floorOutline) = floorPlanLayout(siteType, size, seed)
          val nonHallCount = cells.count(!_.isHall)
          val roomRolls = (1 to nonHallCount).toList.map { _ =>
            val roll = rollDie(10)
            val roomType = roomTypeForRoll(roll)
            (roll, roomType, detailsForRoom(roomType))
          }
          val objectiveIndex =
            if (roomRolls.isEmpty) -1 else roomRolls.indexWhere(_._1 == roomRolls.map(_._1).max)
          var nonHallIndex = 0
          val placed = cells.zipWithIndex.map { case (cell, idx) =>
            if (cell.isHall) {
              DungeonRoom(
                id = idx + 1,
                roll = 0,
                roomType = "Hallway",
                details = Nil,
                position = cell.rect.center,
                width = cell.rect.width,
                height = cell.rect.height,
                objectiveRoom = false
              )
            } else {
              val (roll, roomType, details) = roomRolls(nonHallIndex)
              val isObjective = nonHallIndex == objectiveIndex
              nonHallIndex += 1
              DungeonRoom(
                id = idx + 1,
                roll = roll,
                roomType = roomType,
                details = details,
                position = cell.rect.center,
                width = cell.rect.width,
                height = cell.rect.height,
                objectiveRoom = isObjective
              )
            }
          }
          val corridors = buildFloorPlanCorridors(
            placed.map(room =>
              RoomRect(
                room.position.x - room.width / 2,
                room.position.y - room.height / 2,
                room.width,
                room.height
              )
            )
          )
          (placed, floorOutline, corridors)
        } else {
          val roomRolls = (1 to size.diceCount).toList.map { _ =>
            val roll = rollDie(10)
            val roomType = roomTypeForRoll(roll)
            (roll, roomType, detailsForRoom(roomType))
          }
          val highestRoll = roomRolls.map(_._1).max
          val objectiveIndex = roomRolls.indexWhere(_._1 == highestRoll)
          val placed = placeRooms(size.diceCount, siteType, seed, roomRolls).map { room =>
            if (room.id == objectiveIndex + 1) room.copy(objectiveRoom = true) else room
          }
          val hull = expandHull(convexHull(placed.map(_.position)), 35)
          (placed, hull, buildCorridors(placed))
        }
      val corridors = floorPlanCorridors
      val entranceCount = size.name match {
        case "Small"  => 1
        case "Medium" => 2
        case _        => 3
      }
      val entrances = chooseEntrances(roomsPlaced, outline, entranceCount)
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
          entrances = entrances,
          seed = seed
        )
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
      val style = dungeonStyle(dungeon.siteType)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBackground(g, style)
      val mapX = pageMargin
      val mapY = pageMargin
      val outlinePoints =
        if (dungeon.layout.outline.size >= 3) dungeon.layout.outline
        else defaultOutline()
      val outlinePath = polygonPath(mapX, mapY, outlinePoints)

      val baseFill = if (style.floorPlan) style.openFill else style.wallFill
      g.setColor(baseFill)
      g.fill(outlinePath)

      val oldClip = g.getClip
      g.setClip(outlinePath)
      drawCorridors(g, mapX, mapY, dungeon, style)
      drawRooms(g, mapX, mapY, dungeon, style)
      drawGrid(g, mapX, mapY, mapWidth, mapHeight, dungeon.layout.gridSize, style.gridColor)
      drawDoorMarkers(g, mapX, mapY, dungeon, style)
      drawStairs(g, mapX, mapY, dungeon, style)
      drawEntranceMarker(g, mapX, mapY, dungeon, style)
      g.setClip(oldClip)

      g.setColor(if (style.floorPlan) style.roomStroke else style.corridorStroke)
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
      val style = dungeonStyle(dungeon.siteType)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBackground(g, style)
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

  private def paintBackground(g: Graphics2D, style: DungeonStyle): Unit = {
    g.setPaint(new GradientPaint(0f, 0f, style.backgroundTop, 0f, pageHeight.toFloat, style.backgroundBottom))
    g.fillRect(0, 0, pageWidth, pageHeight)
  }

  private def dungeonStyle(siteType: String): DungeonStyle =
    siteType match {
      case "Cave" =>
        DungeonStyle(
          backgroundTop = new Color(241, 226, 197),
          backgroundBottom = new Color(222, 203, 174),
          gridColor = new Color(160, 140, 110, 40),
          wallFill = new Color(70, 55, 42),
          openFill = new Color(214, 197, 172),
          roomStroke = new Color(110, 90, 70),
          corridorStroke = new Color(95, 75, 58),
          roomCorner = 24,
          roomStrokeWidth = 2.2f,
          corridorWidth = 4.2f,
          corridorDash = None,
          roomStrokeDash = None,
          floorPlan = false
        )
      case "Tomb" =>
        DungeonStyle(
          backgroundTop = new Color(236, 233, 225),
          backgroundBottom = new Color(214, 209, 198),
          gridColor = new Color(150, 150, 145, 45),
          wallFill = new Color(58, 53, 50),
          openFill = new Color(216, 210, 200),
          roomStroke = new Color(110, 102, 95),
          corridorStroke = new Color(95, 88, 82),
          roomCorner = 4,
          roomStrokeWidth = 2.4f,
          corridorWidth = 3.2f,
          corridorDash = None,
          roomStrokeDash = None,
          floorPlan = true
        )
      case "Deep tunnels" =>
        DungeonStyle(
          backgroundTop = new Color(238, 225, 200),
          backgroundBottom = new Color(216, 198, 172),
          gridColor = new Color(150, 135, 110, 40),
          wallFill = new Color(62, 54, 46),
          openFill = new Color(220, 205, 182),
          roomStroke = new Color(120, 100, 78),
          corridorStroke = new Color(105, 86, 68),
          roomCorner = 8,
          roomStrokeWidth = 1.8f,
          corridorWidth = 4.6f,
          corridorDash = None,
          roomStrokeDash = None,
          floorPlan = false
        )
      case _ =>
        DungeonStyle(
          backgroundTop = new Color(242, 229, 205),
          backgroundBottom = new Color(223, 205, 180),
          gridColor = new Color(165, 145, 120, 45),
          wallFill = new Color(65, 57, 48),
          openFill = new Color(226, 212, 192),
          roomStroke = new Color(120, 104, 88),
          corridorStroke = new Color(105, 90, 76),
          roomCorner = 6,
          roomStrokeWidth = 2.2f,
          corridorWidth = 3.4f,
          corridorDash = Some(Array(6f, 4f)),
          roomStrokeDash = Some(Array(10f, 6f)),
          floorPlan = true
        )
    }

  private def drawGrid(
      g: Graphics2D,
      mapX: Int,
      mapY: Int,
      mapWidth: Int,
      mapHeight: Int,
      gridSize: Int,
      gridColor: Color
  ): Unit = {
    g.setColor(gridColor)
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

  private def drawCorridors(g: Graphics2D, mapX: Int, mapY: Int, dungeon: Dungeon, style: DungeonStyle): Unit = {
    if (style.floorPlan) return
    val rand = new Random(dungeon.layout.seed ^ 0x3f62a9c5)
    dungeon.corridors.zipWithIndex.foreach { case (corridor, idx) =>
      val localRand = new Random(rand.nextLong() ^ (idx.toLong << 16))
      val path = corridorPath(corridor, mapX, mapY, dungeon.siteType, localRand)
      dungeon.siteType match {
        case "Cave" =>
          g.setStroke(new BasicStroke(style.corridorWidth + 3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
          g.setColor(style.corridorStroke)
          g.draw(path)
          g.setStroke(new BasicStroke(style.corridorWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
          g.setColor(style.openFill)
          g.draw(path)
        case "Ruins" =>
          g.setStroke(new BasicStroke(style.corridorWidth + 2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER))
          g.setColor(style.corridorStroke)
          g.draw(path)
          val dashStroke = style.corridorDash match {
            case Some(dash) =>
              new BasicStroke(style.corridorWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f)
            case None => new BasicStroke(style.corridorWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
          }
          g.setStroke(dashStroke)
          g.setColor(style.openFill)
          g.draw(path)
        case "Tomb" =>
          g.setStroke(new BasicStroke(style.corridorWidth + 2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER))
          g.setColor(style.corridorStroke)
          g.draw(path)
          g.setStroke(new BasicStroke(style.corridorWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER))
          g.setColor(style.openFill)
          g.draw(path)
        case "Deep tunnels" =>
          g.setStroke(new BasicStroke(style.corridorWidth + 2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
          g.setColor(style.corridorStroke)
          g.draw(path)
          g.setStroke(new BasicStroke(style.corridorWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
          g.setColor(style.openFill)
          g.draw(path)
        case _ =>
          val baseStroke = style.corridorDash match {
            case Some(dash) =>
              new BasicStroke(style.corridorWidth + 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f)
            case None => new BasicStroke(style.corridorWidth + 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
          }
          g.setStroke(baseStroke)
          g.setColor(style.corridorStroke)
          g.draw(path)
          g.setStroke(new BasicStroke(style.corridorWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
          g.setColor(style.openFill)
          g.draw(path)
      }
    }
  }

  private def drawRooms(g: Graphics2D, mapX: Int, mapY: Int, dungeon: Dungeon, style: DungeonStyle): Unit = {
    val rand = new Random(dungeon.layout.seed ^ 0x9e3779b9L)
    dungeon.rooms.foreach { room =>
      val localRand = new Random(rand.nextLong() ^ room.id.toLong)
      val x = mapX + room.position.x - room.width / 2
      val y = mapY + room.position.y - room.height / 2
      val shape = roomShape(room, dungeon.siteType, x, y, localRand, style)

      g.setColor(style.openFill)
      g.fill(shape)
      applyRoomTexture(g, room, dungeon.siteType, x, y, localRand)
      g.setColor(style.roomStroke)
      val stroke = style.roomStrokeDash match {
        case Some(dash) =>
          new BasicStroke(style.roomStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f)
        case None => new BasicStroke(style.roomStrokeWidth)
      }
      g.setStroke(stroke)
      g.draw(shape)

      if (dungeon.siteType == "Tomb" && room.width > 14 && room.height > 14) {
        val inset = 6
        val inner = new Rectangle2D.Double(
          (x + inset).toDouble,
          (y + inset).toDouble,
          (room.width - inset * 2).toDouble,
          (room.height - inset * 2).toDouble
        )
        g.setColor(new Color(170, 162, 155))
        g.setStroke(new BasicStroke(1.2f))
        g.draw(inner)

        val tileStep = 12
        g.setColor(new Color(185, 176, 168, 120))
        var tx = x + inset + tileStep
        while (tx < x + room.width - inset) {
          g.drawLine(tx, y + inset, tx, y + room.height - inset)
          tx += tileStep
        }
        var ty = y + inset + tileStep
        while (ty < y + room.height - inset) {
          g.drawLine(x + inset, ty, x + room.width - inset, ty)
          ty += tileStep
        }
      }

      if (dungeon.siteType == "Ruins") {
        val breaks = 3 + localRand.nextInt(4)
        g.setColor(style.openFill)
        (0 until breaks).foreach { _ =>
          val side = localRand.nextInt(4)
          val size = 6 + localRand.nextInt(8)
          side match {
            case 0 =>
              val bx = x + 4 + localRand.nextInt(math.max(1, room.width - 8))
              g.fillRect(bx, y - 2, size, 6)
            case 1 =>
              val bx = x + 4 + localRand.nextInt(math.max(1, room.width - 8))
              g.fillRect(bx, y + room.height - 4, size, 6)
            case 2 =>
              val by = y + 4 + localRand.nextInt(math.max(1, room.height - 8))
              g.fillRect(x - 2, by, 6, size)
            case _ =>
              val by = y + 4 + localRand.nextInt(math.max(1, room.height - 8))
              g.fillRect(x + room.width - 4, by, 6, size)
          }
        }
      }

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
      legendH: Int
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

    val entranceRoomIds = dungeon.layout.entrances.flatMap { entrance =>
      dungeon.rooms.minByOption(room => distance(room.position, entrance)).map(_.id)
    }.toSet

    def roomLines(room: DungeonRoom): List[String] = {
      val objective = if (room.objectiveRoom) " (Objective)" else ""
      val entranceLabel = if (entranceRoomIds.contains(room.id)) " (Entrance)" else ""
      val baseLine = s"${room.id}. ${room.roomType}$objective$entranceLabel"
      val detailLines = room.details.map(detail => s"  - $detail")
      baseLine :: detailLines
    }

    def estimateHeight(fontSize: Int): Int = {
      val font = new Font("Serif", Font.PLAIN, fontSize)
      val metrics = g.getFontMetrics(font)
      val lineHeight = metrics.getHeight
      val spacing = math.max(2, lineHeight / 3)
      val summaryLines = countWrappedLines(summary, metrics, maxWidth)
      val roomLineCount = dungeon.rooms
        .map(roomLines)
        .map { lines =>
          lines.map(line => countWrappedLines(line, metrics, maxWidth)).sum
        }
        .sum
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
      lineHeight: Int
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

  private def corridorPath(
      corridor: DungeonCorridor,
      mapX: Int,
      mapY: Int,
      siteType: String,
      rand: Random
  ): Path2D = {
    val sx = mapX + corridor.start.x
    val sy = mapY + corridor.start.y
    val ex = mapX + corridor.end.x
    val ey = mapY + corridor.end.y
    val dx = ex - sx
    val dy = ey - sy
    val length = math.max(1.0, math.hypot(dx.toDouble, dy.toDouble))
    val nx = -dy / length
    val ny = dx / length
    val path = new Path2D.Double()
    path.moveTo(sx, sy)
    siteType match {
      case "Cave" =>
        val steps = 4 + rand.nextInt(4)
        (1 until steps).foreach { step =>
          val t = step.toDouble / steps.toDouble
          val jitter = (rand.nextDouble() * 2.0 - 1.0) * 34.0
          val px = sx + dx * t + nx * jitter
          val py = sy + dy * t + ny * jitter
          path.lineTo(px, py)
        }
        path.lineTo(ex, ey)
      case "Deep tunnels" =>
        val steps = 3 + rand.nextInt(3)
        (1 until steps).foreach { step =>
          val t = step.toDouble / steps.toDouble
          val jitter = (rand.nextDouble() * 2.0 - 1.0) * 20.0
          val px = sx + dx * t + nx * jitter
          val py = sy + dy * t + ny * jitter
          path.lineTo(px, py)
        }
        path.lineTo(ex, ey)
      case "Tomb" | "Ruins" =>
        val bendHorizontalFirst = rand.nextBoolean()
        val midX = if (bendHorizontalFirst) ex else sx
        val midY = if (bendHorizontalFirst) sy else ey
        path.lineTo(midX, midY)
        path.lineTo(ex, ey)
      case _ =>
        val baseOffset = siteType match {
          case "Ruins" => 10.0
          case _       => 0.0
        }
        val offset = (rand.nextDouble() * 2.0 - 1.0) * baseOffset
        val midX = (sx + ex) / 2.0 + nx * offset
        val midY = (sy + ey) / 2.0 + ny * offset
        if (baseOffset > 0.1) path.quadTo(midX, midY, ex, ey)
        else path.lineTo(ex, ey)
    }
    path
  }

  private def drawDoorMarkers(g: Graphics2D, mapX: Int, mapY: Int, dungeon: Dungeon, style: DungeonStyle): Unit = {
    val roomByPoint = dungeon.rooms.map(room => (room.position.x, room.position.y) -> room).toMap
    dungeon.corridors.foreach { corridor =>
      val maybeStart = roomByPoint.get((corridor.start.x, corridor.start.y))
      val maybeEnd = roomByPoint.get((corridor.end.x, corridor.end.y))
      for {
        startRoom <- maybeStart
        endRoom <- maybeEnd
      } {
        drawDoorMarker(g, mapX, mapY, startRoom, endRoom.position, dungeon.siteType, style)
        drawDoorMarker(g, mapX, mapY, endRoom, startRoom.position, dungeon.siteType, style)
      }
    }
  }

  private def drawDoorMarker(
      g: Graphics2D,
      mapX: Int,
      mapY: Int,
      room: DungeonRoom,
      otherRoomPos: Point,
      siteType: String,
      style: DungeonStyle
  ): Unit = {
    val start = Point(mapX + room.position.x, mapY + room.position.y)
    val end = Point(mapX + otherRoomPos.x, mapY + otherRoomPos.y)
    val roomRect = new Rectangle2D.Double(
      (mapX + room.position.x - room.width / 2).toDouble,
      (mapY + room.position.y - room.height / 2).toDouble,
      room.width.toDouble,
      room.height.toDouble
    )
    val hit = lineRectIntersection(start, end, roomRect)
    hit.foreach { p =>
      val angle = math.atan2((end.y - start.y).toDouble, (end.x - start.x).toDouble)
      val markerW = 10.0
      val markerH = 4.0
      val oldTransform = g.getTransform
      g.translate(p.x, p.y)
      g.rotate(angle)
      siteType match {
        case "Cave" | "Deep tunnels" =>
          val arch = new Arc2D.Double(-markerW / 2, -markerH, markerW, markerH * 2, 0, 180, Arc2D.OPEN)
          g.setColor(style.roomStroke)
          g.setStroke(new BasicStroke(1.2f))
          g.draw(arch)
        case _ =>
          val door = new Rectangle2D.Double(-markerW / 2, -markerH / 2, markerW, markerH)
          g.setColor(style.openFill)
          g.fill(door)
          g.setColor(style.roomStroke)
          g.setStroke(new BasicStroke(1.2f))
          g.draw(door)
      }
      g.setTransform(oldTransform)
    }
  }

  private def lineRectIntersection(a: Point, b: Point, rect: Rectangle2D): Option[Point] = {
    val ax = a.x.toDouble
    val ay = a.y.toDouble
    val bx = b.x.toDouble
    val by = b.y.toDouble
    if (ax == bx && ay == by) return None

    val sides = List(
      (rect.getMinX, rect.getMinY, rect.getMaxX, rect.getMinY),
      (rect.getMaxX, rect.getMinY, rect.getMaxX, rect.getMaxY),
      (rect.getMaxX, rect.getMaxY, rect.getMinX, rect.getMaxY),
      (rect.getMinX, rect.getMaxY, rect.getMinX, rect.getMinY)
    )

    def segmentIntersection(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        x3: Double,
        y3: Double,
        x4: Double,
        y4: Double
    ): Option[(Double, Double, Double)] = {
      val denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1)
      if (math.abs(denom) < 0.0001) None
      else {
        val ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom
        val ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom
        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
          val ix = x1 + ua * (x2 - x1)
          val iy = y1 + ua * (y2 - y1)
          Some((ua, ix, iy))
        } else None
      }
    }

    val hits = sides.flatMap { case (x1, y1, x2, y2) =>
      segmentIntersection(ax, ay, bx, by, x1, y1, x2, y2).map { case (t, ix, iy) =>
        (t, Point(ix.round.toInt, iy.round.toInt))
      }
    }
    hits.sortBy(_._1).headOption.map(_._2)
  }

  private def drawStairs(g: Graphics2D, mapX: Int, mapY: Int, dungeon: Dungeon, style: DungeonStyle): Unit = {
    val room = dungeon.rooms.find(_.objectiveRoom).orElse(dungeon.rooms.headOption)
    room.foreach { target =>
      val center = Point(mapX + target.position.x, mapY + target.position.y)
      val nearest = dungeon.corridors
        .flatMap(corridor => List(corridor.start, corridor.end))
        .minByOption(p => distance(target.position, p))
        .map(p => Point(mapX + p.x, mapY + p.y))
        .getOrElse(Point(center.x + target.width / 4, center.y))
      val angle = math.atan2((nearest.y - center.y).toDouble, (nearest.x - center.x).toDouble)
      val offset = math.max(8.0, math.min(target.width, target.height) / 3.0)
      val stairCenter = Point(
        (center.x + math.cos(angle) * offset).round.toInt,
        (center.y + math.sin(angle) * offset).round.toInt
      )

      val stepCount = 5
      val stepGap = 3
      val maxWidth = 16
      val oldTransform = g.getTransform
      g.translate(stairCenter.x, stairCenter.y)
      g.rotate(angle)
      g.setColor(style.roomStroke)
      g.setStroke(new BasicStroke(1.4f))
      (0 until stepCount).foreach { idx =>
        val y = idx * stepGap
        val width = maxWidth - idx * 2
        g.drawLine(-width / 2, y, width / 2, y)
      }
      g.setTransform(oldTransform)
    }
  }

  private def drawEntranceMarker(g: Graphics2D, mapX: Int, mapY: Int, dungeon: Dungeon, style: DungeonStyle): Unit = {
    dungeon.layout.entrances.foreach { entrance =>
      val room = dungeon.rooms.minByOption(r => distance(r.position, entrance))
      room.foreach { target =>
        val roomCenter = Point(mapX + target.position.x, mapY + target.position.y)
        val entrancePoint = Point(mapX + entrance.x, mapY + entrance.y)
        val angle = math.atan2((entrancePoint.y - roomCenter.y).toDouble, (entrancePoint.x - roomCenter.x).toDouble)
        val inset = 8.0
        val drawPoint = Point(
          (entrancePoint.x - math.cos(angle) * inset).round.toInt,
          (entrancePoint.y - math.sin(angle) * inset).round.toInt
        )
        val markerW = 18.0
        val markerH = 6.0
        val oldTransform = g.getTransform
        g.translate(drawPoint.x, drawPoint.y)
        g.rotate(angle)
        dungeon.siteType match {
          case "Cave" | "Deep tunnels" =>
            val arch = new Arc2D.Double(-markerW / 2, -markerH, markerW, markerH * 2, 0, 180, Arc2D.OPEN)
            g.setColor(style.openFill)
            g.setStroke(new BasicStroke(2.8f))
            g.draw(arch)
            g.setColor(style.roomStroke)
            g.setStroke(new BasicStroke(1.6f))
            g.draw(arch)
            g.fillOval(-3, -3, 6, 6)
          case _ =>
            val door = new Rectangle2D.Double(-markerW / 2, -markerH / 2, markerW, markerH)
            g.setColor(style.openFill)
            g.fill(door)
            g.setColor(style.roomStroke)
            g.setStroke(new BasicStroke(2.6f))
            g.draw(door)
            g.fillOval(-3, -3, 6, 6)
        }
        g.setTransform(oldTransform)
      }
    }
  }

  private def roomShape(
      room: DungeonRoom,
      siteType: String,
      x: Int,
      y: Int,
      rand: Random,
      style: DungeonStyle
  ): java.awt.Shape = {
    siteType match {
      case "Cave" =>
        val cx = x + room.width / 2.0
        val cy = y + room.height / 2.0
        val rx = room.width / 2.0
        val ry = room.height / 2.0
        val points = 12
        val path = new Path2D.Double()
        (0 until points).foreach { i =>
          val angle = i * (Math.PI * 2 / points)
          val jitterX = 0.8 + rand.nextDouble() * 0.4
          val jitterY = 0.8 + rand.nextDouble() * 0.4
          val px = cx + Math.cos(angle) * rx * jitterX
          val py = cy + Math.sin(angle) * ry * jitterY
          if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.closePath()
        path
      case "Deep tunnels" =>
        val corner = math.min(room.width, room.height).toDouble * 0.65
        new RoundRectangle2D.Double(x.toDouble, y.toDouble, room.width.toDouble, room.height.toDouble, corner, corner)
      case "Tomb" =>
        new Rectangle2D.Double(x.toDouble, y.toDouble, room.width.toDouble, room.height.toDouble)
      case "Ruins" =>
        new Rectangle2D.Double(x.toDouble, y.toDouble, room.width.toDouble, room.height.toDouble)
      case _ =>
        new RoundRectangle2D.Double(
          x.toDouble,
          y.toDouble,
          room.width.toDouble,
          room.height.toDouble,
          style.roomCorner,
          style.roomCorner
        )
    }
  }

  private def applyRoomTexture(
      g: Graphics2D,
      room: DungeonRoom,
      siteType: String,
      x: Int,
      y: Int,
      rand: Random
  ): Unit = {
    siteType match {
      case "Cave" =>
        val dots = math.max(6, (room.width * room.height) / 1200)
        g.setColor(new Color(150, 130, 105, 80))
        (0 until dots).foreach { _ =>
          val radius = 2 + rand.nextInt(4)
          val dx = x + 6 + rand.nextInt(math.max(1, room.width - 12))
          val dy = y + 6 + rand.nextInt(math.max(1, room.height - 12))
          g.fillOval(dx, dy, radius, radius)
        }
      case "Ruins" =>
        val rubble = math.max(4, (room.width * room.height) / 1800)
        g.setColor(new Color(150, 140, 125, 100))
        (0 until rubble).foreach { _ =>
          val w = 4 + rand.nextInt(8)
          val h = 2 + rand.nextInt(6)
          val dx = x + 4 + rand.nextInt(math.max(1, room.width - 8))
          val dy = y + 4 + rand.nextInt(math.max(1, room.height - 8))
          g.fillRect(dx, dy, w, h)
        }
      case _ => ()
    }
  }

  private def buildFloorPlanCorridors(rects: List[RoomRect]): List[DungeonCorridor] = {
    val corridors = scala.collection.mutable.ListBuffer.empty[DungeonCorridor]
    val count = rects.size
    def overlap(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int): Int =
      math.min(aEnd, bEnd) - math.max(aStart, bStart)

    (0 until count).foreach { i =>
      val a = rects(i)
      val aRight = a.x + a.width
      val aBottom = a.y + a.height
      (i + 1 until count).foreach { j =>
        val b = rects(j)
        val bRight = b.x + b.width
        val bBottom = b.y + b.height
        val sharedVertical = (aRight == b.x || bRight == a.x)
        val sharedHorizontal = (aBottom == b.y || bBottom == a.y)
        if (sharedVertical) {
          val overlapY = overlap(a.y, aBottom, b.y, bBottom)
          if (overlapY >= 12) corridors += DungeonCorridor(a.center, b.center)
        } else if (sharedHorizontal) {
          val overlapX = overlap(a.x, aRight, b.x, bRight)
          if (overlapX >= 12) corridors += DungeonCorridor(a.center, b.center)
        }
      }
    }
    corridors.toList
  }

}

object DungeonServer {
  private final case class RoomRect(x: Int, y: Int, width: Int, height: Int) {
    def center: Point = Point(x + width / 2, y + height / 2)
  }

  private final case class FloorCell(rect: RoomRect, isHall: Boolean)

  private final case class DungeonStyle(
      backgroundTop: Color,
      backgroundBottom: Color,
      gridColor: Color,
      wallFill: Color,
      openFill: Color,
      roomStroke: Color,
      corridorStroke: Color,
      roomCorner: Int,
      roomStrokeWidth: Float,
      corridorWidth: Float,
      corridorDash: Option[Array[Float]],
      roomStrokeDash: Option[Array[Float]],
      floorPlan: Boolean
  )

  val live: ZLayer[Any, Nothing, DungeonServer] =
    ZLayer.succeed(DungeonServer())
}
