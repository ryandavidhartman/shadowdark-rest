package servers

import java.awt.{AlphaComposite, BasicStroke, Color, Font, GradientPaint, Graphics2D, RenderingHints}
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom
import javax.imageio.ImageIO
import models.{HexCell, HexMap, HexMapLayout, HexOverlay, HexPointOfInterest}
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
  private val poiInsetFactor = 0.7
  private val hexDirections = List("N", "NE", "SE", "S", "SW", "NW")
  private val activeHexStroke = new BasicStroke(4.0f)
  private val activeHexColor = new Color(25, 92, 54)
  private val terrainSteps = Vector(
    ("Desert", "Arctic"),
    ("Swamp", "Taiga"),
    ("Grassland", "Grassland"),
    ("Jungle", "Forest"),
    ("River/coast", "River/coast"),
    ("Ocean", "Ocean"),
    ("Mountain", "Mountain")
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
    "Holy shrine"
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
    "With a door to another plane"
  )
  private val cataclysms = Vector(
    "Volcano",
    "Fire",
    "Earthquake",
    "Storm",
    "Flood",
    "War",
    "Pestilence",
    "Magical disaster"
  )
  private val riverOrientations = Vector("N-S", "E-W", "NE-SW", "NW-SE")
  private val coastOrientations = Vector("N", "NE", "SE", "S", "SW", "NW")
  private val terrainTextures = Map(
    "Arctic" -> "images/arctic.png",
    "Desert" -> "images/grassland.png",
    "Swamp" -> "images/swamp.png",
    "Taiga" -> "images/taiga.png",
    "Grassland" -> "images/grassland.png",
    "Forest" -> "images/forest.png",
    "Jungle" -> "images/forest.png",
    "Mountain" -> "images/mountain.png",
    "Ocean" -> "images/ocean.png"
  )
  private val overlayTextures = Map(
    "River" -> "images/river_OVERLAY.png",
    "RiverCorner" -> "images/river_CORNER.png",
    "Coast" -> "images/coast_OVERLAY.png"
  )
  private val terrainFillColors = Map(
    "Desert" -> new Color(222, 201, 140),
    "Arctic" -> new Color(210, 230, 238),
    "Swamp" -> new Color(116, 134, 92),
    "Taiga" -> new Color(150, 174, 130),
    "Grassland" -> new Color(164, 188, 115),
    "Jungle" -> new Color(88, 140, 84),
    "Forest" -> new Color(110, 160, 120),
    "River/coast" -> new Color(120, 170, 205),
    "Ocean" -> new Color(88, 130, 178),
    "Mountain" -> new Color(150, 150, 150)
  )
  private lazy val terrainTextureCache: Map[String, BufferedImage] =
    terrainTextures.flatMap { case (terrain, path) => loadImage(path, trim = true).map(terrain -> _) }
  private lazy val overlayTextureCache: Map[String, BufferedImage] =
    overlayTextures.flatMap { case (overlay, path) => loadImage(path, trim = false).map(overlay -> _) }

  def randomMap: Task[HexMap] =
    ZIO.attempt {
      val climate = climateForRoll(rollDie(2))
      val danger = dangerForRoll(rollDie(6))
      val step = terrainStepForRoll(roll2d6())
      val centerHex = buildHex(0, 0, step, climate, 1, 1, allowOverlay = false)
      val allowOcean = centerHex.terrain == "Ocean"
      val neighborOffsets = neighborOffsetsFor(0)
      val (neighborHexes, _, _) = neighborOffsets.foldLeft((List.empty[HexCell], 2, 2)) {
        case ((hexes, nextId, nextPoiId), (col, row)) =>
          val allowNeighborOcean = allowOcean || rollDie(6) == 1
          val nextStep = nextTerrainStepWithRules(step, climate, allowNeighborOcean)
          val hex = buildHex(col, row, nextStep, climate, nextId, nextPoiId, allowOverlay = false)
          (hexes :+ hex, nextId + 1, nextPoiId + 1)
      }
      val hexes = applyRiverCoastOverlays(centerHex +: neighborHexes, climate)
      HexMap(
        name = "Overland Hex Map",
        climate = climate,
        dangerLevel = danger,
        layout = layoutFor(hexes),
        hexes = hexes,
        activeColumn = 0,
        activeRow = 0
      )
    }

  def nextMap(current: HexMap, direction: String): Task[HexMap] =
    ZIO.attempt {
      val origin =
        current.hexes
          .find(h => h.column == current.activeColumn && h.row == current.activeRow)
          .getOrElse(throw new IllegalArgumentException("Origin hex not found in map"))
      val (targetCol, targetRow) = moveCoords(current.activeColumn, current.activeRow, direction)
      val existing = current.hexes.find(h => h.column == targetCol && h.row == targetRow)
      existing match {
        case Some(_) =>
          current.copy(
            layout = layoutFor(current.hexes),
            activeColumn = targetCol,
            activeRow = targetRow
          )
        case None =>
          val step = nextTerrainStep(origin.terrainStep)
          val nextId = current.hexes.map(_.id).maxOption.getOrElse(0) + 1
          val nextPoiId = current.hexes.flatMap(_.pointOfInterest.map(_.id)).maxOption.getOrElse(0) + 1
          val nextHex = buildHex(
            targetCol,
            targetRow,
            step,
            current.climate,
            nextId,
            nextPoiId,
            allowOverlay = false
          )
          val updatedHexes = applyRiverCoastOverlays(current.hexes :+ nextHex, current.climate)
          current.copy(
            layout = layoutFor(updatedHexes),
            hexes = updatedHexes,
            activeColumn = targetCol,
            activeRow = targetRow
          )
      }
    }

  def renderHexMapPdf(map: HexMap): Task[Array[Byte]] =
    ZIO.attempt {
      val image = renderHexMapImage(
        map,
        includeLegend = true,
        transparentBackground = false,
        tightCrop = false
      )
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

  def renderHexMapPng(map: HexMap): Task[Array[Byte]] =
    ZIO.attempt {
      val image = renderHexMapImage(
        map,
        includeLegend = false,
        transparentBackground = true,
        tightCrop = true
      )
      val output = new ByteArrayOutputStream()
      try {
        ImageIO.write(image, "png", output)
        output.toByteArray
      } finally output.close()
    }

  private def rollDie(sides: Int): Int =
    rng.nextInt(1, sides + 1)

  private def roll2d6(): Int =
    rollDie(6) + rollDie(6)

  private def climateForRoll(roll: Int): String =
    if (roll == 1) "Warm" else "Cold"

  private def dangerForRoll(roll: Int): String =
    roll match {
      case 1     => "Safe"
      case 2 | 3 => "Unsafe"
      case 4 | 5 => "Risky"
      case _     => "Deadly"
    }

  private def terrainStepForRoll(roll: Int): Int =
    roll match {
      case 2         => 0
      case 3         => 1
      case 4 | 5 | 6 => 2
      case 7 | 8     => 3
      case 9 | 10    => 4
      case 11        => 5
      case _         => 6
    }

  private def nextTerrainStep(currentStep: Int): Int = {
    val roll = roll2d6()
    val stepCount = terrainSteps.size
    roll match {
      case 2 | 3             => (currentStep + 1) % stepCount
      case 4 | 5 | 6 | 7 | 8 => currentStep
      case 9 | 10 | 11       => (currentStep + 2) % stepCount
      case _                 => terrainStepForRoll(roll2d6())
    }
  }

  private def nextTerrainStepWithRules(currentStep: Int, climate: String, allowOcean: Boolean): Int = {
    var nextStep = nextTerrainStep(currentStep)
    var attempts = 0
    while (!allowOcean && terrainName(nextStep, climate) == "Ocean" && attempts < 12) {
      nextStep = nextTerrainStep(currentStep)
      attempts += 1
    }
    if (!allowOcean && terrainName(nextStep, climate) == "Ocean") {
      var fallback = currentStep
      var guard = 0
      while (terrainName(fallback, climate) == "Ocean" && guard < terrainSteps.size) {
        fallback = (fallback + 1) % terrainSteps.size
        guard += 1
      }
      fallback
    } else {
      nextStep
    }
  }

  private def applyRiverCoastOverlays(hexes: List[HexCell], climate: String): List[HexCell] = {
    val byCoord = hexes.map(h => (h.column, h.row) -> h).toMap
    val coastCoords = byCoord.collect {
      case ((col, row), hex)
          if hex.terrain == "River/coast" &&
            hexDirections.exists { direction =>
              val (ncol, nrow) = neighborCoords(col, row, direction)
              byCoord.get((ncol, nrow)).exists(_.terrain == "Ocean")
            } =>
        (col, row)
    }.toSet
    val riverCoords = byCoord.collect {
      case ((col, row), hex) if hex.terrain == "River/coast" && !coastCoords.contains((col, row)) =>
        (col, row)
    }.toSet
    val riverCoordsWithNeighbor =
      if (riverCoords.size <= 1) riverCoords
      else {
        riverCoords.filter { case (col, row) =>
          hexDirections.exists { direction =>
            val (ncol, nrow) = neighborCoords(col, row, direction)
            riverCoords.contains((ncol, nrow))
          }
        }
      }
    val axisByCoord = riverCoordsWithNeighbor.map { case (col, row) =>
      val dirs = hexDirections.filter { direction =>
        val (ncol, nrow) = neighborCoords(col, row, direction)
        riverCoordsWithNeighbor.contains((ncol, nrow))
      }
      (col, row) -> riverAxisForDirs(dirs)
    }.toMap
    val phaseByCoord = riverPhaseByAxis(riverCoordsWithNeighbor, axisByCoord)
    val withOverlays = hexes.map { hex =>
      if (hex.terrain != "River/coast") hex
      else {
        val oceanDirs = hexDirections.filter { direction =>
          val (col, row) = neighborCoords(hex.column, hex.row, direction)
          byCoord.get((col, row)).exists(_.terrain == "Ocean")
        }
        val riverDirs = hexDirections.filter { direction =>
          val (col, row) = neighborCoords(hex.column, hex.row, direction)
          riverCoordsWithNeighbor.contains((col, row))
        }
        val baseTerrain = randomLandTerrain(climate)
        val overlay =
          if (oceanDirs.nonEmpty) {
            val orientation = oceanDirs(rollDie(oceanDirs.size) - 1)
            Some(HexOverlay(kind = "Coast", orientation = orientation, baseTerrain = baseTerrain))
          } else if (riverDirs.nonEmpty) {
            val (kind, orientation) = riverOverlayForNeighborDirs(
              riverDirs,
              hex,
              axisByCoord,
              phaseByCoord
            )
            Some(HexOverlay(kind = kind, orientation = orientation, baseTerrain = baseTerrain))
          } else None
        overlay match {
          case Some(nextOverlay) =>
            hex.copy(overlay = Some(nextOverlay))
          case None =>
            val nextTerrain = baseTerrain
            val nextStep = terrainStepForTerrain(nextTerrain, climate, hex.terrainStep)
            hex.copy(terrain = nextTerrain, terrainStep = nextStep, overlay = None)
        }
      }
    }
    val trimmed = trimDisconnectedRivers(withOverlays, climate)
    limitRiverCoastDensity(trimmed, climate)
  }

  private def riverAxisForDirs(directions: List[String]): String = {
    val counts = Map(
      "N-S" -> directions.count(dir => dir == "N" || dir == "S"),
      "NE-SW" -> directions.count(dir => dir == "NE" || dir == "SW"),
      "NW-SE" -> directions.count(dir => dir == "NW" || dir == "SE")
    )
    val maxCount = counts.values.maxOption.getOrElse(0)
    val candidates =
      if (maxCount >= 2) counts.collect { case (axis, count) if count == maxCount => axis }.toVector
      else counts.collect { case (axis, count) if count == 1 => axis }.toVector
    if (candidates.nonEmpty) candidates.min
    else riverOrientations.headOption.getOrElse("N-S")
  }

  private def riverPhaseByAxis(
      coords: Set[(Int, Int)],
      axisByCoord: Map[(Int, Int), String]
  ): Map[(Int, Int), Boolean] = {
    val remaining = scala.collection.mutable.Set.from(coords)
    val phaseByCoord = scala.collection.mutable.Map.empty[(Int, Int), Boolean]
    def axisDirs(axis: String): List[String] =
      axis match {
        case "N-S"   => List("N", "S")
        case "NE-SW" => List("NE", "SW")
        case "NW-SE" => List("NW", "SE")
        case _       => Nil
      }
    while (remaining.nonEmpty) {
      val seed = remaining.head
      remaining.remove(seed)
      phaseByCoord.update(seed, false)
      val queue = scala.collection.mutable.Queue(seed)
      while (queue.nonEmpty) {
        val (col, row) = queue.dequeue()
        val axis = axisByCoord.getOrElse((col, row), "N-S")
        axisDirs(axis).foreach { direction =>
          val next = neighborCoords(col, row, direction)
          if (remaining.contains(next) && axisByCoord.get(next).contains(axis)) {
            remaining.remove(next)
            val nextPhase = !phaseByCoord.getOrElse((col, row), false)
            phaseByCoord.update(next, nextPhase)
            queue.enqueue(next)
          }
        }
      }
    }
    phaseByCoord.toMap
  }

  private def trimDisconnectedRivers(hexes: List[HexCell], climate: String): List[HexCell] = {
    val byCoord = hexes.map(h => (h.column, h.row) -> h).toMap
    val coastCoords = byCoord.collect {
      case ((col, row), hex)
          if hex.terrain == "River/coast" &&
            hexDirections.exists { direction =>
              val (ncol, nrow) = neighborCoords(col, row, direction)
              byCoord.get((ncol, nrow)).exists(_.terrain == "Ocean")
            } =>
        (col, row)
    }.toSet
    val riverCoords = byCoord.collect {
      case ((col, row), hex) if hex.terrain == "River/coast" && !coastCoords.contains((col, row)) =>
        (col, row)
    }.toSet
    val components = connectedComponents(riverCoords)
    val largest =
      if (components.isEmpty) Set.empty[(Int, Int)]
      else {
        val maxSize = components.map(_.size).max
        val contenders = components.filter(_.size == maxSize)
        contenders(rng.nextInt(contenders.size))
      }
    val toDowngrade = riverCoords.diff(largest)
    if (toDowngrade.isEmpty) hexes
    else {
      hexes.map { hex =>
        if (!toDowngrade.contains((hex.column, hex.row))) hex
        else {
          val nextTerrain = randomLandTerrain(climate)
          val nextStep = terrainStepForTerrain(nextTerrain, climate, hex.terrainStep)
          hex.copy(terrain = nextTerrain, terrainStep = nextStep, overlay = None)
        }
      }
    }
  }

  private def connectedComponents(coords: Set[(Int, Int)]): List[Set[(Int, Int)]] = {
    var remaining = coords
    var components = List.empty[Set[(Int, Int)]]
    while (remaining.nonEmpty) {
      val start = remaining.head
      var queue = List(start)
      var visited = Set(start)
      remaining -= start
      while (queue.nonEmpty) {
        val current = queue.head
        queue = queue.tail
        hexDirections.foreach { direction =>
          val next = neighborCoords(current._1, current._2, direction)
          if (remaining.contains(next)) {
            remaining -= next
            visited += next
            queue = next :: queue
          }
        }
      }
      components = visited :: components
    }
    components
  }

  private def limitRiverCoastDensity(hexes: List[HexCell], climate: String): List[HexCell] = {
    val maxRiverCoast =
      if (hexes.size <= 7) 3
      else Math.max(3, Math.round(hexes.size * 0.35f).toInt)
    val riverCoast = hexes.filter(_.terrain == "River/coast")
    if (riverCoast.size <= maxRiverCoast) hexes
    else {
      val byCoord = hexes.map(h => (h.column, h.row) -> h).toMap
      val sorted = riverCoast.sortBy { hex =>
        val oceanNeighbor = hexDirections.exists { direction =>
          val (col, row) = neighborCoords(hex.column, hex.row, direction)
          byCoord.get((col, row)).exists(_.terrain == "Ocean")
        }
        val riverNeighbors = hexDirections.count { direction =>
          val (col, row) = neighborCoords(hex.column, hex.row, direction)
          byCoord.get((col, row)).exists(_.terrain == "River/coast")
        }
        (if (oceanNeighbor) 1 else 0, riverNeighbors, rng.nextInt(1000))
      }
      val toDowngrade = sorted.take(riverCoast.size - maxRiverCoast).map(h => (h.column, h.row)).toSet
      hexes.map { hex =>
        if (!toDowngrade.contains((hex.column, hex.row))) hex
        else {
          val nextTerrain = randomLandTerrain(climate)
          val nextStep = terrainStepForTerrain(nextTerrain, climate, hex.terrainStep)
          hex.copy(terrain = nextTerrain, terrainStep = nextStep, overlay = None)
        }
      }
    }
  }

  private def terrainStepForTerrain(terrain: String, climate: String, fallback: Int): Int = {
    val names = terrainSteps.map { case (warm, cold) => if (climate == "Warm") warm else cold }
    names.indexOf(terrain) match {
      case -1  => fallback
      case idx => idx
    }
  }

  private def riverOverlayForNeighborDirs(
      directions: List[String],
      hex: HexCell,
      axisByCoord: Map[(Int, Int), String],
      phaseByCoord: Map[(Int, Int), Boolean]
  ): (String, String) = {
    val dirSet = directions.toSet
    val corner = if (dirSet.size == 2) cornerOrientationForDirs(dirSet) else ""
    if (corner.nonEmpty) ("RiverCorner", corner)
    else {
      val axis = axisByCoord.getOrElse((hex.column, hex.row), riverAxisForDirs(directions))
      val reverse = phaseByCoord.getOrElse((hex.column, hex.row), false)
      val orientation = if (reverse) s"$axis-REV" else axis
      ("River", orientation)
    }
  }

  private def cornerOrientationForDirs(dirSet: Set[String]): String = {
    val orderedPairs = List(
      ("N", "NE", "N-NE"),
      ("NE", "SE", "NE-SE"),
      ("SE", "S", "SE-S"),
      ("S", "SW", "S-SW"),
      ("SW", "NW", "SW-NW"),
      ("NW", "N", "NW-N")
    )
    orderedPairs
      .collectFirst {
        case (a, b, orientation) if dirSet.contains(a) && dirSet.contains(b) => orientation
      }
      .getOrElse("")
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
      allowOverlay: Boolean = true
  ): HexCell = {
    val poi =
      if (rollDie(6) == 1) {
        val location = poiLocations(rollDie(20) - 1)
        val development = poiDevelopments(rollDie(20) - 1)
        val cataclysm =
          if (development.startsWith("Disaster!")) Some(cataclysms(rollDie(8) - 1)) else None
        val (offsetX, offsetY) = randomPoiOffset()
        Some(
          HexPointOfInterest(
            id = nextPoiId,
            location = location,
            development = development,
            cataclysm = cataclysm,
            offsetX = offsetX,
            offsetY = offsetY
          )
        )
      } else None
    val baseTerrain = terrainName(step, climate)
    val terrain =
      if (baseTerrain != "Ocean" && baseTerrain != "River/coast" && rollDie(10) == 1) "River/coast"
      else baseTerrain
    val terrainStep = if (terrain != baseTerrain) terrainStepForTerrain(terrain, climate, step) else step
    val overlay =
      if (allowOverlay && terrain == "River/coast") {
        val baseTerrain = randomLandTerrain(climate)
        val isRiver = rollDie(2) == 1
        val orientation =
          if (isRiver) riverOrientations(rollDie(riverOrientations.size) - 1)
          else coastOrientations(rollDie(coastOrientations.size) - 1)
        val kind = if (isRiver) "River" else "Coast"
        Some(HexOverlay(kind = kind, orientation = orientation, baseTerrain = baseTerrain))
      } else None
    HexCell(
      id = hexId,
      column = column,
      row = row,
      terrain = terrain,
      terrainStep = terrainStep,
      pointOfInterest = poi,
      overlay = overlay
    )
  }

  private def layoutFor(hexes: List[HexCell]): HexMapLayout = {
    val columns = hexes.map(_.column)
    val rows = hexes.map(_.row)
    val width = columns.max - columns.min + 1
    val height = rows.max - rows.min + 1
    HexMapLayout(columns = width, rows = height)
  }

  private def neighborCoords(column: Int, row: Int, direction: String): (Int, Int) = {
    val dir = direction.trim.toUpperCase
    val odd = row % 2 != 0
    (dir, odd) match {
      case ("N", _)      => (column, row - 1)
      case ("S", _)      => (column, row + 1)
      case ("NE", false) => (column + 1, row - 1)
      case ("SE", false) => (column + 1, row)
      case ("SW", false) => (column - 1, row)
      case ("NW", false) => (column - 1, row - 1)
      case ("NE", true)  => (column + 1, row)
      case ("SE", true)  => (column + 1, row + 1)
      case ("SW", true)  => (column - 1, row + 1)
      case ("NW", true)  => (column - 1, row)
      case _             => throw new IllegalArgumentException("Direction must be N, NE, SE, S, SW, or NW")
    }
  }

  private def moveCoords(column: Int, row: Int, direction: String): (Int, Int) = {
    val dir = direction.trim.toUpperCase
    val odd = row % 2 != 0
    (dir, odd) match {
      case ("E", _)      => (column + 1, row)
      case ("W", _)      => (column - 1, row)
      case ("NE", false) => (column, row - 1)
      case ("SE", false) => (column, row + 1)
      case ("SW", false) => (column - 1, row + 1)
      case ("NW", false) => (column - 1, row - 1)
      case ("NE", true)  => (column + 1, row - 1)
      case ("SE", true)  => (column + 1, row + 1)
      case ("SW", true)  => (column, row + 1)
      case ("NW", true)  => (column, row - 1)
      case _             => throw new IllegalArgumentException("Direction must be NW, NE, E, SE, SW, or W")
    }
  }

  private def neighborOffsetsFor(row: Int): List[(Int, Int)] = {
    val odd = row % 2 != 0
    if (odd) {
      List(
        (1, 0),
        (-1, 0),
        (1, -1),
        (0, -1),
        (1, 1),
        (0, 1)
      )
    } else {
      List(
        (1, 0),
        (-1, 0),
        (0, -1),
        (-1, -1),
        (0, 1),
        (-1, 1)
      )
    }
  }

  private def renderHexMapImage(
      map: HexMap,
      includeLegend: Boolean,
      transparentBackground: Boolean,
      tightCrop: Boolean
  ): BufferedImage = {
    val (minCol, maxCol, minRow, maxRow) = boundsFor(map.hexes)
    val columns = maxCol - minCol + 1
    val rows = maxRow - minRow + 1
    val size = hexSizeFor(columns, rows)
    val hexWidth = sqrt3 * size
    val mapPixelWidth = hexWidth * columns + hexWidth / 2.0
    val mapPixelHeight = (2.0 * size) + (rows - 1) * size * 1.5
    val canvasWidth = if (tightCrop) Math.ceil(mapPixelWidth).toInt else pageWidth
    val canvasHeight = if (tightCrop) Math.ceil(mapPixelHeight).toInt else pageHeight
    val image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      if (transparentBackground) {
        g.setComposite(AlphaComposite.Clear)
        g.fillRect(0, 0, canvasWidth, canvasHeight)
        g.setComposite(AlphaComposite.SrcOver)
      } else {
        paintBackground(g)
      }
      val startX =
        if (tightCrop) 0.0 else pageMargin + (mapWidth - mapPixelWidth) / 2.0
      val startY =
        if (tightCrop) 0.0 else pageMargin + (mapHeight - mapPixelHeight) / 2.0

      map.hexes.foreach { hex =>
        val colOffset = hex.column - minCol
        val rowOffset = hex.row - minRow
        val isOddRow = hex.row % 2 != 0
        val centerX = startX + hexWidth * (colOffset + (if (isOddRow) 0.5 else 0.0)) + hexWidth / 2.0
        val centerY = startY + size * 1.5 * rowOffset + size
        val polygon = hexPolygon(centerX, centerY, size)
        val baseTerrain = hex.overlay.map(_.baseTerrain).getOrElse(hex.terrain)
        drawTerrainTexture(g, polygon, baseTerrain)
        hex.overlay.foreach { overlay =>
          drawTerrainOverlay(g, polygon, overlay)
        }
        g.setColor(new Color(60, 55, 50))
        g.setStroke(new BasicStroke(2.0f))
        g.draw(polygon)
        if (hex.column == map.activeColumn && hex.row == map.activeRow) {
          g.setColor(activeHexColor)
          g.setStroke(activeHexStroke)
          g.draw(polygon)
        }
        drawPoiMarker(g, hex, centerX, centerY, size)
      }

      if (includeLegend) {
        drawHexMapLegend(g, map)
      }
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

  private def loadImage(path: String, trim: Boolean): Option[BufferedImage] = {
    val stream = Option(getClass.getClassLoader.getResourceAsStream(path))
    stream.flatMap { input =>
      try Option(ImageIO.read(input)).map(image => if (trim) trimTransparent(image) else image)
      finally input.close()
    }
  }

  private def trimTransparent(image: BufferedImage): BufferedImage = {
    val width = image.getWidth
    val height = image.getHeight
    var minX = width
    var minY = height
    var maxX = -1
    var maxY = -1
    var y = 0
    while (y < height) {
      var x = 0
      while (x < width) {
        val alpha = (image.getRGB(x, y) >> 24) & 0xff
        if (alpha != 0) {
          if (x < minX) minX = x
          if (y < minY) minY = y
          if (x > maxX) maxX = x
          if (y > maxY) maxY = y
        }
        x += 1
      }
      y += 1
    }
    if (maxX < minX || maxY < minY) image
    else {
      val trimmed = image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1)
      val copy = new BufferedImage(trimmed.getWidth, trimmed.getHeight, BufferedImage.TYPE_INT_ARGB)
      val g = copy.createGraphics()
      try g.drawImage(trimmed, 0, 0, null)
      finally g.dispose()
      copy
    }
  }

  private def drawTerrainTexture(g: Graphics2D, polygon: java.awt.Polygon, terrain: String): Boolean = {
    terrainTextureCache.get(terrain) match {
      case Some(texture) =>
        val bounds = polygon.getBounds
        val oldClip = g.getClip
        g.setClip(polygon)
        if (texture.getColorModel.hasAlpha) {
          g.setColor(terrainFillColors.getOrElse(terrain, new Color(190, 190, 190)))
          g.fillPolygon(polygon)
        }
        g.drawImage(texture, bounds.x, bounds.y, bounds.width, bounds.height, null)
        g.setClip(oldClip)
        true
      case None =>
        false
    }
  }

  private def drawTerrainOverlay(
      g: Graphics2D,
      polygon: java.awt.Polygon,
      overlay: HexOverlay
  ): Unit = {
    overlayTextureCache.get(overlay.kind).foreach { image =>
      val bounds = polygon.getBounds
      val oldClip = g.getClip
      val oldTransform = g.getTransform
      val rotation = overlayRotationDegrees(overlay)
      val centerX = bounds.getCenterX
      val centerY = bounds.getCenterY
      val baseScale = Math.max(bounds.getWidth / image.getWidth, bounds.getHeight / image.getHeight)
      val scale = baseScale * overlayScaleMultiplier(overlay.kind) * overlayOrientationScale(overlay)
      g.setClip(polygon)
      val transform = new AffineTransform(oldTransform)
      transform.translate(centerX, centerY)
      transform.rotate(Math.toRadians(rotation))
      transform.scale(scale, scale)
      transform.translate(-image.getWidth / 2.0, -image.getHeight / 2.0)
      g.setTransform(transform)
      g.drawImage(image, 0, 0, null)
      g.setTransform(oldTransform)
      g.setClip(oldClip)
    }
  }

  private def overlayRotationDegrees(overlay: HexOverlay): Double =
    overlay.kind match {
      case "River" =>
        val (axis, reversed) =
          if (overlay.orientation.endsWith("-REV")) (overlay.orientation.stripSuffix("-REV"), true)
          else (overlay.orientation, false)
        val baseRotation = axis match {
          case "N-S"   => 0.0
          case "E-W"   => 90.0
          case "NE-SW" => -60.0
          case "NW-SE" => 60.0
          case _       => 0.0
        }
        if (reversed) baseRotation + 180.0 else baseRotation
      case "RiverCorner" =>
        overlay.orientation match {
          case "N-NE"  => 0.0
          case "NE-SE" => 60.0
          case "SE-S"  => 120.0
          case "S-SW"  => 180.0
          case "SW-NW" => 240.0
          case "NW-N"  => 300.0
          case _       => 0.0
        }
      case "Coast" =>
        val baseOffset = 90.0
        overlay.orientation match {
          case "N"  => 0.0 + baseOffset
          case "NE" => 60.0 + baseOffset
          case "SE" => 120.0 + baseOffset
          case "S"  => 180.0 + baseOffset
          case "SW" => 240.0 + baseOffset
          case "NW" => 300.0 + baseOffset
          case _    => baseOffset
        }
      case _ => 0.0
    }

  private def overlayScaleMultiplier(kind: String): Double =
    kind match {
      case "River"       => 1.28
      case "RiverCorner" => 1.28
      case "Coast"       => 1.22
      case _             => 1.0
    }

  private def overlayOrientationScale(overlay: HexOverlay): Double =
    overlay.kind match {
      case "River" =>
        val orientation =
          if (overlay.orientation.endsWith("-REV")) overlay.orientation.stripSuffix("-REV")
          else overlay.orientation
        orientation match {
          case "N-S" | "E-W"     => 1.10
          case "NE-SW" | "NW-SE" => 1.12
          case _                 => 1.0
        }
      case "RiverCorner" => 1.10
      case _             => 1.0
    }

  private def drawPoiMarker(g: Graphics2D, hex: HexCell, centerX: Double, centerY: Double, size: Double): Unit = {
    hex.pointOfInterest.foreach { poi =>
      val poiSize = Math.max(12, (size * 0.28).toInt)
      val poiCenterX = centerX + (poi.offsetX * size)
      val poiCenterY = centerY + (poi.offsetY * size)
      val iconX = (poiCenterX - poiSize / 2.0).toInt
      val iconY = (poiCenterY - poiSize / 2.0).toInt
      val style = poiMarkerStyle(poi.location, poi.development)
      g.setColor(style.color)
      style.draw(g, iconX, iconY, poiSize)
    }
  }

  private def randomPoiOffset(): (Double, Double) = {
    val size = poiInsetFactor
    val halfWidth = sqrt3 * size / 2.0
    val minX = -halfWidth
    val maxX = halfWidth
    val minY = -size
    val maxY = size
    var x = 0.0
    var y = 0.0
    var attempts = 0
    while (attempts < 40) {
      x = rng.nextDouble(minX, maxX)
      y = rng.nextDouble(minY, maxY)
      if (isInsideHex(x, y, size)) return (x, y)
      attempts += 1
    }
    (0.0, 0.0)
  }

  private def isInsideHex(x: Double, y: Double, size: Double): Boolean = {
    val ax = Math.abs(x)
    val ay = Math.abs(y)
    ax <= (sqrt3 * size / 2.0) && ay <= size && (ax / sqrt3 + ay) <= size
  }

  private final case class PoiMarkerStyle(color: Color, draw: (Graphics2D, Int, Int, Int) => Unit)

  private def poiMarkerStyle(location: String, development: String): PoiMarkerStyle = {
    val loc = location.toLowerCase
    val dev = development.toLowerCase
    if (loc.contains("temple") || loc.contains("holy shrine")) {
      PoiMarkerStyle(new Color(200, 160, 70), drawCross)
    } else if (loc.contains("barrow") || loc.contains("dolmens")) {
      PoiMarkerStyle(new Color(120, 120, 120), drawCairn)
    } else if (loc.contains("village") || loc.contains("town") || loc.contains("city")) {
      PoiMarkerStyle(new Color(35, 30, 25), drawHouse)
    } else if (loc.contains("keep") || loc.contains("tower")) {
      PoiMarkerStyle(new Color(80, 80, 80), drawTower)
    } else if (loc.contains("ravine")) {
      PoiMarkerStyle(new Color(120, 90, 60), drawRavine)
    } else if (loc.contains("cave")) {
      PoiMarkerStyle(new Color(90, 70, 55), drawCave)
    } else if (loc.contains("natural landmark")) {
      PoiMarkerStyle(new Color(110, 85, 60), drawRock)
    } else if (loc.contains("monster")) {
      PoiMarkerStyle(new Color(160, 40, 30), drawStar)
    } else if (loc.contains("hermit")) {
      PoiMarkerStyle(new Color(100, 70, 50), drawHut)
    } else if (loc.contains("barbarian")) {
      PoiMarkerStyle(new Color(180, 110, 50), drawTent)
    } else if (dev.contains("disaster") || dev.contains("cataclysm")) {
      PoiMarkerStyle(new Color(190, 60, 40), drawFlame)
    } else if (dev.contains("oracle") || dev.contains("cult") || dev.contains("wizards")) {
      PoiMarkerStyle(new Color(120, 80, 150), drawEye)
    } else if (dev.contains("dragon") || dev.contains("treasure") || dev.contains("plane")) {
      PoiMarkerStyle(new Color(50, 130, 140), drawGem)
    } else {
      PoiMarkerStyle(new Color(150, 30, 30), drawDot)
    }
  }

  private def drawDot(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    g.fillOval(x, y, size, size)
  }

  private def drawCross(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val bar = Math.max(2, size / 5)
    g.fillRect(x + size / 2 - bar / 2, y + size / 6, bar, size * 2 / 3)
    g.fillRect(x + size / 6, y + size / 2 - bar / 2, size * 2 / 3, bar)
  }

  private def drawCairn(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val r = size / 5
    g.fillOval(x + size / 2 - r, y + size / 6, r * 2, r * 2)
    g.fillOval(x + size / 2 - (r + 2), y + size / 2 - r, (r + 2) * 2, (r + 2) * 2)
    g.fillOval(x + size / 2 - (r + 4), y + size * 2 / 3 - r, (r + 4) * 2, (r + 4) * 2)
  }

  private def drawHouse(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val roof = new java.awt.Polygon(
      Array(x + size / 2, x + size - 2, x + 2),
      Array(y + 2, y + size / 2, y + size / 2),
      3
    )
    g.fillPolygon(roof)
    g.fillRect(x + size / 5, y + size / 2, size * 3 / 5, size / 2)
  }

  private def drawTower(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    g.fillRect(x + size / 3, y + size / 5, size / 3, size * 3 / 5)
    g.fillRect(x + size / 4, y + size / 5 - size / 10, size / 2, size / 6)
  }

  private def drawRock(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val rock = new java.awt.Polygon(
      Array(x + size / 5, x + size * 4 / 5, x + size - 2, x + size / 3),
      Array(y + size * 2 / 3, y + size * 2 / 3, y + size / 3, y + size / 5),
      4
    )
    g.fillPolygon(rock)
  }

  private def drawRavine(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    g.setStroke(new BasicStroke(Math.max(2.0f, size / 10.0f)))
    g.drawLine(x + size / 3, y + size / 6, x + size * 2 / 3, y + size * 5 / 6)
  }

  private def drawCave(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    g.fillOval(x + size / 4, y + size / 3, size / 2, size / 2)
  }

  private def drawStar(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val r = size / 2
    val cx = x + r
    val cy = y + r
    val points = new java.awt.Polygon()
    var i = 0
    while (i < 10) {
      val angle = Math.toRadians(-90 + i * 36)
      val radius = if (i % 2 == 0) r else r / 2
      points.addPoint((cx + Math.cos(angle) * radius).toInt, (cy + Math.sin(angle) * radius).toInt)
      i += 1
    }
    g.fillPolygon(points)
  }

  private def drawHut(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    g.fillOval(x + size / 4, y + size / 2, size / 2, size / 3)
    val roof = new java.awt.Polygon(
      Array(x + size / 2, x + size * 3 / 4, x + size / 4),
      Array(y + size / 3, y + size / 2, y + size / 2),
      3
    )
    g.fillPolygon(roof)
  }

  private def drawTent(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val tent = new java.awt.Polygon(
      Array(x + size / 2, x + size - 2, x + 2),
      Array(y + 2, y + size - 2, y + size - 2),
      3
    )
    g.fillPolygon(tent)
  }

  private def drawFlame(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val flame = new java.awt.Polygon(
      Array(x + size / 2, x + size * 3 / 4, x + size * 2 / 3, x + size / 2, x + size / 3, x + size / 4),
      Array(y + 2, y + size / 3, y + size * 2 / 3, y + size - 2, y + size * 2 / 3, y + size / 3),
      6
    )
    g.fillPolygon(flame)
  }

  private def drawEye(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    g.setStroke(new BasicStroke(Math.max(2.0f, size / 12.0f)))
    g.drawOval(x + size / 6, y + size / 3, size * 2 / 3, size / 3)
    g.fillOval(x + size / 2 - size / 10, y + size / 2 - size / 10, size / 5, size / 5)
  }

  private def drawGem(g: Graphics2D, x: Int, y: Int, size: Int): Unit = {
    val gem = new java.awt.Polygon(
      Array(x + size / 2, x + size - 2, x + size / 2, x + 2),
      Array(y + 2, y + size / 2, y + size - 2, y + size / 2),
      4
    )
    g.fillPolygon(gem)
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
    g.drawString("POI: icon", x + 16, y + 94)
    val legendStartY = y + 120
    terrainNames.zipWithIndex.foreach { case (terrain, idx) =>
      val rowY = legendStartY + idx * rowHeight
      val hexCenterX = x + 28
      val hexCenterY = rowY + 8
      val polygon = hexPolygon(hexCenterX, hexCenterY, 10)
      val baseTerrain = legendBaseTerrain(terrain, map.climate)
      drawTerrainTexture(g, polygon, baseTerrain)
      if (terrain == "River/coast") {
        val overlay = HexOverlay(
          kind = "Coast",
          orientation = "N",
          baseTerrain = baseTerrain
        )
        drawTerrainOverlay(g, polygon, overlay)
      }
      g.setColor(new Color(60, 55, 50))
      g.draw(polygon)
      g.setColor(new Color(40, 35, 30))
      g.setFont(new Font("Serif", Font.PLAIN, 13))
      g.drawString(terrain, x + 50, rowY + 12)
    }
  }

  private def terrainNamesForClimate(climate: String): List[String] = {
    val names = terrainSteps.map { case (warm, cold) => if (climate == "Warm") warm else cold }
    names.toList
  }

  private def randomLandTerrain(climate: String): String = {
    val land = terrainSteps
      .map { case (warm, cold) => if (climate == "Warm") warm else cold }
      .filterNot(name => name == "River/coast" || name == "Ocean")
    land(rollDie(land.size) - 1)
  }

  private def legendBaseTerrain(terrain: String, climate: String): String =
    if (terrain == "River/coast") {
      if (climate == "Warm") "Grassland" else "Taiga"
    } else terrain
}

object HexMapServer {
  val live: ZLayer[Any, Nothing, HexMapServer] =
    ZLayer.succeed(HexMapServer())
}
