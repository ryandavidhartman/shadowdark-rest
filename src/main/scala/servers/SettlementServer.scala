package servers

import models._
import zio._

import java.awt.{BasicStroke, Color, Font, GradientPaint, RenderingHints}
import java.awt.geom.{Path2D, Ellipse2D}
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.ThreadLocalRandom
import javax.imageio.ImageIO
import scala.util.Random

private[servers] final case class PoiTemplate(
  name: String,
  kind: String,
  tavernQuality: Option[String] = None,
  shopQuality: Option[String] = None,
  shopTypeOverride: Option[String] = None,
)

final case class SettlementServer() {
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

  private val alignments = List(
    "Lawful",
    "Lawful",
    "Lawful",
    "Neutral",
    "Neutral",
    "Chaotic",
  )

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

  private def pickOne[A](values: List[A]): A =
    values(rng.nextInt(values.length))

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

  private def buildPoints(count: Int): List[Point] = {
    val minDistance = 140
    (1 to count).foldLeft(List.empty[Point]) { (acc, _) =>
      var point: Option[Point] = None
      var attempts             = 0
      while (point.isEmpty && attempts < 50) {
        attempts += 1
        val candidate =
          Point(
            rng.nextInt(mapPadding, mapWidth - mapPadding),
            rng.nextInt(mapPadding, mapHeight - mapPadding),
          )
        val farEnough = acc.forall(existing => distance(existing, candidate) >= minDistance)
        if (farEnough || attempts > 20) point = Some(candidate)
      }
      acc :+ point.getOrElse(Point(mapWidth / 2, mapHeight / 2))
    }
  }

  private def distance(a: Point, b: Point): Double = {
    val dx = a.x - b.x
    val dy = a.y - b.y
    Math.sqrt(dx.toDouble * dx.toDouble + dy.toDouble * dy.toDouble)
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

  def randomSettlement: Task[Settlement] =
    ZIO.attempt {
      val settlementType = settlementTypeForRoll(rollDie(6))
      val seed           = rng.nextLong()
      val dieRolls       = (1 to settlementType.diceCount).map(_ => rollDie(settlementType.dieSides)).toList
      val points         = buildPoints(settlementType.diceCount)
      val settlementAlignment = rollAlignment
      val maxRoll        = dieRolls.max
      val seatIndex      = dieRolls.indexOf(maxRoll) + 1
      val outline        = expandHull(convexHull(points), 140, seed)
      val districts = dieRolls.zip(points).zipWithIndex.map { case ((roll, point), idx) =>
        val districtType = districtTypeForRoll(roll)
        val poiCount     = rollDie(4)
        val pois = (1 to poiCount).map { _ =>
          val template = rollPoiTemplate(districtType)
          val tavern   = template.tavernQuality.map(buildTavern)
          val shop     = template.shopQuality.map(q => buildShop(q, template.shopTypeOverride))
          PointOfInterest(template.name, template.kind, tavern, shop)
        }.toList
        District(
          id = idx + 1,
          roll = roll,
          districtType = districtType,
          alignment = rollAlignment,
          seatOfGovernment = idx + 1 == seatIndex,
          position = point,
          pointsOfInterest = pois,
        )
      }

      Settlement(
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

  def renderSettlementPng(settlement: Settlement): Task[Array[Byte]] =
    ZIO.attempt {
      val image = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB)
      val g     = image.createGraphics()
      try {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val parchmentTop    = new Color(245, 231, 204)
        val parchmentBottom = new Color(230, 214, 183)
        g.setPaint(new GradientPaint(0f, 0f, parchmentTop, 0f, pageHeight.toFloat, parchmentBottom))
        g.fillRect(0, 0, pageWidth, pageHeight)

        val mapX      = pageMargin
        val mapY      = pageMargin
        val mapWidth  = settlement.layout.width
        val mapHeight = settlement.layout.height

        g.setColor(new Color(235, 222, 198))
        g.fillRect(mapX, mapY, mapWidth, mapHeight)

        drawGrid(g, mapX, mapY, mapWidth, mapHeight, settlement.layout.gridSize)
        drawOutline(g, mapX, mapY, settlement.layout.outline, new Random(settlement.layout.seed))
        drawDistricts(g, mapX, mapY, settlement, new Random(settlement.layout.seed + 19))
        drawLegend(g, settlement)
        drawTitle(g, settlement)

        val output = new ByteArrayOutputStream()
        try {
          ImageIO.write(image, "png", output)
          output.toByteArray
        } finally output.close()
      } finally g.dispose()
    }

  private def drawGrid(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    mapWidth: Int,
    mapHeight: Int,
    gridSize: Int,
  ): Unit = {
    g.setColor(new Color(185, 170, 140, 90))
    g.setStroke(new BasicStroke(1f))
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

  private def drawOutline(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    outline: List[Point],
    rand: Random,
  ): Unit = {
    if (outline.nonEmpty) {
      val path = new Path2D.Double()
      val first = outline.head
      path.moveTo(mapX + jitter(first.x, rand), mapY + jitter(first.y, rand))
      outline.tail.foreach { p =>
        path.lineTo(mapX + jitter(p.x, rand), mapY + jitter(p.y, rand))
      }
      path.closePath()
      g.setColor(new Color(90, 70, 50))
      g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
      g.draw(path)
    }
  }

  private def drawDistricts(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    settlement: Settlement,
    rand: Random,
  ): Unit = {
    val palette = List(
      new Color(198, 132, 118, 150),
      new Color(155, 181, 196, 150),
      new Color(173, 196, 156, 150),
      new Color(214, 190, 127, 150),
      new Color(178, 156, 196, 150),
      new Color(206, 170, 140, 150),
      new Color(165, 196, 178, 150),
      new Color(201, 167, 185, 150),
    )

    val centers = settlement.districts.map(_.position)
    val radiusByDistrict = settlement.districts.map { district =>
      val distances = centers.filterNot(_ == district.position).map(distance(district.position, _))
      val minDist   = if (distances.nonEmpty) distances.min else 180.0
      val base      = math.max(80.0, minDist * 0.45)
      base + rand.nextInt(20)
    }

    settlement.districts.zipWithIndex.foreach { case (district, idx) =>
      val radius = radiusByDistrict(idx)
      val path   = blobPath(mapX + district.position.x, mapY + district.position.y, radius, rand)
      g.setColor(palette(idx % palette.length))
      g.fill(path)
      g.setColor(new Color(90, 70, 50, 180))
      g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
      g.draw(path)
    }

    drawRoads(g, mapX, mapY, settlement.districts, rand)
    drawDistrictLabels(g, mapX, mapY, settlement.districts)
  }

  private def drawRoads(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    districts: List[District],
    rand: Random,
  ): Unit = {
    g.setColor(new Color(120, 96, 72, 200))
    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    districts.foreach { district =>
      val neighbors = districts
        .filterNot(_.id == district.id)
        .sortBy(other => distance(district.position, other.position))
        .take(2)
      neighbors.foreach { neighbor =>
        val path = new Path2D.Double()
        path.moveTo(mapX + jitter(district.position.x, rand), mapY + jitter(district.position.y, rand))
        path.lineTo(mapX + jitter(neighbor.position.x, rand), mapY + jitter(neighbor.position.y, rand))
        g.draw(path)
      }
    }
  }

  private def drawDistrictLabels(
    g: java.awt.Graphics2D,
    mapX: Int,
    mapY: Int,
    districts: List[District],
  ): Unit = {
    val labelFont = new Font("Serif", Font.BOLD, 22)
    g.setFont(labelFont)
    val metrics = g.getFontMetrics
    districts.foreach { district =>
      val x = mapX + district.position.x
      val y = mapY + district.position.y
      val label = district.id.toString
      val width = metrics.stringWidth(label)
      val circle = new Ellipse2D.Double(x - 16, y - 16, 32, 32)
      g.setColor(new Color(245, 238, 220, 220))
      g.fill(circle)
      g.setColor(new Color(70, 55, 40))
      g.setStroke(new BasicStroke(2f))
      g.draw(circle)
      g.setColor(new Color(60, 45, 30))
      g.drawString(label, x - width / 2, y + metrics.getAscent / 2 - 2)
      if (district.seatOfGovernment) {
        g.setColor(new Color(140, 90, 40))
        g.drawString("S", x + 14, y - 10)
      }
    }
  }

  private def drawLegend(g: java.awt.Graphics2D, settlement: Settlement): Unit = {
    val legendX = pageMargin
    val legendY = pageMargin + mapHeight + 20
    val legendW = pageWidth - (pageMargin * 2)
    val legendH = legendHeight - 20

    g.setColor(new Color(235, 222, 198))
    g.fillRect(legendX, legendY, legendW, legendH)
    g.setColor(new Color(90, 70, 50))
    g.setStroke(new BasicStroke(2f))
    g.drawRect(legendX, legendY, legendW, legendH)

    val titleFont = new Font("Serif", Font.BOLD, 18)
    val bodyFont  = new Font("Serif", Font.PLAIN, 14)
    g.setFont(titleFont)
    g.drawString("Keyed Index", legendX + 10, legendY + 24)

    g.setFont(bodyFont)
    val lineHeight = g.getFontMetrics.getHeight
    var cursorY = legendY + 40
    val maxWidth = legendW - 20

    settlement.districts.foreach { district =>
      val seatLabel = if (district.seatOfGovernment) " (Seat)" else ""
      val header = s"${district.id}. ${district.districtType} (${district.alignment})$seatLabel"
      cursorY = drawWrappedText(g, header, legendX + 10, cursorY, maxWidth, lineHeight)
      district.pointsOfInterest.foreach { poi =>
        val detail = poi.tavern.map(t => s"${t.name} - ${t.knownFor}")
          .orElse(poi.shop.map(s => s"${s.name} - ${s.knownFor}"))
        val line = detail.map(d => s"  - ${poi.name}: $d").getOrElse(s"  - ${poi.name}")
        cursorY = drawWrappedText(g, line, legendX + 10, cursorY, maxWidth, lineHeight)
      }
      cursorY += lineHeight / 3
    }
  }

  private def drawTitle(g: java.awt.Graphics2D, settlement: Settlement): Unit = {
    val titleFont = new Font("Serif", Font.BOLD, 26)
    g.setFont(titleFont)
    g.setColor(new Color(60, 45, 30))
    val title = s"${settlement.settlementType.name} (${settlement.alignment})"
    val width = g.getFontMetrics.stringWidth(title)
    g.drawString(title, (pageWidth - width) / 2, pageMargin - 10)
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

  private def jitter(value: Int, rand: Random): Int =
    value + rand.nextInt(13) - 6

  private def blobPath(cx: Int, cy: Int, radius: Double, rand: Random): Path2D = {
    val points = 8
    val path   = new Path2D.Double()
    (0 until points).foreach { i =>
      val angle = (Math.PI * 2 / points) * i
      val variance = 0.8 + rand.nextDouble() * 0.4
      val r = radius * variance
      val x = cx + Math.cos(angle) * r
      val y = cy + Math.sin(angle) * r
      if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.closePath()
    path
  }
}

object SettlementServer {
  val live: ZLayer[Any, Nothing, SettlementServer] =
    ZLayer.succeed(SettlementServer())
}
