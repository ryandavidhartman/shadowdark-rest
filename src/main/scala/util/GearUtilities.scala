package util

import models.Item

import java.util.concurrent.ThreadLocalRandom
import scala.util.Random

object GearUtilities {

  final case class GearLoadout(
    gear: List[String],
    freeGear: List[String],
    attacks: List[String],
    armorClass: Int,
    usedSlots: Double,
    totalSlots: Double,
  )

  private def pickOne[A](values: List[A], rng: ThreadLocalRandom): Option[A] =
    if (values.isEmpty) None else Some(values(rng.nextInt(values.size)))

  private def formatMod(mod: Int): String =
    if (mod >= 0) s"+$mod" else s"$mod"

  private def allowsDexBonus(description: Option[String]): Boolean =
    description.exists(_.toLowerCase.contains("dex"))

  def selectStartingGear(
    items: List[Item],
    allowedWeapons: List[String],
    allowedArmor: List[String],
    strengthScore: Int,
    conMod: Int,
    dexMod: Int,
    strMod: Int,
    baseArmorClass: Int,
    rng: ThreadLocalRandom,
  ): GearLoadout = {
    val capacity         = math.max(strengthScore.toDouble, 10.0)
    val baseSlots        = capacity + (if (conMod > 0) conMod else 0)
    val totalSlotsInt    = math.max(1, math.ceil(baseSlots).toInt)
    val nonMagical       = items.filter(_.magical.contains(false))
    var remainingSlots   = totalSlotsInt
    var gearNames        = List.empty[String]
    var freeGear         = List.empty[String]
    var usedNames        = Set.empty[String]
    val random           = new Random(rng.nextLong())
    val crawlingKitItems = List("Backpack", "Flint and Steel", "Torch", "Rations", "Iron Spikes (10)", "Grappling Hook", "Rope, 60'")
    val gearDedupGroups  = List(
      Set("Flask", "Flask or Bottle", "Empty Bottle", "Bottle", "Flask, Leather", "Flask, Metal")
    )

    def slotsOf(item: Item): Double = item.slots.getOrElse(1.0)
    def slotCount(item: Item): Int   = math.max(1, math.ceil(slotsOf(item)).toInt)
    def canonicalName(name: String): String =
      gearDedupGroups.find(_.exists(_.equalsIgnoreCase(name))).flatMap(_.headOption).getOrElse(name)

    def weaponAllowed(item: Item): Boolean = {
      if (!item.itemType.exists(_.equalsIgnoreCase("Weapon"))) return false
      val allowedLower = allowedWeapons.map(_.toLowerCase)
      allowedLower.exists(_.contains("all weapons")) ||
        allowedLower.contains(item.name.toLowerCase)
    }

    def armorAllowed(item: Item): Boolean = {
      val allowedLower = allowedArmor.map(_.toLowerCase)
      allowedLower.exists(_.contains("all armor")) ||
      (item.name.toLowerCase.contains("shield") && allowedLower.exists(_.contains("shields"))) ||
      allowedLower.exists(a => item.name.toLowerCase == a)
    }

    val armorCandidates = nonMagical
      .filter(i => i.itemType.exists(_.equalsIgnoreCase("Armor")) && i.ac.isDefined)
      .filter(armorAllowed)
      .filter(i => slotCount(i) <= remainingSlots)
      .sortBy(i => (-i.ac.getOrElse(0), slotsOf(i)))

    val armor = pickOne(armorCandidates, rng)
    armor.foreach { a =>
      val count = slotCount(a)
      remainingSlots -= count
      val canon = canonicalName(a.name)
      gearNames = gearNames ++ List.fill(count)(canon)
      usedNames = usedNames + canon
    }

    val shieldCandidates = nonMagical
      .filter(i => i.defenseBonus.exists(_ > 0))
      .filter(armorAllowed)
      .filter(i => slotCount(i) <= remainingSlots)
      .filterNot(i => usedNames.exists(n => n.toLowerCase.contains("shield") || n.toLowerCase.contains("buckler")))

    def firstThatFits(cands: List[Item]): Option[Item] =
      random
        .shuffle(cands)
        .find(i => slotCount(i) <= remainingSlots)

    val rangedWeapons = nonMagical
      .filter(weaponAllowed)
      .filter(_.attackType.exists(_.toLowerCase.contains("ranged")))
      .filter(i => slotCount(i) <= remainingSlots)
    val ranged = firstThatFits(rangedWeapons)
    ranged.foreach { r =>
      val count = slotCount(r)
      remainingSlots -= count
      val canon = canonicalName(r.name)
      gearNames = gearNames ++ List.fill(count)(canon)
      usedNames = usedNames + canon
    }

    val meleeWeapons = nonMagical
      .filter(weaponAllowed)
      .filter(_.attackType.exists(_.toLowerCase.contains("melee")))
      .filter(i => slotCount(i) <= remainingSlots)
    val melee = firstThatFits(meleeWeapons)
    melee.foreach { m =>
      val count = slotCount(m)
      remainingSlots -= count
      val canon = canonicalName(m.name)
      gearNames = gearNames ++ List.fill(count)(canon)
      usedNames = usedNames + canon
    }
    val twoHandedMelee = melee.exists(_.twoHanded.contains(true))

    val shield =
      if (twoHandedMelee) None
      else firstThatFits(shieldCandidates)
    shield.foreach { s =>
      val count = slotCount(s)
      remainingSlots -= count
      val canon = canonicalName(s.name)
      gearNames = gearNames ++ List.fill(count)(canon)
      usedNames = usedNames + canon
    }

    val fillerGear = random.shuffle(
      nonMagical
        .filter(i => i.itemType.exists(t => t.equalsIgnoreCase("Equipment") || t.equalsIgnoreCase("Gear")))
        .filter(i => slotsOf(i) > 0 && slotsOf(i) <= remainingSlots)
        .sortBy(slotsOf),
    )

    fillerGear.take(3).foreach { g =>
      val count = slotCount(g)
      val canon = canonicalName(g.name)
      if (count <= remainingSlots && !usedNames.contains(canon)) {
        remainingSlots -= count
        gearNames = gearNames ++ List.fill(count)(canon)
        usedNames = usedNames + canon
      }
    }

    val zeroSlotGear = random.shuffle(
      nonMagical
        .filter(i => slotsOf(i) <= 0)
        .filter(i => i.itemType.exists(t => t.equalsIgnoreCase("Equipment") || t.equalsIgnoreCase("Gear"))),
    )

    zeroSlotGear.take(3).foreach { g =>
      val canon = canonicalName(g.name)
      if (!usedNames.contains(canon)) {
        freeGear = freeGear :+ canon
        usedNames = usedNames + canon
      }
    }

    if (remainingSlots > 0) {
      val extraCrawling = random.shuffle(crawlingKitItems).map(canonicalName).filterNot(n => usedNames.contains(n)).take(remainingSlots)
      val empties       = List.fill(Math.max(0, remainingSlots - extraCrawling.size))("Empty")
      gearNames = gearNames ++ extraCrawling ++ empties
      remainingSlots = 0
    }

    // annotate slots with indices for clarity
    val indexedGear = gearNames.zipWithIndex.map { case (name, idx) => s"Slot ${idx + 1}: $name" }

    val armorAc = armor.flatMap { a =>
      val base = a.ac.getOrElse(baseArmorClass)
      val dex  = if (allowsDexBonus(a.description)) dexMod else 0
      Some(math.max(base + dex, baseArmorClass))
    }.getOrElse(baseArmorClass)

    val acWithShield = armorAc + shield.flatMap(_.defenseBonus).getOrElse(0)

    val attacks =
      List(melee, ranged).flatten.map { weapon =>
        val useDex = weapon.finesse.contains(true) || weapon.attackType.exists(_.toLowerCase.contains("ranged"))
        val mod    = if (useDex) dexMod else strMod
        s"${weapon.name} attack ${formatMod(mod)} (${weapon.damage.getOrElse("1d6")})"
      }

    GearLoadout(
      gear = indexedGear,
      freeGear = freeGear,
      attacks = attacks,
      armorClass = acWithShield,
      usedSlots = totalSlotsInt - remainingSlots,
      totalSlots = totalSlotsInt.toDouble,
    )
  }
}
