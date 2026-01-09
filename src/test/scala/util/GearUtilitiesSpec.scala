package util

import zio.test.{ZIOSpecDefault, assertTrue, suite, test}

object GearUtilitiesSpec extends ZIOSpecDefault {
  override def spec =
    suite("GearUtilities")(
      test("formatGearSlots pads to total slots with Empty") {
        val gear = List("Oil, flask", "Caltrops (one bag)", "Shortbow and 5 arrows")
        val formatted = GearUtilities.formatGearSlots(gear, 10)
        assertTrue(
          formatted.length == 10,
          formatted.head == "Slot 1: Oil, flask",
          formatted(2) == "Slot 3: Shortbow and 5 arrows",
          formatted(9) == "Slot 10: Empty",
        )
      },
      test("formatGearSlots clamps to total slots") {
        val gear = List("Rope", "Torch", "Rations")
        val formatted = GearUtilities.formatGearSlots(gear, 2)
        assertTrue(
          formatted.length == 2,
          formatted == List("Slot 1: Rope", "Slot 2: Torch"),
        )
      },
      test("formatGearSlots keeps minimum slot count at 10 for zero-level expectation") {
        val gear = List("Oil, flask")
        val totalSlots = math.max(7, 10)
        val formatted = GearUtilities.formatGearSlots(gear, totalSlots)
        assertTrue(
          formatted.length == 10,
          formatted.head == "Slot 1: Oil, flask",
          formatted.last == "Slot 10: Empty",
        )
      },
    )
}
