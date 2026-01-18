package servers

import models._
import zio._

import java.awt.{BasicStroke, Color, Font, GradientPaint, RenderingHints}
import java.awt.geom.{Ellipse2D, Path2D}
import java.awt.image.BufferedImage
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.PDPageContentStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom
import javax.imageio.ImageIO
import org.locationtech.jts.geom.{Coordinate, Envelope, Geometry, GeometryFactory, LineString, Polygon}
import org.locationtech.jts.geom.util.AffineTransformation
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder
import scala.jdk.CollectionConverters._
import scala.util.Random

private[servers] final case class PoiTemplate(
  name: String,
  kind: String,
  tavernQuality: Option[String] = None,
  shopQuality: Option[String] = None,
  shopTypeOverride: Option[String] = None,
)

private final case class RoadCurve(start: Point, end: Point, isMain: Boolean, line: LineString)

final case class SettlementServer(
  nameServer: NameServer,
  raceServer: RaceServer,
  personalityServer: PersonalityServer,
  backgroundServer: BackgroundServer,
  npcQualityServer: NpcQualityServer,
  settlementNameServer: SettlementNameServer,
) {
  private val pageWidth    = 1275
  private val pageHeight   = 1650
  private val pageMargin   = 50
  private val legendHeight = 350
  private val mapWidth     = pageWidth - (pageMargin * 2)
  private val mapHeight    = pageHeight - (pageMargin * 2) - legendHeight
  private val mapPadding   = 90
  private val gridSize     = 50

  private val settlementTypes = List(
    SettlementType("Village", 3, 4),
    SettlementType("Town", 4, 4),
    SettlementType("City", 6, 6),
    SettlementType("Metropolis", 8, 8),
  )

  private val districtTypes = List(
    "Slums",
    "Low district",
    "Artisan district",
    "Market",
    "High District",
    "Temple district",
    "University district",
    "Castle district",
  )
  private val districtPalette = List(
    new Color(198, 132, 118, 150),
    new Color(155, 181, 196, 150),
    new Color(173, 196, 156, 150),
    new Color(214, 190, 127, 150),
    new Color(178, 156, 196, 150),
    new Color(206, 170, 140, 150),
    new Color(165, 196, 178, 150),
    new Color(201, 167, 185, 150),
  )

  private val alignments = List(
    "Lawful",
    "Lawful",
    "Lawful",
    "Neutral",
    "Neutral",
    "Chaotic",
  )

  private val npcAppearanceCategory = "appearance"
  private val npcDoesCategory       = "does"
  private val npcSecretCategory     = "secret"
  private val npcAgeCategory        = "age"
  private val npcWealthCategory     = "wealth"

  private val tavernNames = List(
    ("The Crimson", "Rat", "High-stakes gambling"),
    ("The Dancing", "Wench", "Illicit poison sales"),
    ("The Dog &", "Lantern", "Wizard patrons"),
    ("The Rusty", "Eel", "Cult rituals in the basement"),
    ("The Demon's", "Goblet", "Rare food and drinks"),
    ("The Singing", "Trident", "Dancing contests"),
    ("The Boar &", "Candle", "Violent brawls"),
    ("The Silver", "Dagger", "Ancient tunnels in the cellar"),
    ("The Filthy", "Wheel", "Thugs for hire"),
    ("The Captain's", "Pig", "Thieves' Guild spies"),
    ("The Jolly", "Snake", "Hostility toward spellcasters"),
    ("The Wise", "Camel", "City Watch patrons"),
    ("Cloak &", "Dragon", "Underground pit fighting"),
    ("The Royal", "Axe", "Famous bard performances"),
    ("The Gilded", "Bell", "Treasonous meetings"),
    ("The Blade &", "Tankard", "Ban on all weapons"),
    ("The Drunken", "Shield", "Hostility toward non-regulars"),
    ("Cup & Blade", "Exotic", "Taxidermy collection"),
    ("The Jeweled", "Anvil", "Pirate and smuggler patrons"),
    ("The Frog &", "Bard", "Drinking contests"),
  )

  private val drinks = List(
    Drink("Barnacle grog", "1 cp", "DC 9 Constitution check or blind 1 hour"),
    Drink("Watered-down swill", "3 cp", "Toxic, -1 Constitution 1 hour"),
    Drink("Vinegary wine", "5 cp", "Stains teeth purple, -1 Charisma 1 hour"),
    Drink("Stale ale", "5 cp", "Dulls the senses, -1 Wisdom 1 hour"),
    Drink("Clear spirits", "1 sp", "Burns, ends 1 bad effect of another drink"),
    Drink("House ale", "2 sp", "Crisp and clean, first mug is free"),
    Drink("Autumn mead", "3 sp", "Floral, doubles effect of next drink"),
    Drink("Halfling summer wine", "5 sp", "Sparkling, +1 Charisma 1 hour"),
    Drink("Elvish brandy", "5 sp", "Spiced, +1 Intelligence 1 hour"),
    Drink("Dwarvish gold ale", "5 sp", "Icy cold, regain 1d4 HP per mug"),
    Drink("Aged royal wine", "2 gp", "Smooth and rich, +1 Wisdom 1 hour"),
    Drink("Van Dinkle whiskey", "20 gp a sip", "Only 5 bottles made, +1 XP"),
  )

  private val poorFood = List(
    Food("Boiled cabbage", "1d4 cp"),
    Food("Dates and olives", "1d4 cp"),
    Food("Goat stew", "1d4 cp"),
    Food("Pickled eggs", "1d4 cp"),
    Food("Cheese and bread", "1d4 cp"),
    Food("Hearty broth", "1d4 cp"),
    Food("Meat pastry", "1d4 cp"),
    Food("Mushroom kebab", "1d4 cp"),
    Food("Roasted pigeon", "1d4 cp"),
    Food("Garlic flatbread", "1d4 cp"),
    Food("Turkey leg", "1d4 cp"),
    Food("Rat-on-a-stick", "1d4 cp"),
  )

  private val standardFood = List(
    Food("Alligator steak", "1d6 sp"),
    Food("Rosemary ham", "1d6 sp"),
    Food("Raw flailfish", "1d6 sp"),
    Food("Seared venison", "1d6 sp"),
    Food("Buttered ostrich", "1d6 sp"),
    Food("Spicy veal curry", "1d6 sp"),
    Food("Salted frog legs", "1d6 sp"),
    Food("Herbed snails", "1d6 sp"),
    Food("Grilled tiger eel", "1d6 sp"),
    Food("Spit-roasted boar", "1d6 sp"),
    Food("Saffron duck neck", "1d6 sp"),
    Food("Crimson pudding", "1d6 sp"),
  )

  private val wealthyFood = List(
    Food("Fried basilisk eyes", "1d8 gp"),
    Food("Giant snake filet", "1d8 gp"),
    Food("Griffon eggs", "1d8 gp"),
    Food("Candied scarabs", "1d8 gp"),
    Food("Baked troll bones", "1d8 gp"),
    Food("Cockatrice wings", "1d8 gp"),
    Food("Crispy silkworms", "1d8 gp"),
    Food("Roasted stingbat", "1d8 gp"),
    Food("Dire lobster tail", "1d8 gp"),
    Food("Wyvern tongue", "1d8 gp"),
    Food("Shrieking seaweed", "1d8 gp"),
    Food("Dragon shanks", "1d8 gp"),
  )

  private val poorShopTypes = List(
    "Filthy bakery",
    "Used adventuring gear",
    "Dead body collector",
    "Pawn shop/fence",
    "Moneylender",
    "Manure collector",
    "Tannery",
    "Back-alley chirurgeon",
    "Ratcatcher",
    "Fishmonger",
    "Gambling house",
    "Drug den",
  )

  private val standardShopTypes = List(
    "Brewer",
    "Butcher",
    "Tailor",
    "Common blacksmith",
    "Adventuring gear",
    "Leatherworker",
    "Shipwright/carpenter",
    "Stonemason",
    "Herald/town crier",
    "Livestock",
  )

  private val wealthyShopTypes = List(
    "Fine tailor",
    "Glassblower",
    "Jeweler",
    "Apothecary",
    "Artist",
    "Scribe",
    "Guildhall",
    "Goldsmith",
    "Master blacksmith",
    "Antiques and curios",
  )

  private val shopNames = List(
    ("Fink &", "Sons", "Ancient, beloved owner"),
    ("Imperial", "Toad", "Buying anything of value"),
    ("The Stout", "Hammer", "Charging non-regulars extra"),
    ("Rose's", "Commodities", "Being a Thieves' Guild front"),
    ("The King's", "Daughters", "Resident cat, Crumpet"),
    ("Fox &", "Sundries", "Password required to enter"),
    ("Noble", "Castle", "Free ale with a purchase"),
    ("Sylvia's", "Finery", "Heavily armed bodyguards"),
    ("Sunrise", "Oddments", "Paying top coin for curios"),
    ("The Corner", "Beetle", "Secret room behind shelf"),
    ("Grigor's", "Storehouse", "Fencing illicit goods"),
    ("Royal", "Keep", "Ringing a gong at every sale"),
    ("Crown &", "Coins", "Goods from distant lands"),
    ("Ralina's", "Hearth", "Shoddy and cheap items"),
    ("The Village", "Wheel", "Accusing customers of theft"),
    ("Golden", "Wares", "All goods are dyed blue"),
    ("Boot &", "Market", "Owner's talking parrot"),
    ("Marvolo's", "Lantern", "Famous bronze imp statue"),
    ("The Merry", "Vendibles", "Being haunted"),
    ("The Jade", "Stocks", "Aggressive rodent problem"),
  )

  private val interestingCustomers = Vector(
    Vector("Odd wizard", "1d10 children", "Cackling crone", "Loud dwarf"),
    Vector("Nervous elf", "Shifty thug", "Town guard", "1d4 priests"),
    Vector("Goblin pirate", "Cowled mage", "Half-orc knight", "Drunk man"),
    Vector("Staring child", "Rival crawlers", "Glum halfling", "Pickpocket"),
  )

  private def rng: ThreadLocalRandom = ThreadLocalRandom.current()

  private def rollDie(sides: Int): Int =
    rng.nextInt(1, sides + 1)

  private def roll2d6: Int =
    rollDie(6) + rollDie(6)

  private def rollAlignment: String =
    alignments(rng.nextInt(alignments.length))

  private def pickOne[A](values: List[A]): Option[A] =
    if (values.isEmpty) None else Some(values(rng.nextInt(values.length)))

  private def shuffleWith[A](values: List[A], rand: Random): List[A] = {
    val buffer = scala.collection.mutable.ArrayBuffer(values: _*)
    for (i <- buffer.indices.reverse) {
      val j = rand.nextInt(i + 1)
      val tmp = buffer(i)
      buffer(i) = buffer(j)
      buffer(j) = tmp
    }
    buffer.toList
  }

  private def districtTypeForRoll(roll: Int): String =
    districtTypes(math.max(0, math.min(districtTypes.length - 1, roll - 1)))

  private def settlementTypeForRoll(roll: Int): SettlementType =
    roll match {
      case 1       => settlementTypes(0)
      case 2 | 3   => settlementTypes(1)
      case 4 | 5   => settlementTypes(2)
      case _       => settlementTypes(3)
    }

  private def poiTemplateFor(districtType: String): List[(Int, Int, PoiTemplate)] =
    districtType match {
      case "Slums" =>
        List(
          (1, 1, PoiTemplate("Seedy flophouse", "feature")),
          (2, 3, PoiTemplate("Poor tavern", "tavern", tavernQuality = Some("Poor"))),
          (4, 4, PoiTemplate("Criminal safehouse", "feature")),
          (5, 5, PoiTemplate("Poor shop", "shop", shopQuality = Some("Poor"))),
          (6, 6, PoiTemplate("Witch/warlock's hovel", "feature")),
        )
      case "Low district" =>
        List(
          (1, 1, PoiTemplate("Graveyard", "feature")),
          (2, 3, PoiTemplate("Poor tavern", "tavern", tavernQuality = Some("Poor"))),
          (4, 4, PoiTemplate("Poor shop", "shop", shopQuality = Some("Poor"))),
          (5, 5, PoiTemplate("Standard shop", "shop", shopQuality = Some("Standard"))),
          (6, 6, PoiTemplate("Warehouses/sheds", "feature")),
        )
      case "Artisan district" =>
        List(
          (1, 1, PoiTemplate("Stocks and pillories", "feature")),
          (2, 3, PoiTemplate("Modest temple", "feature")),
          (4, 5, PoiTemplate("Standard tavern", "tavern", tavernQuality = Some("Standard"))),
          (6, 6, PoiTemplate("Wealthy shop", "shop", shopQuality = Some("Wealthy"))),
        )
      case "Market" =>
        List(
          (1, 1, PoiTemplate("Fortune teller", "feature")),
          (2, 4, PoiTemplate("Rare and exotic goods", "feature")),
          (5, 5, PoiTemplate("Apothecary", "shop", shopQuality = Some("Standard"), shopTypeOverride = Some("Apothecary"))),
          (6, 6, PoiTemplate("Illicit black market", "feature")),
        )
      case "High District" =>
        List(
          (1, 1, PoiTemplate("Guildhouse", "feature")),
          (2, 3, PoiTemplate("Wealthy tavern", "tavern", tavernQuality = Some("Wealthy"))),
          (4, 4, PoiTemplate("Manor house", "feature")),
          (5, 5, PoiTemplate("Wealthy shop", "shop", shopQuality = Some("Wealthy"))),
          (6, 6, PoiTemplate("City Watch outpost", "feature")),
        )
      case "Temple district" =>
        List(
          (1, 1, PoiTemplate("Ruined temple", "feature")),
          (2, 3, PoiTemplate("Minor deity's chapel", "feature")),
          (4, 4, PoiTemplate("Forbidden shrine", "feature")),
          (5, 5, PoiTemplate("Major god's temple", "feature")),
          (6, 6, PoiTemplate("Revered holy site", "feature")),
        )
      case "University district" =>
        List(
          (1, 1, PoiTemplate("Library", "feature")),
          (2, 3, PoiTemplate("Lecture hall", "feature")),
          (4, 5, PoiTemplate("Standard tavern", "tavern", tavernQuality = Some("Standard"))),
          (6, 6, PoiTemplate("Wizard's tower", "feature")),
        )
      case "Castle district" =>
        List(
          (1, 1, PoiTemplate("Royal bathhouse", "feature")),
          (2, 3, PoiTemplate("City Watch's garrison", "feature")),
          (4, 5, PoiTemplate("Theater or coliseum", "feature")),
          (6, 6, PoiTemplate("Royal castle", "feature")),
        )
      case _ =>
        List((1, 6, PoiTemplate("Unusual landmark", "feature")))
    }

  private def rollPoiTemplate(districtType: String): PoiTemplate = {
    val roll  = rollDie(6)
    val table = poiTemplateFor(districtType)
    table.collectFirst { case (min, max, poi) if roll >= min && roll <= max => poi }
      .getOrElse(table.head._3)
  }

  private def buildTavern(quality: String): Tavern = {
    val (prefix, suffix, knownFor) = tavernNames(rollDie(20) - 1)
    val name                       = s"$prefix $suffix"

    val drinkRolls = quality match {
      case "Poor"     => List.fill(2)(rollDie(6))
      case "Standard" => List.fill(3)(roll2d6)
      case _          => List.fill(4)(rollDie(12))
    }
    val drinksPicked = drinkRolls.map { roll =>
      val idx = math.max(1, math.min(12, roll)) - 1
      drinks(idx)
    }

    val foodPicked = quality match {
      case "Poor" =>
        List.fill(3)(poorFood(rollDie(12) - 1))
      case "Standard" =>
        List(poorFood(rollDie(12) - 1)) ++ List.fill(2)(standardFood(rollDie(12) - 1))
      case _ =>
        List.fill(2)(standardFood(rollDie(12) - 1)) ++ List.fill(2)(wealthyFood(rollDie(12) - 1))
    }

    Tavern(name, knownFor, drinksPicked, foodPicked)
  }

  private def buildShop(quality: String, overrideType: Option[String]): Shop = {
    val (prefix, suffix, knownFor) = shopNames(rollDie(20) - 1)
    val name                       = s"$prefix $suffix"
    val shopType = overrideType.getOrElse {
      quality match {
        case "Poor"     => poorShopTypes(rollDie(12) - 1)
        case "Standard" => standardShopTypes(rollDie(10) - 1)
        case _          => wealthyShopTypes(rollDie(10) - 1)
      }
    }
    val customer = {
      val row = rollDie(4) - 1
      val col = rollDie(4) - 1
      interestingCustomers(row)(col)
    }
    Shop(name, shopType, knownFor, customer)
  }

  private def buildPoints(count: Int, mask: Polygon): List[Point] = {
    val minDistance = 140
    val envelope    = mask.getEnvelopeInternal
    (1 to count).foldLeft(List.empty[Point]) { (acc, _) =>
      var point: Option[Point] = None
      var attempts             = 0
      while (point.isEmpty && attempts < 80) {
        attempts += 1
        val candidate = Point(
          (envelope.getMinX + rng.nextDouble() * envelope.getWidth).toInt,
          (envelope.getMinY + rng.nextDouble() * envelope.getHeight).toInt,
        )
        val candidateGeom = geometryFactory.createPoint(toCoordinate(candidate))
        val insideMask    = mask.contains(candidateGeom)
        val farEnough = acc.forall(existing => distance(existing, candidate) >= minDistance)
        if (insideMask && (farEnough || attempts > 20)) point = Some(candidate)
      }
      acc :+ point.getOrElse(Point(mapWidth / 2, mapHeight / 2))
    }
  }

  private def distance(a: Point, b: Point): Double = {
    val dx = a.x - b.x
    val dy = a.y - b.y
    Math.sqrt(dx.toDouble * dx.toDouble + dy.toDouble * dy.toDouble)
  }

  private val geometryFactory = new GeometryFactory()

  private def toCoordinate(point: Point): Coordinate =
    new Coordinate(point.x.toDouble, point.y.toDouble)

  private def toPoint(coord: Coordinate): Point =
    Point(math.round(coord.x).toInt, math.round(coord.y).toInt)

  private def polygonFrom(points: List[Point]): Polygon = {
    val ring = (points :+ points.head).map(toCoordinate).toArray
    geometryFactory.createPolygon(ring)
  }

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

  private def buildVoronoiCells(points: List[Point], mask: Polygon): Map[Point, List[Point]] = {
    if (points.isEmpty) Map.empty
    else {
      val builder = new VoronoiDiagramBuilder()
      builder.setSites(points.map(toCoordinate).asJava)
      builder.setClipEnvelope(new Envelope(0, mapWidth.toDouble, 0, mapHeight.toDouble))
      val diagram = builder.getDiagram(geometryFactory)
      val polygons = (0 until diagram.getNumGeometries).toList.map { idx =>
        diagram.getGeometryN(idx).asInstanceOf[Polygon]
      }

      val grouped = polygons.groupBy { poly =>
        val centroid = toPoint(poly.getCentroid.getCoordinate)
        points.minBy(p => distance(p, centroid))
      }

      grouped.flatMap { case (site, polys) =>
        val raw = polys.maxBy(_.getArea)
        val clipped = raw.intersection(mask)
        val poly = clipped match {
          case polygon: Polygon => Some(polygon)
          case multi if multi.getNumGeometries > 0 =>
            (0 until multi.getNumGeometries)
              .map(multi.getGeometryN)
              .collect { case p: Polygon => p }
              .sortBy(_.getArea)
              .lastOption
          case _ => None
        }
        poly.map { p =>
          val coords = p.getExteriorRing.getCoordinates.toList
          val boundary = coords.dropRight(1).map(toPoint _)
          site -> boundary
        }
      }
    }
  }

  private def buildCityMask(seed: Long): List[Point] = {
    val rand      = new Random(seed + 13)
    val centerX   = mapWidth / 2
    val centerY   = mapHeight / 2
    val base      = math.min(mapWidth, mapHeight) * 0.44
    val points    = 18
    (0 until points).map { i =>
      val angle = (Math.PI * 2 / points) * i
      val variance = 0.7 + rand.nextDouble() * 0.35
      val radius = base * variance
      val x = (centerX + math.cos(angle) * radius + rand.nextInt(24) - 12).toInt
      val y = (centerY + math.sin(angle) * radius + rand.nextInt(24) - 12).toInt
      Point(
        math.max(mapPadding, math.min(mapWidth - mapPadding, x)),
        math.max(mapPadding, math.min(mapHeight - mapPadding, y)),
      )
    }.toList
  }

  private def buildSettlementOutline(boundaries: List[List[Point]], seed: Long): List[Point] = {
    val polygons = boundaries.flatMap { boundary =>
      if (boundary.size >= 3) Some(polygonFrom(boundary)) else None
    }
    if (polygons.isEmpty) List.empty
    else {
      val geom = geometryFactory
        .createGeometryCollection(polygons.toArray)
        .union()
        .buffer(26.0)
      val simplified = DouglasPeuckerSimplifier.simplify(geom, 6.0)
      val shell = geom match {
        case polygon: Polygon => polygon
        case multi if multi.getNumGeometries > 0 =>
          (0 until multi.getNumGeometries)
            .map(multi.getGeometryN)
            .collect { case poly: Polygon => poly }
            .sortBy(_.getArea)
            .lastOption
            .getOrElse(polygons.head)
        case _ => polygons.head
      }
      val simplifiedShell = simplified match {
        case polygon: Polygon => polygon
        case multi if multi.getNumGeometries > 0 =>
          (0 until multi.getNumGeometries)
            .map(multi.getGeometryN)
            .collect { case poly: Polygon => poly }
            .sortBy(_.getArea)
            .lastOption
            .getOrElse(shell)
        case _ => shell
      }
      val coords = simplifiedShell.getExteriorRing.getCoordinates.toList.dropRight(1).map(toPoint _)
      if (coords.size >= 3) roughenBoundary(coords, new Random(seed + 11)) else coords
    }
  }

  private def expandHull(points: List[Point], padding: Int, seed: Long): List[Point] = {
    if (points.isEmpty) points
    else {
      val rand     = new Random(seed)
      val centroid = Point(points.map(_.x).sum / points.size, points.map(_.y).sum / points.size)
      points.map { p =>
        val dx   = (p.x - centroid.x).toDouble
        val dy   = (p.y - centroid.y).toDouble
        val dist = Math.max(1.0, Math.sqrt(dx * dx + dy * dy))
        val scale = (dist + padding) / dist
        val jitter = 16
        val x = (centroid.x + dx * scale + rand.nextInt(jitter * 2 + 1) - jitter).toInt
        val y = (centroid.y + dy * scale + rand.nextInt(jitter * 2 + 1) - jitter).toInt
        Point(
          math.max(10, math.min(mapWidth - 10, x)),
          math.max(10, math.min(mapHeight - 10, y)),
        )
      }
    }
  }

  private final case class BuildingProfile(
    minWidth: Int,
    maxWidth: Int,
    minHeight: Int,
    maxHeight: Int,
  )

  private def buildingProfileFor(districtType: String, rand: Random): BuildingProfile =
    districtType match {
      case "Temple district" =>
        BuildingProfile(28, 60, 28, 60)
      case "Castle district" =>
        BuildingProfile(36, 72, 36, 72)
      case "Market" =>
        if (rand.nextBoolean())
          BuildingProfile(10, 18, 20, 36)
        else
          BuildingProfile(20, 36, 10, 18)
      case "University district" =>
        BuildingProfile(24, 54, 18, 40)
      case "High District" =>
        BuildingProfile(22, 48, 20, 44)
      case "Artisan district" =>
        BuildingProfile(18, 40, 16, 36)
      case "Low district" =>
        BuildingProfile(16, 34, 14, 30)
      case "Slums" =>
        BuildingProfile(12, 28, 12, 28)
      case _ =>
        BuildingProfile(16, 46, 16, 46)
    }

  private def generateBuildingFootprints(
    boundary: List[Point],
    rand: Random,
    targetCount: Int,
    roadAnchors: List[(Point, Point, Boolean)],
    roadCurves: List[RoadCurve],
    plazas: List[Plaza],
    profile: BuildingProfile,
  ): List[List[Point]] = {
    if (boundary.size < 3 || targetCount <= 0) List.empty
    else {
      val districtPoly = polygonFrom(boundary)
      val baseUsable: Geometry = Option(districtPoly.buffer(-10)).filterNot(_.isEmpty).getOrElse(districtPoly)

      val buildings = scala.collection.mutable.ListBuffer.empty[Polygon]
      val footprints = scala.collection.mutable.ListBuffer.empty[List[Point]]
      val maxAttempts = targetCount * 25
      var attempts = 0

      val plazasByCoord = plazas.map { plaza =>
        (plaza, geometryFactory.createPoint(toCoordinate(plaza.center)))
      }
      val plazaBuffers = plazas.map { plaza =>
        geometryFactory.createPoint(toCoordinate(plaza.center)).buffer(plaza.radius + 6.0)
      }

      val roadBuffer = 14.0
      val roadsInDistrict = roadCurves.filter { curve =>
        curve.line.intersects(baseUsable) || curve.line.distance(baseUsable) <= roadBuffer
      }
      val carvedUsable = roadsInDistrict.foldLeft(baseUsable) { (acc, curve) =>
        val buffer = if (curve.isMain) roadBuffer + 4.0 else roadBuffer
        val cut = curve.line.buffer(buffer)
        val diff = acc.difference(cut)
        if (diff.isEmpty) acc else diff
      }
      val usable = if (carvedUsable.isEmpty) baseUsable else carvedUsable
      val envelope = usable.getEnvelopeInternal
      val centroid = usable.getCentroid.getCoordinate

      def pickRoadAnchor(): Option[(Point, Point, Boolean)] = {
        if (roadAnchors.isEmpty) None
        else {
          val main = roadAnchors.filter(_._3)
          if (main.nonEmpty && rand.nextDouble() < 0.7) Some(main(rand.nextInt(main.length)))
          else Some(roadAnchors(rand.nextInt(roadAnchors.length)))
        }
      }

      while (footprints.size < targetCount && attempts < maxAttempts) {
        attempts += 1
        val widthRange = math.max(1, profile.maxWidth - profile.minWidth + 1)
        val heightRange = math.max(1, profile.maxHeight - profile.minHeight + 1)
        val width = profile.minWidth + rand.nextInt(widthRange)
        val height = profile.minHeight + rand.nextInt(heightRange)
        val baseX = envelope.getMinX + rand.nextDouble() * math.max(1.0, envelope.getWidth - width)
        val baseY = envelope.getMinY + rand.nextDouble() * math.max(1.0, envelope.getHeight - height)
        val plazaGuided = plazas.nonEmpty && rand.nextDouble() < 0.6
        val alignToRoad = !plazaGuided && roadAnchors.nonEmpty && rand.nextDouble() < 0.7
        val (x, y, angle) = if (plazaGuided) {
          val plaza = plazas(rand.nextInt(plazas.length))
          val theta = rand.nextDouble() * math.Pi * 2
          val distance = plaza.radius + 10.0 + rand.nextDouble() * 18.0
          val px = plaza.center.x + math.cos(theta) * distance
          val py = plaza.center.y + math.sin(theta) * distance
          val angle = math.atan2(plaza.center.y - py, plaza.center.x - px) + (rand.nextDouble() - 0.5) * 0.25
          (px, py, angle)
        } else {
          pickRoadAnchor() match {
            case Some((start, end, _)) if alignToRoad =>
              val t = 0.2 + rand.nextDouble() * 0.7
              val lineX = start.x + (end.x - start.x) * t
              val lineY = start.y + (end.y - start.y) * t
              val dx = end.x - start.x
              val dy = end.y - start.y
              val len = math.max(1.0, math.hypot(dx.toDouble, dy.toDouble))
              val nx = -dy / len
              val ny = dx / len
              val lane = if (rand.nextBoolean()) 1.0 else -1.0
              val corridor = 16.0 + rand.nextDouble() * 12.0
              val px = lineX
              val py = lineY
              val angle = math.atan2(dy.toDouble, dx.toDouble) + (rand.nextDouble() - 0.5) * 0.08
              val offsetX = px + nx * corridor * lane
              val offsetY = py + ny * corridor * lane
              (offsetX, offsetY, angle)
            case _ =>
              val mix =
                if (rand.nextDouble() < 0.6) 0.6 + rand.nextDouble() * 0.3
                else 0.25 + rand.nextDouble() * 0.35
              val px = centroid.x * mix + baseX * (1.0 - mix)
              val py = centroid.y * mix + baseY * (1.0 - mix)
              val angle = (rand.nextDouble() - 0.5) * 0.35
              (px, py, angle)
          }
        }
        val rectCoords = Array(
          new Coordinate(x, y),
          new Coordinate(x + width, y),
          new Coordinate(x + width, y + height),
          new Coordinate(x, y + height),
          new Coordinate(x, y),
        )
        val rect = geometryFactory.createPolygon(rectCoords)
        val rotated =
          AffineTransformation.rotationInstance(angle, x + width / 2.0, y + height / 2.0).transform(rect)

        val within = usable.contains(rotated)
        val overlaps = buildings.exists(existing => existing.buffer(3).intersects(rotated))
        val nearPlaza = plazasByCoord.exists { case (plaza, point) =>
          point.distance(rotated.getCentroid) <= (plaza.radius + 14)
        }
        val intersectsPlaza = plazaBuffers.exists(_.intersects(rotated))
        val nearRoad = roadsInDistrict.exists { curve =>
          val buffer = if (curve.isMain) roadBuffer + 4.0 else roadBuffer
          curve.line.distance(rotated) < buffer
        }
        if (within && !overlaps && !nearRoad && !intersectsPlaza && (!nearPlaza || plazaGuided)) {
          val rotatedPolygon = rotated.asInstanceOf[Polygon]
          buildings += rotatedPolygon
          val coords = rotatedPolygon.getExteriorRing.getCoordinates.toList.dropRight(1).map(toPoint _)
          if (coords.size >= 3) footprints += coords
        }
      }

      footprints.toList
    }
  }

  private def pickWeightedRace(races: List[Race]): Option[Race] = {
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

  private def randomFullName(names: List[Name], race: Option[String]): String = {
    val firstNames = names.filter(n => n.firstName.contains(true) && matchesRace(n, race))
    val lastNames  = names.filter(n => n.lastName.contains(true) && matchesRace(n, race))

    val first = pickOne(firstNames).map(_.name)
    val last  = pickOne(lastNames).map(_.name)

    (first, last) match {
      case (Some(f), Some(l)) => s"$f $l"
      case (Some(f), None)    => f
      case (None, Some(l))    => l
      case _                  => "Nameless"
    }
  }

  private def pickSettlementName(
    settlementNames: List[SettlementName],
    settlementType: SettlementType,
    rand: Random,
  ): String = {
    val matching = settlementNames.filter(_.settlementType.equalsIgnoreCase(settlementType.name))
    if (matching.isEmpty) settlementType.name
    else matching(rand.nextInt(matching.length)).name
  }

  private def qualityValue(qualities: List[NpcQuality], category: String): Option[String] =
    pickOne(qualities.filter(_.category == category).map(_.value))

  private def backgroundKeywordsFor(poiName: String, kind: String, shopType: Option[String]): List[String] = {
    val combined = s"$poiName ${shopType.getOrElse("")}".toLowerCase
    if (kind == "tavern" || combined.contains("tavern") || combined.contains("inn")) {
      List("barkeep", "miller", "baker", "butcher", "barber", "attendant", "merchant")
    } else if (kind == "shop") {
      List(
        "blacksmith",
        "armorer",
        "jeweler",
        "goldsmith",
        "locksmith",
        "dyer",
        "stonemason",
        "merchant",
        "moneylender",
      )
    } else if (combined.contains("temple") || combined.contains("chapel") || combined.contains("shrine")) {
      List("beadle", "healer", "scribe", "sage", "scholar")
    } else if (combined.contains("market")) {
      List("merchant", "moneylender", "cartographer")
    } else if (combined.contains("library") || combined.contains("university")) {
      List("scribe", "scholar", "sage", "cartographer")
    } else if (combined.contains("castle") || combined.contains("guard") || combined.contains("watch")) {
      List("soldier", "caravan guard", "barrister", "attendant")
    } else if (combined.contains("farm")) {
      List("farmer", "miller", "mushroom-farmer")
    } else {
      List.empty
    }
  }

  private def pickBackgroundFor(
    backgrounds: List[Background],
    poiName: String,
    kind: String,
    shopType: Option[String],
  ): Option[Background] = {
    val keywords = backgroundKeywordsFor(poiName, kind, shopType)
    val candidates =
      if (keywords.nonEmpty)
        backgrounds.filter(bg => keywords.exists(k => bg.name.toLowerCase.contains(k)))
      else
        backgrounds
    pickOne(candidates)
  }

  private def centroid(points: List[Point]): Point = {
    if (points.isEmpty) Point(mapWidth / 2, mapHeight / 2)
    else {
      val (xSum, ySum) = points.foldLeft((0, 0)) { case ((xs, ys), p) => (xs + p.x, ys + p.y) }
      Point(xSum / points.size, ySum / points.size)
    }
  }

  private def nudgePoiLocation(
    point: Point,
    used: List[Point],
    rand: Random,
    boundary: List[Point],
    buildings: List[Building],
    anchor: Option[Point],
  ): Point = {
    val minDistance = 26.0
    val boundaryPoly = if (boundary.size >= 3) polygonFrom(boundary) else null
    val boundaryCentroid =
      if (boundaryPoly != null) toPoint(boundaryPoly.getCentroid.getCoordinate) else Point(mapWidth / 2, mapHeight / 2)
    anchor match {
      case Some(center) =>
        val ringOffsets = List(0, 8, 12, 16)
        val directions = List(
          Point(0, 0),
          Point(1, 0),
          Point(-1, 0),
          Point(0, 1),
          Point(0, -1),
          Point(1, 1),
          Point(-1, 1),
          Point(1, -1),
          Point(-1, -1),
        )
        val candidates = ringOffsets.flatMap { radius =>
          directions.map { dir =>
            Point(center.x + dir.x * radius, center.y + dir.y * radius)
          }
        }
        candidates.find { candidate =>
          insideBoundary(candidate, boundaryPoly) &&
          used.forall(p => distance(p, candidate) >= minDistance)
        }.getOrElse {
          if (insideBoundary(center, boundaryPoly)) center
          else Point(
            ((center.x + boundaryCentroid.x) / 2.0).round.toInt,
            ((center.y + boundaryCentroid.y) / 2.0).round.toInt,
          )
        }
      case None =>
        var current = point
        var attempts = 0
        while (attempts < 12 && (used.exists(p => distance(p, current) < minDistance) || !insideBoundary(current, boundaryPoly))) {
          attempts += 1
          val nudged = Point(
            math.max(10, math.min(mapWidth - 10, current.x + rand.nextInt(31) - 15)),
            math.max(10, math.min(mapHeight - 10, current.y + rand.nextInt(31) - 15)),
          )
          current =
            if (boundaryPoly != null && !insideBoundary(nudged, boundaryPoly)) {
              Point(
                ((nudged.x + boundaryCentroid.x) / 2.0).round.toInt,
                ((nudged.y + boundaryCentroid.y) / 2.0).round.toInt,
              )
            } else nudged
        }
        val candidate = buildings
          .filter(_.poiId.isDefined)
          .map(b => centroid(b.footprint))
          .sortBy(p => distance(p, current))
          .headOption
          .filter(p => distance(p, current) <= 50)
          .getOrElse(current)
        candidate
    }
  }

  private def insideBoundary(point: Point, boundaryPoly: Polygon): Boolean =
    if (boundaryPoly == null) true
    else boundaryPoly.contains(geometryFactory.createPoint(toCoordinate(point)))

  private def buildNpc(
    names: List[Name],
    races: List[Race],
    personalities: List[Personality],
    backgrounds: List[Background],
    qualities: List[NpcQuality],
    poiName: String,
    kind: String,
    shopType: Option[String],
  ): Npc = {
    val pickedRace = pickWeightedRace(races).orElse(pickOne(races))
    val ancestry   = pickedRace.map(_.race).getOrElse("Unknown")
    val npcName    = randomFullName(names, pickedRace.map(_.race))
    val background = pickBackgroundFor(backgrounds, poiName, kind, shopType).map(_.name)
    val personality = pickOne(personalities).map(_.name)

    Npc(
      name = npcName,
      ancestry = ancestry,
      age = qualityValue(qualities, npcAgeCategory).getOrElse("Adult"),
      alignment = rollAlignment,
      wealth = qualityValue(qualities, npcWealthCategory).getOrElse("Standard"),
      appearance = qualityValue(qualities, npcAppearanceCategory).getOrElse("Unremarkable"),
      mannerism = qualityValue(qualities, npcDoesCategory).getOrElse("Quiet"),
      secret = qualityValue(qualities, npcSecretCategory).getOrElse("Unknown"),
      background = background,
      personality = personality,
    )
  }

  def randomSettlement: Task[Settlement] =
    for {
      names         <- nameServer.getNames
      races         <- raceServer.getRaces
      personalities <- personalityServer.getPersonalities
      backgrounds   <- backgroundServer.getBackgrounds
      qualities     <- npcQualityServer.getQualities
      settlementNames <- settlementNameServer.getSettlementNames
      settlement    <- ZIO.attempt {
                         val settlementType     = settlementTypeForRoll(rollDie(6))
                         val seed               = rng.nextLong()
                         val rand               = new Random(seed)
                         val dieRolls           = (1 to settlementType.diceCount).map(_ => rollDie(settlementType.dieSides)).toList
                         val maskPoints         = buildCityMask(seed)
                         val maskPolygon         = polygonFrom(maskPoints)
                         val points             = buildPoints(settlementType.diceCount, maskPolygon)
                         val settlementAlignment = rollAlignment
                         val settlementName     = pickSettlementName(settlementNames, settlementType, rand)
                         val maxRoll            = dieRolls.max
                         val seatIndex          = dieRolls.indexOf(maxRoll) + 1
                         val cellBoundaries     = buildVoronoiCells(points, maskPolygon)
                         val outline            = buildSettlementOutline(cellBoundaries.values.toList, seed) match {
                           case points if points.size >= 3 => points
                           case _                           => roughenBoundary(maskPoints, new Random(seed + 17))
                         }
                         val outlinePolygon = polygonFrom(outline)
                         val roadEdgesForPoints = buildRoadEdgesForPoints(points, seatIndex)
                         val roadSeed = roadSeedFor(seed)
                         val roadCurves = buildRoadCurvesForPoints(roadEdgesForPoints, new Random(roadSeed))
                         val districtTypeAssignments =
                           if (dieRolls.size < districtTypes.length)
                             shuffleWith(districtTypes, new Random(seed + 31)).take(dieRolls.size)
                           else
                             dieRolls.map(districtTypeForRoll)

                         var poiId      = 0
                         var buildingId = 0
                         val usedPoiLocations = scala.collection.mutable.ListBuffer.empty[Point]

                         val seatPoint = points.lift(seatIndex - 1).getOrElse(Point(mapWidth / 2, mapHeight / 2))
                         val districts = dieRolls.zip(points).zipWithIndex.map { case ((roll, point), idx) =>
                           val districtType = districtTypeAssignments(idx)
                           val fallbackBoundary = List(
                             Point(point.x - 40, point.y - 40),
                             Point(point.x + 40, point.y - 40),
                             Point(point.x + 40, point.y + 40),
                             Point(point.x - 40, point.y + 40),
                           )
                           val boundary =
                             cellBoundaries.get(point).filter(_.size >= 3).getOrElse(fallbackBoundary)
                           val plazas =
                             if (Set("Market", "Temple district", "Castle district").contains(districtType)) {
                               val center = centroid(boundary)
                               val radius = 26 + rand.nextInt(16)
                               List(Plaza(center, radius))
                             } else {
                               List.empty
                             }
                           val districtPolygon = polygonFrom(boundary)
                           val area = districtPolygon.getArea
                           val roadAnchors = roadEdgesForPoints.collect {
                             case (start, end, isMain) if start == point => (start, end, isMain)
                             case (start, end, isMain) if end == point   => (end, start, isMain)
                           }
                           val baseTarget = math.max(6, math.min(50, (area / 5200).toInt))
                           val mainRoadCount = roadAnchors.count(_._3)
                           val roadBias = 1.0 + math.min(0.45, mainRoadCount * 0.15)
                           val hubBias = (if (idx + 1 == seatIndex) 0.2 else 0.0) + (if (plazas.nonEmpty) 0.15 else 0.0)
                           val edgeDistance = {
                             val centerPoint = geometryFactory.createPoint(districtPolygon.getCentroid.getCoordinate)
                             outlinePolygon.getExteriorRing.distance(centerPoint)
                           }
                           val edgeFactor = math.max(0.0, math.min(1.0, (edgeDistance - 20.0) / 120.0))
                           val edgeBias = 0.7 + 0.3 * edgeFactor
                           val buildingTarget = math.max(6, math.min(50, (baseTarget * roadBias * (1.0 + hubBias) * edgeBias).round.toInt))
                           val buildingProfile = buildingProfileFor(districtType, rand)
                           val footprints =
                             generateBuildingFootprints(
                               boundary,
                               rand,
                               buildingTarget,
                               roadAnchors,
                               roadCurves,
                               plazas,
                               buildingProfile,
                             )
                           val baseBuildings = footprints.map { footprint =>
                             buildingId += 1
                             Building(id = buildingId, footprint = footprint, usage = "Residence", poiId = None)
                           }

                           val poiCount = rollDie(4)
                           val buildingSlots = shuffleWith(baseBuildings.indices.toList, rand)
                           val (pois, updatedBuildings) = (1 to poiCount).foldLeft((List.empty[PointOfInterest], baseBuildings)) {
                             case ((poiAcc, buildingsAcc), _) =>
                               val template = rollPoiTemplate(districtType)
                               val tavern   = template.tavernQuality.map(buildTavern)
                               val shop     = template.shopQuality.map(q => buildShop(q, template.shopTypeOverride))
                               val usage = template.kind match {
                                 case "tavern" => tavern.map(_.name).getOrElse(template.name)
                                 case "shop"   => shop.map(_.shopType).getOrElse(template.name)
                                 case _        => template.name
                               }
                               val assignedBuildingIndex = buildingSlots.drop(poiAcc.size).headOption
                               val (buildingIdOpt, buildingLocation, nextBuildings) = assignedBuildingIndex match {
                                 case Some(index) =>
                                   val building = buildingsAcc(index)
                                   val updated = building.copy(usage = usage, poiId = Some(poiId + 1))
                                   val updatedBuildings = buildingsAcc.updated(index, updated)
                                   (Some(building.id), centroid(building.footprint), updatedBuildings)
                                 case None =>
                                   (None, point, buildingsAcc)
                               }
                               poiId += 1
                               val npc = buildNpc(names, races, personalities, backgrounds, qualities, template.name, template.kind, shop.map(_.shopType))
                               val markerLocation = nudgePoiLocation(
                                 buildingLocation,
                                 usedPoiLocations.toList,
                                 rand,
                                 boundary,
                                 nextBuildings,
                                 buildingIdOpt.map(_ => buildingLocation),
                               )
                               usedPoiLocations += markerLocation
                               val poi = PointOfInterest(
                                 id = poiId,
                                 name = template.name,
                                 kind = template.kind,
                                 location = markerLocation,
                                 tavern = tavern,
                                 shop = shop,
                                 npc = Some(npc),
                                 buildingId = buildingIdOpt,
                               )
                               (poiAcc :+ poi, nextBuildings)
                           }

                           District(
                             id = idx + 1,
                             roll = roll,
                             districtType = districtType,
                             alignment = rollAlignment,
                             seatOfGovernment = idx + 1 == seatIndex,
                             position = point,
                             boundary = boundary,
                             plazas = plazas,
                             pointsOfInterest = pois,
                             buildings = updatedBuildings,
                           )
                         }

                         Settlement(
                           name = settlementName,
                           settlementType = settlementType,
                           alignment = settlementAlignment,
                           districts = districts,
                           seatOfGovernment = seatIndex,
                           layout = SettlementLayout(
                             width = mapWidth,
                             height = mapHeight,
                             gridSize = gridSize,
                             outline = outline,
                             seed = seed,
                           ),
                         )
                       }
    } yield settlement

  def renderSettlementPng(settlement: Settlement): Task[Array[Byte]] =
    ZIO.attempt {
      val image = renderSettlementMapImage(settlement)
      val output = new ByteArrayOutputStream()
      try {
        ImageIO.write(image, "png", output)
        output.toByteArray
      } finally output.close()
    }

  def renderSettlementPdf(settlement: Settlement): Task[Array[Byte]] =
    ZIO.attempt {
      val mapImage = renderSettlementMapImage(settlement)
      val legendImage = renderSettlementLegendImage(settlement)
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

  private def renderSettlementMapImage(settlement: Settlement): BufferedImage = {
    val image = baseSettlementImage()
    val g = image.createGraphics()
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBackground(g)

      val mapX      = pageMargin
      val mapY      = pageMargin
      val mapWidth  = settlement.layout.width
      val mapHeight = settlement.layout.height

      val fallbackOutline = List(
        Point(mapPadding, mapPadding),
        Point(mapWidth - mapPadding, mapPadding),
        Point(mapWidth - mapPadding, mapHeight - mapPadding),
        Point(mapPadding, mapHeight - mapPadding),
      )
      val outlinePoints = if (settlement.layout.outline.size >= 3) settlement.layout.outline else fallbackOutline
      val outlinePath = polygonPath(mapX, mapY, outlinePoints, new Random(settlement.layout.seed + 7))

      g.setColor(new Color(235, 222, 198))
      g.fill(outlinePath)

      val oldClip = g.getClip
      g.setClip(outlinePath)
      drawGrid(g, mapX, mapY, mapWidth, mapHeight, settlement.layout.gridSize)
      drawDistricts(g, mapX, mapY, settlement, outlinePath, new Random(settlement.layout.seed + 19))
      g.setClip(oldClip)
      drawOutline(g, outlinePath)
      drawTitle(g, settlement)
    } finally g.dispose()
    image
  }

  private def renderSettlementLegendImage(settlement: Settlement): BufferedImage = {
    val image = baseSettlementImage()
    val g = image.createGraphics()
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBackground(g)
      val legendX = pageMargin
      val legendY = pageMargin
      val legendW = pageWidth - (pageMargin * 2)
      val legendH = pageHeight - (pageMargin * 2)
      drawLegend(g, settlement, legendX, legendY, legendW, legendH)
      drawTitle(g, settlement)
    } finally g.dispose()
    image
  }

  private def baseSettlementImage(): BufferedImage =
    new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB)

  private def paintBackground(g: java.awt.Graphics2D): Unit = {
    val parchmentTop    = new Color(245, 231, 204)
    val parchmentBottom = new Color(230, 214, 183)
    g.setPaint(new GradientPaint(0f, 0f, parchmentTop, 0f, pageHeight.toFloat, parchmentBottom))
    g.fillRect(0, 0, pageWidth, pageHeight)
  }

  private def drawGrid(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    mapWidth: Int,
    mapHeight: Int,
    gridSize: Int,
  ): Unit = {
    g.setColor(new Color(185, 170, 140, 60))
    g.setStroke(new BasicStroke(0.8f))
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

  private def drawOutline(g: java.awt.Graphics2D, path: Path2D): Unit = {
    g.setColor(new Color(90, 70, 50))
    g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g.draw(path)
  }

  private sealed trait TexturePattern
  private case object DiagonalHatch extends TexturePattern
  private case object CrossHatch extends TexturePattern
  private case object DotPattern extends TexturePattern
  private case object HorizontalHatch extends TexturePattern

  private final case class DistrictStyle(fill: Color, pattern: TexturePattern)

  private def clampColor(value: Int): Int =
    math.max(0, math.min(255, value))

  private def tintedColor(base: Color, seed: Long): Color = {
    val rand = new Random(seed)
    val delta = rand.nextInt(19) - 9
    new Color(
      clampColor(base.getRed + delta),
      clampColor(base.getGreen + delta),
      clampColor(base.getBlue + delta),
      base.getAlpha,
    )
  }

  private def texturePatternFor(districtType: String): TexturePattern =
    districtType match {
      case "Temple district"     => CrossHatch
      case "Castle district"     => DiagonalHatch
      case "Market"              => DotPattern
      case "University district" => HorizontalHatch
      case "High District"       => DiagonalHatch
      case "Artisan district"    => DotPattern
      case "Low district"        => HorizontalHatch
      case "Slums"               => DiagonalHatch
      case _                     => DiagonalHatch
    }

  private def districtStyleFor(district: District, idx: Int, seed: Long): DistrictStyle = {
    val base = districtPalette(idx % districtPalette.length)
    val tintSeed = seed + district.id * 31L + district.districtType.hashCode.toLong
    DistrictStyle(tintedColor(base, tintSeed), texturePatternFor(district.districtType))
  }

  private def patternColor(fill: Color, alpha: Int): Color =
    new Color(
      clampColor(fill.getRed - 32),
      clampColor(fill.getGreen - 32),
      clampColor(fill.getBlue - 32),
      alpha,
    )

  private def drawTextureInBounds(
    g: java.awt.Graphics2D,
    path: Path2D,
    pattern: TexturePattern,
    fill: Color,
  ): Unit = {
    val bounds = path.getBounds2D
    val oldClip = g.getClip
    g.setClip(path)
    pattern match {
      case DotPattern =>
        g.setColor(patternColor(fill, 45))
        val spacing = 14
        val size = 3
        var y = bounds.getY.toInt
        while (y <= bounds.getMaxY) {
          var x = bounds.getX.toInt
          while (x <= bounds.getMaxX) {
            g.fillOval(x, y, size, size)
            x += spacing
          }
          y += spacing
        }
      case HorizontalHatch =>
        g.setColor(patternColor(fill, 50))
        g.setStroke(new BasicStroke(1.0f))
        val spacing = 12
        var y = bounds.getY.toInt
        while (y <= bounds.getMaxY) {
          g.drawLine(bounds.getX.toInt, y, bounds.getMaxX.toInt, y)
          y += spacing
        }
      case DiagonalHatch =>
        g.setColor(patternColor(fill, 50))
        g.setStroke(new BasicStroke(1.0f))
        val spacing = 14
        val start = bounds.getX.toInt - bounds.getHeight.toInt
        val end = bounds.getMaxX.toInt + bounds.getHeight.toInt
        var x = start
        while (x <= end) {
          g.drawLine(x, bounds.getY.toInt, x + bounds.getHeight.toInt, bounds.getMaxY.toInt)
          x += spacing
        }
      case CrossHatch =>
        g.setColor(patternColor(fill, 48))
        g.setStroke(new BasicStroke(1.0f))
        val spacing = 14
        val start = bounds.getX.toInt - bounds.getHeight.toInt
        val end = bounds.getMaxX.toInt + bounds.getHeight.toInt
        var x = start
        while (x <= end) {
          g.drawLine(x, bounds.getY.toInt, x + bounds.getHeight.toInt, bounds.getMaxY.toInt)
          g.drawLine(x, bounds.getMaxY.toInt, x + bounds.getHeight.toInt, bounds.getY.toInt)
          x += spacing
        }
    }
    g.setClip(oldClip)
  }

  private def drawDistricts(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    settlement: Settlement,
    outlinePath: Path2D,
    rand: Random,
  ): Unit = {
    settlement.districts.zipWithIndex.foreach { case (district, idx) =>
      val path   = polygonPath(mapX, mapY, roughenBoundary(district.boundary, rand), rand)
      val style = districtStyleFor(district, idx, settlement.layout.seed)
      g.setColor(style.fill)
      g.fill(path)
      drawTextureInBounds(g, path, style.pattern, style.fill)
      g.setColor(new Color(90, 70, 50, 180))
      g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
      g.draw(path)
    }

    val roadEdges = buildRoadEdges(settlement.districts)
    val roadSeed = roadSeedFor(settlement.layout.seed)
    val roadSamples = sampleRoads(roadEdges, mapX, mapY, new Random(roadSeed))

    drawPlazas(g, mapX, mapY, settlement.districts)
    drawBuildings(g, mapX, mapY, settlement.districts, rand)
    drawRoads(g, mapX, mapY, roadEdges, new Random(roadSeed))
    drawPoiMarkers(g, mapX, mapY, settlement.districts)
  }

  private def drawPlazas(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    districts: List[District],
  ): Unit = {
    g.setColor(new Color(232, 224, 204, 220))
    districts.foreach { district =>
      district.plazas.foreach { plaza =>
        val x = mapX + plaza.center.x - plaza.radius
        val y = mapY + plaza.center.y - plaza.radius
        val size = plaza.radius * 2
        g.fillOval(x, y, size, size)
        g.setColor(new Color(150, 128, 98, 170))
        g.setStroke(new BasicStroke(1.4f))
        g.drawOval(x, y, size, size)
        g.setColor(new Color(232, 224, 204, 220))
      }
    }
  }
  private def roughenBoundary(points: List[Point], rand: Random): List[Point] = {
    if (points.size < 3) points
    else {
      val jitter = 12.0
      points.zip(points.tail :+ points.head).flatMap { case (a, b) =>
        val mid = Point((a.x + b.x) / 2, (a.y + b.y) / 2)
        val dx = b.x - a.x
        val dy = b.y - a.y
        val len = math.max(1.0, math.hypot(dx.toDouble, dy.toDouble))
        val nx = -dy / len
        val ny = dx / len
        val offset = (rand.nextDouble() - 0.5) * 2 * jitter
        val nudged = Point(
          (mid.x + nx * offset).round.toInt,
          (mid.y + ny * offset).round.toInt,
        )
        List(a, nudged)
      }
    }
  }

  private def drawBuildings(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    districts: List[District],
    rand: Random,
  ): Unit = {
    districts.foreach { district =>
      district.buildings.foreach { building =>
        val path = polygonPath(mapX, mapY, building.footprint, rand)
        val fill = new Color(244, 236, 218, 220)
        g.setColor(fill)
        g.fill(path)
        val stroke = if (building.poiId.isDefined) new Color(90, 70, 50, 200) else new Color(120, 100, 80, 160)
        g.setColor(stroke)
        val width = if (building.poiId.isDefined) 2.0f else 1.2f
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
        g.draw(path)
      }
    }
  }

  private def drawPoiMarkers(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    districts: List[District],
  ): Unit = {
    val labelFont = new Font("Serif", Font.BOLD, 18)
    g.setFont(labelFont)
    val metrics = g.getFontMetrics
    districts.flatMap(_.pointsOfInterest).foreach { poi =>
      val x = mapX + poi.location.x
      val y = mapY + poi.location.y
      val label = poi.id.toString
      val width = metrics.stringWidth(label)
      val circle = new Ellipse2D.Double(x - 12, y - 12, 24, 24)
      g.setColor(new Color(250, 244, 230, 230))
      g.fill(circle)
      g.setColor(new Color(80, 60, 45))
      g.setStroke(new BasicStroke(2f))
      g.draw(circle)
      g.setColor(new Color(60, 45, 30))
      g.drawString(label, x - width / 2, y + metrics.getAscent / 2 - 2)
    }
  }

  private def roadSeedFor(seed: Long): Long =
    seed + 971L

  private def buildRoadEdges(districts: List[District]): List[(District, District, Boolean)] = {
    if (districts.isEmpty) return Nil

    val seat = districts.find(_.seatOfGovernment).getOrElse(districts.head)
    val mainTargets = districts
      .filterNot(_.id == seat.id)
      .sortBy(other => distance(seat.position, other.position))
      .take(math.min(2, districts.size - 1))

    val edges = scala.collection.mutable.LinkedHashMap.empty[(Int, Int), Boolean]

    def addEdge(a: District, b: District, main: Boolean): Unit = {
      val key = if (a.id < b.id) (a.id, b.id) else (b.id, a.id)
      edges.updateWith(key) {
        case Some(existing) => Some(existing || main)
        case None           => Some(main)
      }
    }

    mainTargets.foreach(target => addEdge(seat, target, main = true))

    val ringDistricts = districts.filterNot(_.id == seat.id)
    if (ringDistricts.size >= 3) {
      val sorted = ringDistricts.sortBy { district =>
        math.atan2((district.position.y - seat.position.y).toDouble, (district.position.x - seat.position.x).toDouble)
      }
      sorted.zip(sorted.tail :+ sorted.head).foreach { case (a, b) =>
        addEdge(a, b, main = true)
      }
    }

    ringDistricts match {
      case Nil => ()
      case _ =>
        val farthest = ringDistricts.maxBy(other => distance(seat.position, other.position))
        addEdge(seat, farthest, main = true)
    }

    districts.foreach { district =>
      val neighbor = districts
        .filterNot(_.id == district.id)
        .minBy(other => distance(district.position, other.position))
      addEdge(district, neighbor, main = false)
    }

    val byId = districts.map(d => d.id -> d).toMap
    edges.toList.flatMap { case ((aId, bId), isMain) =>
      for {
        a <- byId.get(aId)
        b <- byId.get(bId)
      } yield (a, b, isMain)
    }
  }

  private def buildRoadCurvesForPoints(
    roadEdges: List[(Point, Point, Boolean)],
    rand: Random,
  ): List[RoadCurve] =
    roadEdges.map { case (start, end, isMain) =>
      val startX = jitter(start.x, rand).toDouble
      val startY = jitter(start.y, rand).toDouble
      val endX = jitter(end.x, rand).toDouble
      val endY = jitter(end.y, rand).toDouble
      val midX = (startX + endX) / 2.0
      val midY = (startY + endY) / 2.0
      val dx = endX - startX
      val dy = endY - startY
      val len = math.max(1.0, math.hypot(dx, dy))
      val nx = -dy / len
      val ny = dx / len
      val bend = (rand.nextDouble() - 0.5) * 2 * (if (isMain) 50.0 else 32.0)
      val ctrlX = midX + nx * bend
      val ctrlY = midY + ny * bend
      val steps = if (isMain) 12 else 10
      val coords = (0 to steps).map { i =>
        val t = i.toDouble / steps
        val one = 1.0 - t
        val x = one * one * startX + 2 * one * t * ctrlX + t * t * endX
        val y = one * one * startY + 2 * one * t * ctrlY + t * t * endY
        new Coordinate(x, y)
      }.toArray
      RoadCurve(start, end, isMain, geometryFactory.createLineString(coords))
    }

  private def buildRoadEdgesForPoints(points: List[Point], seatIndex: Int): List[(Point, Point, Boolean)] = {
    if (points.isEmpty) return Nil

    val seat = points.lift(seatIndex - 1).getOrElse(points.head)
    val mainTargets = points.zipWithIndex
      .filterNot { case (_, idx) => idx == seatIndex - 1 }
      .sortBy { case (point, _) => distance(seat, point) }
      .take(math.min(2, points.size - 1))

    val edges = scala.collection.mutable.LinkedHashMap.empty[(Int, Int), Boolean]

    def addEdge(a: Int, b: Int, main: Boolean): Unit = {
      val key = if (a < b) (a, b) else (b, a)
      edges.updateWith(key) {
        case Some(existing) => Some(existing || main)
        case None           => Some(main)
      }
    }

    mainTargets.foreach { case (_, idx) => addEdge(seatIndex - 1, idx, main = true) }

    val ringPoints = points.zipWithIndex.filterNot { case (_, idx) => idx == seatIndex - 1 }
    if (ringPoints.size >= 3) {
      val sorted = ringPoints.sortBy { case (point, _) =>
        math.atan2((point.y - seat.y).toDouble, (point.x - seat.x).toDouble)
      }
      sorted.zip(sorted.tail :+ sorted.head).foreach { case ((_, aIdx), (_, bIdx)) =>
        addEdge(aIdx, bIdx, main = true)
      }
    }

    ringPoints match {
      case Nil => ()
      case _ =>
        val (farthest, farthestIdx) = ringPoints.maxBy { case (point, _) => distance(seat, point) }
        addEdge(seatIndex - 1, farthestIdx, main = true)
    }

    points.indices.foreach { idx =>
      val neighbor = points.indices.filterNot(_ == idx).minBy(other => distance(points(idx), points(other)))
      addEdge(idx, neighbor, main = false)
    }

    edges.toList.map { case ((aIdx, bIdx), isMain) =>
      (points(aIdx), points(bIdx), isMain)
    }
  }

  private def sampleRoads(
    roadEdges: List[(District, District, Boolean)],
    mapX: Int,
    mapY: Int,
    rand: Random,
  ): List[(Double, Double)] =
    roadEdges.flatMap { case (a, b, isMain) =>
      val startX = mapX + jitter(a.position.x, rand)
      val startY = mapY + jitter(a.position.y, rand)
      val endX = mapX + jitter(b.position.x, rand)
      val endY = mapY + jitter(b.position.y, rand)
      val midX = (startX + endX) / 2.0
      val midY = (startY + endY) / 2.0
      val dx = endX - startX
      val dy = endY - startY
      val len = math.max(1.0, math.hypot(dx, dy))
      val nx = -dy / len
      val ny = dx / len
      val bend = (rand.nextDouble() - 0.5) * 2 * (if (isMain) 50.0 else 32.0)
      val ctrlX = midX + nx * bend
      val ctrlY = midY + ny * bend
      val steps = if (isMain) 12 else 10
      (0 to steps).map { i =>
        val t = i.toDouble / steps
        val one = 1.0 - t
        val x = one * one * startX + 2 * one * t * ctrlX + t * t * endX
        val y = one * one * startY + 2 * one * t * ctrlY + t * t * endY
        (x, y)
      }
    }

  private def drawRoads(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    roadEdges: List[(District, District, Boolean)],
    rand: Random,
  ): Unit = {
    if (roadEdges.isEmpty) return

    val districts = roadEdges.flatMap { case (a, b, _) => List(a, b) }
    val uniqueDistricts = districts.groupBy(_.id).values.map(_.head).toList
    val spineKeyOpt = districts.find(_.seatOfGovernment).flatMap { seat =>
      districts.filterNot(_.id == seat.id) match {
        case Nil => None
        case others =>
          val farthest = others.maxBy(other => distance(seat.position, other.position))
          val key = if (seat.id < farthest.id) (seat.id, farthest.id) else (farthest.id, seat.id)
          Some(key)
      }
    }

    roadEdges.foreach { case (a, b, isMain) =>
      val key = if (a.id < b.id) (a.id, b.id) else (b.id, a.id)
      val isSpine = spineKeyOpt.contains(key)
      val stroke = if (isSpine) 4.2f else if (isMain) 3.0f else 1.3f
      val color =
        if (isSpine) new Color(95, 72, 52, 230)
        else if (isMain) new Color(110, 88, 66, 220)
        else new Color(130, 106, 82, 180)
      g.setColor(color)
      g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))

      val startX = mapX + jitter(a.position.x, rand)
      val startY = mapY + jitter(a.position.y, rand)
      val endX = mapX + jitter(b.position.x, rand)
      val endY = mapY + jitter(b.position.y, rand)
      val midX = (startX + endX) / 2.0
      val midY = (startY + endY) / 2.0
      val dx = endX - startX
      val dy = endY - startY
      val len = math.max(1.0, math.hypot(dx, dy))
      val nx = -dy / len
      val ny = dx / len
      val bend = (rand.nextDouble() - 0.5) * 2 * (if (isMain) 50.0 else 32.0)
      val ctrlX = midX + nx * bend
      val ctrlY = midY + ny * bend
      val path = new Path2D.Double()
      path.moveTo(startX, startY)
      path.quadTo(ctrlX, ctrlY, endX, endY)
      g.draw(path)
    }

    val plazaColor = new Color(135, 112, 88, 125)
    val plazaStroke = new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    uniqueDistricts.foreach { district =>
      district.plazas.foreach { plaza =>
        val startX = mapX + jitter(district.position.x, rand)
        val startY = mapY + jitter(district.position.y, rand)
        val endX = mapX + plaza.center.x
        val endY = mapY + plaza.center.y
        val dx = endX - startX
        val dy = endY - startY
        val dist = math.hypot(dx, dy)
        if (dist > 12.0) {
          val midX = (startX + endX) / 2.0
          val midY = (startY + endY) / 2.0
          val nx = -dy / dist
          val ny = dx / dist
          val bend = (rand.nextDouble() - 0.5) * 2 * 18.0
          val ctrlX = midX + nx * bend
          val ctrlY = midY + ny * bend
          val path = new Path2D.Double()
          path.moveTo(startX, startY)
          path.quadTo(ctrlX, ctrlY, endX, endY)
          g.setColor(plazaColor)
          g.setStroke(plazaStroke)
          g.draw(path)
        }
      }
    }
  }

  private def drawDistrictLabels(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    mapWidth: Int,
    mapHeight: Int,
    districts: List[District],
    outlinePath: Path2D,
    roadSamples: List[(Double, Double)],
  ): Unit = {
    val fontSizes = List(18, 16, 14, 12)
    val poiCenters = districts.flatMap(_.pointsOfInterest).map(poi => (mapX + poi.location.x, mapY + poi.location.y))
    val poiRadius = 16.0
    val roadMargin = 10.0

    def intersectsPoi(centerX: Double, centerY: Double, width: Double, height: Double): Boolean = {
      val left = centerX - width / 2.0
      val right = centerX + width / 2.0
      val top = centerY - height / 2.0
      val bottom = centerY + height / 2.0
      poiCenters.exists { case (px, py) =>
        val closestX = math.max(left, math.min(px.toDouble, right))
        val closestY = math.max(top, math.min(py.toDouble, bottom))
        val dx = px - closestX
        val dy = py - closestY
        (dx * dx + dy * dy) <= (poiRadius * poiRadius)
      }
    }

    def intersectsRoad(centerX: Double, centerY: Double, width: Double, height: Double): Boolean = {
      val left = centerX - width / 2.0 - roadMargin
      val right = centerX + width / 2.0 + roadMargin
      val top = centerY - height / 2.0 - roadMargin
      val bottom = centerY + height / 2.0 + roadMargin
      roadSamples.exists { case (rx, ry) =>
        rx >= left && rx <= right && ry >= top && ry <= bottom
      }
    }

    districts.foreach { district =>
      val districtPoly = if (district.boundary.size >= 3) polygonFrom(district.boundary) else null
      val x = mapX + district.position.x
      val y = mapY + district.position.y
      val seatSuffix = if (district.seatOfGovernment) " • Seat" else ""
      val text = s"${district.id}. ${district.districtType}$seatSuffix"
      val paddingX = 10
      val paddingY = 6
      val boundaryCenter = centroid(district.boundary)
      val boundaryX = mapX + boundaryCenter.x
      val boundaryY = mapY + boundaryCenter.y
      val mapCenterX = mapX + mapWidth / 2.0
      val mapCenterY = mapY + mapHeight / 2.0

      val candidates = List(
        (x.toDouble, y.toDouble),
        (boundaryX.toDouble, boundaryY.toDouble),
        ((x + boundaryX) / 2.0, (y + boundaryY) / 2.0),
      )

      val chosen = fontSizes.view.flatMap { size =>
        val labelFont = new Font("Serif", Font.BOLD, size)
        g.setFont(labelFont)
        val metrics = g.getFontMetrics(labelFont)
        val textWidth = metrics.stringWidth(text)
        val textHeight = metrics.getHeight
        val rectW = textWidth + paddingX * 2
        val rectH = textHeight + paddingY * 2
        val totalWidth = rectW

        def rectInside(cx: Double, cy: Double): Boolean = {
          val left = cx - totalWidth / 2.0
          val right = cx + totalWidth / 2.0
          val top = cy - rectH / 2.0
          val bottom = cy + rectH / 2.0
          if (districtPoly == null)
            outlinePath.contains(left, top, totalWidth, rectH)
          else {
            val corners = List(
              new Coordinate(left, top),
              new Coordinate(right, top),
              new Coordinate(right, bottom),
              new Coordinate(left, bottom),
            )
            corners.forall(coord => districtPoly.contains(geometryFactory.createPoint(coord)))
          }
        }

        def findSpot(center: (Double, Double)): Option[(Double, Double)] = {
          val (baseX, baseY) = center
          val rings = List(0.0, 12.0, 24.0, 36.0)
          val angles = (0 until 8).map(_ * (Math.PI / 4.0))
          val points = rings.flatMap { r =>
            if (r == 0.0) List((baseX, baseY))
            else angles.map(a => (baseX + Math.cos(a) * r, baseY + Math.sin(a) * r)).toList
          }
          points.find { case (cx, cy) =>
            rectInside(cx, cy) &&
            !intersectsPoi(cx, cy, totalWidth, rectH) &&
            !intersectsRoad(cx, cy, totalWidth, rectH)
          }
        }

        def nudgeTowardMapCenter(center: (Double, Double)): Option[(Double, Double)] = {
          val (baseX, baseY) = center
          val steps = List(0.15, 0.3, 0.45, 0.6, 0.75, 0.9)
          steps
            .map { t => (baseX + (mapCenterX - baseX) * t, baseY + (mapCenterY - baseY) * t) }
            .find { case (cx, cy) => rectInside(cx, cy) }
        }

        val spot = candidates.view.flatMap(findSpot).headOption
          .orElse(candidates.find { case (cx, cy) => rectInside(cx, cy) })
          .orElse(candidates.view.flatMap(nudgeTowardMapCenter).headOption)
          .orElse(nudgeTowardMapCenter((boundaryX.toDouble, boundaryY.toDouble)))

        spot.map { case (cx, cy) =>
          val drawX = (cx - rectW / 2.0).round.toInt
          val drawY = (cy - rectH / 2.0).round.toInt
          (drawX, drawY, rectW, rectH, metrics.getAscent)
        }
      }.headOption

      chosen.foreach { case (drawX, drawY, rectW, rectH, ascent) =>
        g.setColor(new Color(246, 240, 222, 230))
        g.fillRoundRect(drawX, drawY, rectW, rectH, 12, 12)
        g.setColor(new Color(90, 70, 50, 200))
        g.setStroke(new BasicStroke(1.6f))
        g.drawRoundRect(drawX, drawY, rectW, rectH, 12, 12)
        g.setColor(new Color(60, 45, 30))
        g.drawString(text, drawX + paddingX, drawY + paddingY + ascent - 2)
      }
    }
  }

  private def drawLegend(
    g: java.awt.Graphics2D,
    settlement: Settlement,
    legendX: Int,
    legendY: Int,
    legendW: Int,
    legendH: Int,
  ): Unit = {
    val keyColumnWidth = 260
    val keyGap = 16

    g.setColor(new Color(235, 222, 198))
    g.fillRect(legendX, legendY, legendW, legendH)
    g.setColor(new Color(90, 70, 50))
    g.setStroke(new BasicStroke(2f))
    g.drawRect(legendX, legendY, legendW, legendH)

    val titleFont = new Font("Serif", Font.BOLD, 18)
    val bodyFontSizes = List(14, 13, 12, 11)
    val headerFontSizes = bodyFontSizes.map(size => new Font("Serif", Font.BOLD, size))
    g.setFont(titleFont)
    g.drawString("Keyed Index", legendX + 10, legendY + 24)

    val bodyStartY = legendY + 40
    val maxWidth = legendW - keyColumnWidth - keyGap - 20
    val availableHeight = legendY + legendH - 12 - bodyStartY

    def estimateHeight(fontSize: Int): Int = {
      val font = new Font("Serif", Font.PLAIN, fontSize)
      val metrics = g.getFontMetrics(font)
      val lineHeight = metrics.getHeight
      val spacing = math.max(2, lineHeight / 3)
      settlement.districts.foldLeft(0) { (acc, district) =>
        val seatLabel = if (district.seatOfGovernment) " (Seat)" else ""
        val header = s"${district.id}. ${district.districtType} (${district.alignment})$seatLabel"
        val headerLines = countWrappedLines(header, metrics, maxWidth)
        val poiLines = district.pointsOfInterest.foldLeft(0) { (poiAcc, poi) =>
          val detail = poi.tavern.map(t => s"${t.name} - ${t.knownFor}")
            .orElse(poi.shop.map(s => s"${s.name} - ${s.knownFor}"))
          val baseLine = detail.map(d => s"  ${poi.id}. ${poi.name}: $d").getOrElse(s"  ${poi.id}. ${poi.name}")
          val npcLine = poi.npc.map { npc =>
            val bg = npc.background.getOrElse("Unknown background")
            val personality = npc.personality.getOrElse("No personality")
            s"      NPC: ${npc.name} (${npc.ancestry}), $bg, $personality"
          }
          val baseLines = countWrappedLines(baseLine, metrics, maxWidth)
          val npcLines = npcLine.map(line => countWrappedLines(line, metrics, maxWidth)).getOrElse(0)
          poiAcc + baseLines + npcLines
        }
        acc + (headerLines + poiLines) * lineHeight + spacing
      }
    }

    val chosenSize = bodyFontSizes.find(size => estimateHeight(size) <= availableHeight).getOrElse(bodyFontSizes.last)
    val bodyFont = new Font("Serif", Font.PLAIN, chosenSize)
    val headerFont = new Font("Serif", Font.BOLD, chosenSize)
    g.setFont(bodyFont)
    val metrics = g.getFontMetrics
    val lineHeight = metrics.getHeight
    val spacing = math.max(2, lineHeight / 3)
    var cursorY = bodyStartY

    settlement.districts.foreach { district =>
      if (cursorY <= legendY + legendH - 12) {
      val seatLabel = if (district.seatOfGovernment) " (Seat)" else ""
      val header = s"${district.id}. ${district.districtType} (${district.alignment})$seatLabel"
      g.setFont(headerFont)
      cursorY = drawWrappedText(g, header, legendX + 10, cursorY, maxWidth, lineHeight)
      g.setFont(bodyFont)
      district.pointsOfInterest.foreach { poi =>
        val detail = poi.tavern.map(t => s"${t.name} - ${t.knownFor}")
          .orElse(poi.shop.map(s => s"${s.name} - ${s.knownFor}"))
        val baseLine = detail.map(d => s"  ${poi.id}. ${poi.name}: $d").getOrElse(s"  ${poi.id}. ${poi.name}")
        cursorY = drawWrappedText(g, baseLine, legendX + 10, cursorY, maxWidth, lineHeight)
        val npcLine = poi.npc.map { npc =>
          val bg = npc.background.getOrElse("Unknown background")
          val personality = npc.personality.getOrElse("No personality")
          s"      NPC: ${npc.name} (${npc.ancestry}), $bg, $personality"
        }
        npcLine.foreach { line =>
          cursorY = drawWrappedText(g, line, legendX + 10, cursorY, maxWidth, lineHeight)
        }
      }
      cursorY += spacing
      }
    }

    val keyX = legendX + legendW - keyColumnWidth + 12
    val keyTitleY = legendY + 24
    val keyLineStart = legendX + legendW - keyColumnWidth - (keyGap / 2)
    g.setColor(new Color(90, 70, 50))
    g.setStroke(new BasicStroke(1.4f))
    g.drawLine(keyLineStart, legendY + 8, keyLineStart, legendY + legendH - 8)

    g.setFont(titleFont)
    g.drawString("District Key", keyX, keyTitleY)
    g.setFont(bodyFont)
    var keyCursorY = legendY + 44
    val swatchSize = 14
    settlement.districts.zipWithIndex.foreach { case (district, idx) =>
      val style = districtStyleFor(district, idx, settlement.layout.seed)
      drawTextureSwatch(g, keyX, keyCursorY - swatchSize + 3, swatchSize, style)
      g.setColor(new Color(90, 70, 50))
      g.drawRect(keyX, keyCursorY - swatchSize + 3, swatchSize, swatchSize)
      g.setColor(new Color(60, 45, 30))
      g.drawString(s"${district.id}. ${district.districtType}", keyX + swatchSize + 8, keyCursorY)
      keyCursorY += lineHeight + 4
    }
  }

  private def drawTitle(g: java.awt.Graphics2D, settlement: Settlement): Unit = {
    val titleFont = new Font("Serif", Font.BOLD, 26)
    g.setFont(titleFont)
    g.setColor(new Color(60, 45, 30))
    val title = s"${settlement.name} (${settlement.settlementType.name}, ${settlement.alignment})"
    val width = g.getFontMetrics.stringWidth(title)
    g.drawString(title, (pageWidth - width) / 2, pageMargin - 10)
  }

  private def drawTextureSwatch(
    g: java.awt.Graphics2D,
    x: Int,
    y: Int,
    size: Int,
    style: DistrictStyle,
  ): Unit = {
    val path = new Path2D.Double()
    path.moveTo(x.toDouble, y.toDouble)
    path.lineTo(x + size, y.toDouble)
    path.lineTo(x + size, y + size)
    path.lineTo(x.toDouble, y + size)
    path.closePath()
    g.setColor(style.fill)
    g.fill(path)
    drawTextureInBounds(g, path, style.pattern, style.fill)
  }

  private def drawWrappedText(
    g: java.awt.Graphics2D,
    text: String,
    x: Int,
    y: Int,
    maxWidth: Int,
    lineHeight: Int,
  ): Int = {
    val words = text.split("\\s+").toList
    val metrics = g.getFontMetrics
    var line   = ""
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

  private def countWrappedLines(
    text: String,
    metrics: java.awt.FontMetrics,
    maxWidth: Int,
  ): Int = {
    val words = text.split("\\s+").toList
    var line = ""
    var count = 0
    words.foreach { word =>
      val test = if (line.isEmpty) word else s"$line $word"
      if (metrics.stringWidth(test) <= maxWidth) {
        line = test
      } else {
        if (line.nonEmpty) count += 1
        line = word
      }
    }
    if (line.nonEmpty) count += 1
    math.max(1, count)
  }

  private def jitter(value: Int, rand: Random): Int =
    value + rand.nextInt(13) - 6

  private def polygonPath(mapX: Int, mapY: Int, points: List[Point], rand: Random): Path2D = {
    val path = new Path2D.Double()
    points.headOption.foreach { first =>
      path.moveTo(mapX + jitter(first.x, rand), mapY + jitter(first.y, rand))
      points.tail.foreach { p =>
        path.lineTo(mapX + jitter(p.x, rand), mapY + jitter(p.y, rand))
      }
      path.closePath()
    }
    path
  }

}

object SettlementServer {
  val live: ZLayer[
    NameServer with RaceServer with PersonalityServer with BackgroundServer with NpcQualityServer with SettlementNameServer,
    Nothing,
    SettlementServer,
  ] =
    ZLayer.fromFunction(SettlementServer.apply _)
}
