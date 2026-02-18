package routes

import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem}
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.{Content, MediaType, Schema => SwaggerSchema}
import io.swagger.v3.oas.models.parameters.{Parameter, RequestBody}
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.Components

import scala.jdk.CollectionConverters._

object OpenAPIGen {

  def generateSpec(): String = {
    val openAPI = new OpenAPI()

    val info = new Info()
      .title("Shadowdark REST API")
      .version("1.0.0")
      .description("Shadowdark content and generators API.")
    openAPI.info(info)

    openAPI.servers(
      List(new Server().url("http://localhost:8081").description("Local development")).asJava
    )

    addSystemEndpoints(openAPI)
    addDocsEndpoints(openAPI)
    addContentEndpoints(openAPI)
    addSettlementEndpoints(openAPI)
    addDungeonEndpoints(openAPI)
    addHexEndpoints(openAPI)
    addSchemaDefinitions(openAPI)

    Json.pretty(openAPI)
  }

  private def addSystemEndpoints(openAPI: OpenAPI): Unit = {
    addGetPath(
      openAPI,
      "/",
      summary = "Service banner",
      tag = "System",
      responseDescription = "Service banner text",
      responseSchema = textSchema(),
      contentType = "text/plain",
      responseExample = Some("Shadowdark REST API")
    )
    addGetPath(
      openAPI,
      "/health",
      summary = "Health check",
      tag = "System",
      responseDescription = "OK",
      responseSchema = textSchema(),
      contentType = "text/plain",
      responseExample = Some("OK")
    )
  }

  private def addDocsEndpoints(openAPI: OpenAPI): Unit = {
    addGetPath(
      openAPI,
      "/api/openapi.json",
      summary = "OpenAPI specification",
      tag = "System",
      responseDescription = "OpenAPI JSON",
      responseSchema = refSchema("GenericObject"),
      contentType = "application/json",
      responseExample = Some(exampleMap("openapi" -> "3.0.3", "info" -> exampleMap("title" -> "Shadowdark REST API")))
    )
    addGetPath(
      openAPI,
      "/api/docs",
      summary = "Swagger UI",
      tag = "System",
      responseDescription = "Swagger UI HTML",
      responseSchema = textSchema(),
      contentType = "text/html",
      responseExample = Some("<!DOCTYPE html>")
    )
  }

  private def addContentEndpoints(openAPI: OpenAPI): Unit = {
    addGetPath(
      openAPI,
      "/names",
      "List names",
      "Content",
      "Names list",
      arrayRefSchema("Name"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439011",
            "name" -> "Aria",
            "race" -> "Human",
            "gender" -> "Female",
            "firstName" -> java.lang.Boolean.TRUE,
            "lastName" -> java.lang.Boolean.FALSE
          )
        )
      )
    )
    addPostPath(
      openAPI,
      "/names",
      "Create a name",
      "Content",
      requestSchemaRef = "NameCreate",
      responseSchemaRef = "Name",
      responseCode = "201",
      responseDescription = "Created name",
      requestExample = Some(
        exampleMap(
          "name" -> "Aria",
          "race" -> "Human",
          "gender" -> "Female",
          "firstName" -> java.lang.Boolean.TRUE,
          "lastName" -> java.lang.Boolean.FALSE
        )
      ),
      responseExample = Some(
        exampleMap(
          "_id" -> "507f1f77bcf86cd799439011",
          "name" -> "Aria",
          "race" -> "Human",
          "gender" -> "Female",
          "firstName" -> java.lang.Boolean.TRUE,
          "lastName" -> java.lang.Boolean.FALSE
        )
      )
    )

    addGetPath(
      openAPI,
      "/races",
      "List races",
      "Content",
      "Race list",
      arrayRefSchema("Race"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439011",
            "race" -> "Human",
            "ability" -> exampleMap("name" -> "Adaptable", "description" -> "Gain one extra talent."),
            "languages" -> exampleList("Common"),
            "chance" -> new java.lang.Double(0.5)
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/personalities",
      "List personalities",
      "Content",
      "Personalities list",
      arrayRefSchema("Personality"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439015",
            "name" -> "Stoic",
            "alignment" -> "Neutral"
          )
        )
      )
    )
    addPostPath(
      openAPI,
      "/personalities",
      "Create a personality",
      "Content",
      requestSchemaRef = "PersonalityCreate",
      responseSchemaRef = "Personality",
      responseCode = "201",
      responseDescription = "Created personality",
      requestExample = Some(
        exampleMap(
          "name" -> "Stoic",
          "alignment" -> "Neutral"
        )
      ),
      responseExample = Some(
        exampleMap(
          "_id" -> "507f1f77bcf86cd799439015",
          "name" -> "Stoic",
          "alignment" -> "Neutral"
        )
      )
    )
    addGetPath(
      openAPI,
      "/backgrounds",
      "List backgrounds",
      "Content",
      "Background list",
      arrayRefSchema("Background"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439016",
            "name" -> "Urchin",
            "range" -> exampleMap("min" -> new java.lang.Integer(1), "max" -> new java.lang.Integer(3)),
            "possessions" -> "Ragged cloak",
            "details" -> "Streetwise and wary"
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/classes",
      "List character classes",
      "Content",
      "Class list",
      arrayRefSchema("CharacterClass"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439017",
            "name" -> "Fighter",
            "weapons" -> exampleList("Any"),
            "armor" -> exampleList("Any"),
            "hitPointsPerLevel" -> "1d8",
            "abilityPriority" -> exampleList("Strength", "Constitution"),
            "features" -> exampleList(exampleMap("name" -> "Fighting Style", "description" -> "Gain +1 attack.")),
            "spellcasting" -> null,
            "talents" -> exampleList(
              exampleMap(
                "range" -> exampleMap("min" -> new java.lang.Integer(2), "max" -> new java.lang.Integer(12)),
                "effect" -> "Cleave"
              )
            ),
            "titles" -> exampleList(
              exampleMap(
                "levels" -> exampleMap("min" -> new java.lang.Integer(1), "max" -> new java.lang.Integer(3)),
                "lawful" -> "Squire",
                "chaotic" -> "Ruffian",
                "neutral" -> "Mercenary"
              )
            )
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/spells",
      "List spells",
      "Content",
      "Spell list",
      arrayRefSchema("Spell"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439012",
            "name" -> "Light",
            "tier" -> new java.lang.Integer(1),
            "castingAttribute" -> exampleList("Wisdom"),
            "description" -> "A small object shines with torchlight."
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/items",
      "List items",
      "Content",
      "Item list",
      arrayRefSchema("Item"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439013",
            "name" -> "Shortsword",
            "itemType" -> "Weapon",
            "slots" -> new java.lang.Integer(1),
            "damage" -> "1d6",
            "damageType" -> "Piercing"
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/titles",
      "List titles",
      "Content",
      "Title list",
      arrayRefSchema("Title"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439018",
            "characterClass" -> "Fighter",
            "alignment" -> "Lawful",
            "minLevel" -> new java.lang.Integer(1),
            "maxLevel" -> new java.lang.Integer(3),
            "title" -> "Squire"
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/deities",
      "List deities",
      "Content",
      "Deity list",
      arrayRefSchema("Deity"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439019",
            "name" -> "Saint Tarrin",
            "alignment" -> "Lawful",
            "description" -> "Patron of guardians."
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/languages",
      "List languages",
      "Content",
      "Language list",
      arrayRefSchema("LanguageEntry"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439020",
            "name" -> "Common",
            "speakers" -> "Most civilizations",
            "rarity" -> "Common"
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/monsters",
      "List monsters",
      "Content",
      "Monster list",
      arrayRefSchema("Monster"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439021",
            "name" -> "Goblin",
            "flavourText" -> "Small, cunning raiders.",
            "attacks" -> "Spear +2 (1d6)",
            "stats" -> exampleMap(
              "ac" -> "12",
              "hp" -> "5",
              "mv" -> "30",
              "str" -> "8",
              "dex" -> "12",
              "con" -> "10",
              "int" -> "8",
              "wis" -> "8",
              "cha" -> "6"
            ),
            "alignment" -> "Chaotic",
            "level" -> "1",
            "specials" -> exampleList()
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/settlement-names",
      "List settlement names",
      "Content",
      "Settlement name list",
      arrayRefSchema("SettlementName"),
      responseExample = Some(
        exampleList(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439022",
            "name" -> "Greyhaven",
            "settlementType" -> "Town"
          )
        )
      )
    )

    addGetPath(
      openAPI,
      "/random-character",
      "Generate a random character",
      "Characters",
      "Character JSON",
      refSchema("Character"),
      queryParameters = List(
        queryParam("zeroLevel", "Use true or 1 to generate a 0-level character", "string")
      ),
      responseExample = Some(
        exampleMap(
          "_id" -> "507f1f77bcf86cd799439014",
          "name" -> "Bran",
          "ancestry" -> "Human",
          "characterClass" -> "Fighter",
          "level" -> new java.lang.Integer(1),
          "xp" -> new java.lang.Integer(0),
          "xpForNextLevel" -> new java.lang.Integer(10),
          "abilities" -> exampleMap(
            "strength" -> exampleMap("score" -> new java.lang.Integer(14), "modifier" -> new java.lang.Integer(2)),
            "dexterity" -> exampleMap("score" -> new java.lang.Integer(10), "modifier" -> new java.lang.Integer(0)),
            "constitution" -> exampleMap("score" -> new java.lang.Integer(12), "modifier" -> new java.lang.Integer(1)),
            "intelligence" -> exampleMap("score" -> new java.lang.Integer(9), "modifier" -> new java.lang.Integer(-1)),
            "wisdom" -> exampleMap("score" -> new java.lang.Integer(10), "modifier" -> new java.lang.Integer(0)),
            "charisma" -> exampleMap("score" -> new java.lang.Integer(8), "modifier" -> new java.lang.Integer(-1))
          ),
          "hitPoints" -> new java.lang.Integer(8),
          "armorClass" -> new java.lang.Integer(14),
          "features" -> exampleList(exampleMap("name" -> "Fighting Style", "description" -> "Gain +1 attack.")),
          "talents" -> exampleList("Brutal Strike"),
          "spells" -> exampleList(),
          "attacks" -> exampleList("Shortsword +2 (1d6)"),
          "gear" -> exampleList("Slot 1: Shortsword", "Slot 2: Shield"),
          "languages" -> exampleList("Common"),
          "personalities" -> exampleList("Stoic"),
          "goldPieces" -> new java.lang.Integer(12),
          "silverPieces" -> new java.lang.Integer(0),
          "copperPieces" -> new java.lang.Integer(0),
          "freeToCarry" -> exampleList("Torch", "Rations")
        )
      )
    )
    addGetPath(
      openAPI,
      "/random-character.pdf",
      "Generate a random character PDF",
      "Characters",
      "Character PDF",
      binarySchema(),
      contentType = "application/pdf",
      queryParameters = List(
        queryParam("zeroLevel", "Use true or 1 to generate a 0-level character", "string")
      )
    )
  }

  private def addSettlementEndpoints(openAPI: OpenAPI): Unit = {
    addGetPath(
      openAPI,
      "/settlements/random",
      "Generate a random settlement",
      "Settlements",
      "Settlement JSON",
      refSchema("Settlement"),
      responseExample = Some(
        exampleMap(
          "name" -> "Greyhaven",
          "settlementType" -> exampleMap(
            "name" -> "Town",
            "diceCount" -> new java.lang.Integer(8),
            "dieSides" -> new java.lang.Integer(10)
          ),
          "alignment" -> "Neutral",
          "districts" -> exampleList(),
          "seatOfGovernment" -> new java.lang.Integer(1),
          "layout" -> exampleMap(
            "width" -> new java.lang.Integer(1024),
            "height" -> new java.lang.Integer(768),
            "gridSize" -> new java.lang.Integer(16),
            "outline" -> exampleList(),
            "seed" -> new java.lang.Long(123456789L)
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/settlements/random.pdf",
      "Generate a random settlement PDF",
      "Settlements",
      "Settlement PDF",
      binarySchema(),
      contentType = "application/pdf"
    )
  }

  private def addDungeonEndpoints(openAPI: OpenAPI): Unit = {
    addGetPath(
      openAPI,
      "/dungeons/random",
      "Generate a random dungeon",
      "Dungeons",
      "Dungeon JSON",
      refSchema("Dungeon"),
      responseExample = Some(
        exampleMap(
          "name" -> "Crypt of Ash",
          "siteType" -> "Tomb",
          "size" -> exampleMap(
            "name" -> "Small",
            "diceCount" -> new java.lang.Integer(5),
            "dieSides" -> new java.lang.Integer(10)
          ),
          "dangerLevel" -> "Risky",
          "rooms" -> exampleList(),
          "corridors" -> exampleList(),
          "layout" -> exampleMap(
            "width" -> new java.lang.Integer(800),
            "height" -> new java.lang.Integer(600),
            "gridSize" -> new java.lang.Integer(20),
            "outline" -> exampleList(),
            "entrances" -> exampleList(),
            "seed" -> new java.lang.Long(987654321L)
          )
        )
      )
    )
    addGetPath(
      openAPI,
      "/dungeons/random.pdf",
      "Generate a random dungeon PDF",
      "Dungeons",
      "Dungeon PDF",
      binarySchema(),
      contentType = "application/pdf"
    )
  }

  private def addHexEndpoints(openAPI: OpenAPI): Unit = {
    addGetPath(
      openAPI,
      "/hexes/random",
      "Generate a random hex map",
      "Hexes",
      "Hex map JSON",
      refSchema("HexMap"),
      responseExample = Some(
        exampleMap(
          "name" -> "Western Reach",
          "climate" -> "Temperate",
          "dangerLevel" -> "Unsafe",
          "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
          "hexes" -> exampleList(
            exampleMap(
              "id" -> new java.lang.Integer(1),
              "column" -> new java.lang.Integer(0),
              "row" -> new java.lang.Integer(0),
              "terrain" -> "Forest",
              "terrainStep" -> new java.lang.Integer(1)
            )
          ),
          "activeColumn" -> new java.lang.Integer(0),
          "activeRow" -> new java.lang.Integer(0)
        )
      )
    )
    addPostPath(
      openAPI,
      "/hexes/next",
      "Move the active hex and expand the map if needed",
      "Hexes",
      requestSchemaRef = "HexNextRequest",
      responseSchemaRef = "HexMap",
      responseDescription = "Updated hex map",
      errorSchemaRef = "Error",
      requestExample = Some(
        exampleMap(
          "map" -> exampleMap(
            "name" -> "Western Reach",
            "climate" -> "Temperate",
            "dangerLevel" -> "Unsafe",
            "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
            "hexes" -> exampleList(),
            "activeColumn" -> new java.lang.Integer(0),
            "activeRow" -> new java.lang.Integer(0)
          ),
          "direction" -> "NE"
        )
      ),
      responseExample = Some(
        exampleMap(
          "name" -> "Western Reach",
          "climate" -> "Temperate",
          "dangerLevel" -> "Unsafe",
          "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
          "hexes" -> exampleList(),
          "activeColumn" -> new java.lang.Integer(1),
          "activeRow" -> new java.lang.Integer(0)
        )
      )
    )
    addPostPath(
      openAPI,
      "/hexes/render",
      "Render a hex map as PDF or PNG, or echo JSON",
      "Hexes",
      requestSchemaRef = "HexRenderRequest",
      responseSchemaRef = "HexMap",
      responseDescription = "Rendered output",
      responseContentTypes = List("application/pdf", "image/png", "application/json"),
      errorSchemaRef = "Error",
      requestExample = Some(
        exampleMap(
          "map" -> exampleMap(
            "name" -> "Western Reach",
            "climate" -> "Temperate",
            "dangerLevel" -> "Unsafe",
            "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
            "hexes" -> exampleList(),
            "activeColumn" -> new java.lang.Integer(0),
            "activeRow" -> new java.lang.Integer(0)
          ),
          "type" -> "pdf"
        )
      ),
      responseExample = Some(
        exampleMap(
          "name" -> "Western Reach",
          "climate" -> "Temperate",
          "dangerLevel" -> "Unsafe",
          "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
          "hexes" -> exampleList(),
          "activeColumn" -> new java.lang.Integer(0),
          "activeRow" -> new java.lang.Integer(0)
        )
      )
    )
  }

  private def addSchemaDefinitions(openAPI: OpenAPI): Unit = {
    val components = new Components()

    components.addSchemas(
      "GenericObject",
      new SwaggerSchema().`type`("object").additionalProperties(true)
    )
    components.addSchemas(
      "Error",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("error")
        .addProperty("error", new SwaggerSchema().`type`("string"))
        .addProperty("details", new SwaggerSchema().`type`("string"))
        .addProperty(
          "allowed",
          new SwaggerSchema().`type`("array").items(new SwaggerSchema().`type`("string"))
        )
    )
    components.addSchemas(
      "NameCreate",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("race")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("race", new SwaggerSchema().`type`("string"))
        .addProperty("gender", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("firstName", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("lastName", new SwaggerSchema().`type`("boolean").nullable(true))
        .example(
          exampleMap(
            "name" -> "Aria",
            "race" -> "Human",
            "gender" -> "Female",
            "firstName" -> java.lang.Boolean.TRUE,
            "lastName" -> java.lang.Boolean.FALSE
          )
        )
    )
    components.addSchemas(
      "Name",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("race")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("race", new SwaggerSchema().`type`("string"))
        .addProperty("gender", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("firstName", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("lastName", new SwaggerSchema().`type`("boolean").nullable(true))
    )
    components.addSchemas(
      "PersonalityCreate",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("alignment")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
        .example(
          exampleMap(
            "name" -> "Stoic",
            "alignment" -> "Neutral"
          )
        )
    )
    components.addSchemas(
      "Personality",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("alignment")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "RaceAbility",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("description")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("description", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "Race",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("race")
        .addRequiredItem("ability")
        .addRequiredItem("languages")
        .addRequiredItem("chance")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("race", new SwaggerSchema().`type`("string"))
        .addProperty("ability", refSchema("RaceAbility"))
        .addProperty("languages", stringArraySchema())
        .addProperty("chance", new SwaggerSchema().`type`("number"))
        .example(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439011",
            "race" -> "Human",
            "ability" -> exampleMap("name" -> "Adaptable", "description" -> "Gain one extra talent."),
            "languages" -> exampleList("Common"),
            "chance" -> new java.lang.Double(0.5)
          )
        )
    )
    components.addSchemas(
      "BackgroundRange",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("min")
        .addRequiredItem("max")
        .addProperty("min", new SwaggerSchema().`type`("integer"))
        .addProperty("max", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "Background",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("range")
        .addRequiredItem("possessions")
        .addRequiredItem("details")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("range", refSchema("BackgroundRange"))
        .addProperty("possessions", new SwaggerSchema().`type`("string"))
        .addProperty("details", new SwaggerSchema().`type`("string"))
        .addProperty("poiKinds", stringArraySchema().nullable(true))
        .addProperty("poiNames", stringArraySchema().nullable(true))
    )
    components.addSchemas(
      "LanguageBenefit",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("choices")
        .addRequiredItem("choose")
        .addRequiredItem("extraCommon")
        .addRequiredItem("extraRare")
        .addProperty("choices", stringArraySchema())
        .addProperty("choose", new SwaggerSchema().`type`("integer"))
        .addProperty("extraCommon", new SwaggerSchema().`type`("integer"))
        .addProperty("extraRare", new SwaggerSchema().`type`("integer"))
        .addProperty("notes", new SwaggerSchema().`type`("string").nullable(true))
    )
    components.addSchemas(
      "ClassFeature",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("description")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("description", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "SpellTiers",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("tier1")
        .addRequiredItem("tier2")
        .addRequiredItem("tier3")
        .addRequiredItem("tier4")
        .addRequiredItem("tier5")
        .addProperty("tier1", new SwaggerSchema().`type`("integer"))
        .addProperty("tier2", new SwaggerSchema().`type`("integer"))
        .addProperty("tier3", new SwaggerSchema().`type`("integer"))
        .addProperty("tier4", new SwaggerSchema().`type`("integer"))
        .addProperty("tier5", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "SpellProgression",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("level")
        .addRequiredItem("tiers")
        .addProperty("level", new SwaggerSchema().`type`("integer"))
        .addProperty("tiers", refSchema("SpellTiers"))
    )
    components.addSchemas(
      "Spellcasting",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("spellList")
        .addRequiredItem("initialKnown")
        .addRequiredItem("progression")
        .addRequiredItem("notes")
        .addProperty("spellList", new SwaggerSchema().`type`("string"))
        .addProperty(
          "initialKnown",
          new SwaggerSchema()
            .`type`("object")
            .additionalProperties(new SwaggerSchema().`type`("integer"))
        )
        .addProperty("progression", arrayRefSchema("SpellProgression"))
        .addProperty("notes", stringArraySchema())
    )
    components.addSchemas(
      "DiceRange",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("min")
        .addRequiredItem("max")
        .addProperty("min", new SwaggerSchema().`type`("integer"))
        .addProperty("max", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "Talent",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("range")
        .addRequiredItem("effect")
        .addProperty("range", refSchema("DiceRange"))
        .addProperty("effect", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "LevelRange",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("min")
        .addRequiredItem("max")
        .addProperty("min", new SwaggerSchema().`type`("integer"))
        .addProperty("max", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "ClassTitle",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("levels")
        .addRequiredItem("lawful")
        .addRequiredItem("chaotic")
        .addRequiredItem("neutral")
        .addProperty("levels", refSchema("LevelRange"))
        .addProperty("lawful", new SwaggerSchema().`type`("string"))
        .addProperty("chaotic", new SwaggerSchema().`type`("string"))
        .addProperty("neutral", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "CharacterClass",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("weapons")
        .addRequiredItem("armor")
        .addRequiredItem("hitPointsPerLevel")
        .addRequiredItem("abilityPriority")
        .addRequiredItem("features")
        .addRequiredItem("talents")
        .addRequiredItem("titles")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("weapons", stringArraySchema())
        .addProperty("armor", stringArraySchema())
        .addProperty("hitPointsPerLevel", new SwaggerSchema().`type`("string"))
        .addProperty("languages", refSchema("LanguageBenefit").nullable(true))
        .addProperty("abilityPriority", stringArraySchema())
        .addProperty("features", arrayRefSchema("ClassFeature"))
        .addProperty("spellcasting", refSchema("Spellcasting").nullable(true))
        .addProperty("talents", arrayRefSchema("Talent"))
        .addProperty("titles", arrayRefSchema("ClassTitle"))
    )
    components.addSchemas(
      "Title",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("characterClass")
        .addRequiredItem("alignment")
        .addRequiredItem("minLevel")
        .addRequiredItem("maxLevel")
        .addRequiredItem("title")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("characterClass", new SwaggerSchema().`type`("string"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
        .addProperty("minLevel", new SwaggerSchema().`type`("integer"))
        .addProperty("maxLevel", new SwaggerSchema().`type`("integer"))
        .addProperty("title", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "Deity",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("alignment")
        .addRequiredItem("description")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
        .addProperty("description", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "LanguageEntry",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("speakers")
        .addRequiredItem("rarity")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("speakers", new SwaggerSchema().`type`("string"))
        .addProperty("rarity", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "MonsterSpecial",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("text")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("text", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "MonsterStats",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("ac")
        .addRequiredItem("hp")
        .addRequiredItem("mv")
        .addRequiredItem("str")
        .addRequiredItem("dex")
        .addRequiredItem("con")
        .addRequiredItem("int")
        .addRequiredItem("wis")
        .addRequiredItem("cha")
        .addProperty("ac", new SwaggerSchema().`type`("string"))
        .addProperty("hp", new SwaggerSchema().`type`("string"))
        .addProperty("mv", new SwaggerSchema().`type`("string"))
        .addProperty("str", new SwaggerSchema().`type`("string"))
        .addProperty("dex", new SwaggerSchema().`type`("string"))
        .addProperty("con", new SwaggerSchema().`type`("string"))
        .addProperty("int", new SwaggerSchema().`type`("string"))
        .addProperty("wis", new SwaggerSchema().`type`("string"))
        .addProperty("cha", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "Monster",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("flavourText")
        .addRequiredItem("attacks")
        .addRequiredItem("stats")
        .addRequiredItem("alignment")
        .addRequiredItem("level")
        .addRequiredItem("specials")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("flavourText", new SwaggerSchema().`type`("string"))
        .addProperty("attacks", new SwaggerSchema().`type`("string"))
        .addProperty("stats", refSchema("MonsterStats"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
        .addProperty("level", new SwaggerSchema().`type`("string"))
        .addProperty("specials", arrayRefSchema("MonsterSpecial"))
    )
    components.addSchemas(
      "Spell",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("tier")
        .addRequiredItem("castingAttribute")
        .addRequiredItem("description")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("tier", new SwaggerSchema().`type`("integer"))
        .addProperty("castingAttribute", stringArraySchema())
        .addProperty("prohibitedAlignments", stringArraySchema().nullable(true))
        .addProperty("spellType", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("range", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("duration", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("dc", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("description", new SwaggerSchema().`type`("string"))
        .addProperty("damage", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("damageType", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("healing", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("levelScaling", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("multiplier", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("opposed", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("opposedDc", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("opposedAbility", new SwaggerSchema().`type`("string").nullable(true))
        .example(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439012",
            "name" -> "Light",
            "tier" -> new java.lang.Integer(1),
            "castingAttribute" -> exampleList("Wisdom"),
            "description" -> "A small object shines with torchlight."
          )
        )
    )
    components.addSchemas(
      "Item",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("slots")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("itemType", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("description", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("cost", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("nonidentified", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("slots", new SwaggerSchema().`type`("integer"))
        .addProperty("magical", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("ac", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("count", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("nodex", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("attackType", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("damage", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("damageType", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("finesse", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("itemAttackBonus", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("defenseBonus", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("loading", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("loadingFullRound", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("range", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("thrown", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("twoHanded", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("versatile", new SwaggerSchema().`type`("boolean").nullable(true))
        .addProperty("versatileDamage", new SwaggerSchema().`type`("string").nullable(true))
        .example(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439013",
            "name" -> "Shortsword",
            "itemType" -> "Weapon",
            "slots" -> new java.lang.Integer(1),
            "damage" -> "1d6",
            "damageType" -> "Piercing"
          )
        )
    )
    components.addSchemas(
      "SettlementName",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("settlementType")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("settlementType", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "AbilityScore",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("score")
        .addRequiredItem("modifier")
        .addProperty("score", new SwaggerSchema().`type`("integer"))
        .addProperty("modifier", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "AbilityScores",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("strength")
        .addRequiredItem("dexterity")
        .addRequiredItem("constitution")
        .addRequiredItem("intelligence")
        .addRequiredItem("wisdom")
        .addRequiredItem("charisma")
        .addProperty("strength", refSchema("AbilityScore"))
        .addProperty("dexterity", refSchema("AbilityScore"))
        .addProperty("constitution", refSchema("AbilityScore"))
        .addProperty("intelligence", refSchema("AbilityScore"))
        .addProperty("wisdom", refSchema("AbilityScore"))
        .addProperty("charisma", refSchema("AbilityScore"))
    )
    components.addSchemas(
      "Character",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("_id")
        .addRequiredItem("name")
        .addRequiredItem("xpForNextLevel")
        .addRequiredItem("abilities")
        .addRequiredItem("hitPoints")
        .addRequiredItem("armorClass")
        .addRequiredItem("features")
        .addRequiredItem("talents")
        .addRequiredItem("spells")
        .addRequiredItem("attacks")
        .addRequiredItem("gear")
        .addRequiredItem("languages")
        .addRequiredItem("personalities")
        .addRequiredItem("goldPieces")
        .addRequiredItem("silverPieces")
        .addRequiredItem("copperPieces")
        .addRequiredItem("freeToCarry")
        .addProperty("_id", new SwaggerSchema().`type`("string"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("ancestry", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("characterClass", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("level", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("xp", new SwaggerSchema().`type`("integer").nullable(true))
        .addProperty("xpForNextLevel", new SwaggerSchema().`type`("integer"))
        .addProperty("title", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("alignment", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("background", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("deity", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("abilities", refSchema("AbilityScores"))
        .addProperty("hitPoints", new SwaggerSchema().`type`("integer"))
        .addProperty("armorClass", new SwaggerSchema().`type`("integer"))
        .addProperty("features", arrayRefSchema("ClassFeature"))
        .addProperty("talents", stringArraySchema())
        .addProperty("spells", stringArraySchema())
        .addProperty("attacks", stringArraySchema())
        .addProperty("gear", stringArraySchema())
        .addProperty("languages", stringArraySchema())
        .addProperty("personalities", stringArraySchema())
        .addProperty("gender", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("goldPieces", new SwaggerSchema().`type`("integer"))
        .addProperty("silverPieces", new SwaggerSchema().`type`("integer"))
        .addProperty("copperPieces", new SwaggerSchema().`type`("integer"))
        .addProperty("freeToCarry", stringArraySchema())
        .example(
          exampleMap(
            "_id" -> "507f1f77bcf86cd799439014",
            "name" -> "Bran",
            "ancestry" -> "Human",
            "characterClass" -> "Fighter",
            "level" -> new java.lang.Integer(1),
            "xp" -> new java.lang.Integer(0),
            "xpForNextLevel" -> new java.lang.Integer(10),
            "abilities" -> exampleMap(
              "strength" -> exampleMap("score" -> new java.lang.Integer(14), "modifier" -> new java.lang.Integer(2)),
              "dexterity" -> exampleMap("score" -> new java.lang.Integer(10), "modifier" -> new java.lang.Integer(0)),
              "constitution" -> exampleMap(
                "score" -> new java.lang.Integer(12),
                "modifier" -> new java.lang.Integer(1)
              ),
              "intelligence" -> exampleMap(
                "score" -> new java.lang.Integer(9),
                "modifier" -> new java.lang.Integer(-1)
              ),
              "wisdom" -> exampleMap("score" -> new java.lang.Integer(10), "modifier" -> new java.lang.Integer(0)),
              "charisma" -> exampleMap("score" -> new java.lang.Integer(8), "modifier" -> new java.lang.Integer(-1))
            ),
            "hitPoints" -> new java.lang.Integer(8),
            "armorClass" -> new java.lang.Integer(14),
            "features" -> exampleList(exampleMap("name" -> "Fighting Style", "description" -> "Gain +1 attack.")),
            "talents" -> exampleList("Brutal Strike"),
            "spells" -> exampleList(),
            "attacks" -> exampleList("Shortsword +2 (1d6)"),
            "gear" -> exampleList("Slot 1: Shortsword", "Slot 2: Shield"),
            "languages" -> exampleList("Common"),
            "personalities" -> exampleList("Stoic"),
            "goldPieces" -> new java.lang.Integer(12),
            "silverPieces" -> new java.lang.Integer(0),
            "copperPieces" -> new java.lang.Integer(0),
            "freeToCarry" -> exampleList("Torch", "Rations")
          )
        )
    )
    components.addSchemas(
      "Point",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("x")
        .addRequiredItem("y")
        .addProperty("x", new SwaggerSchema().`type`("integer"))
        .addProperty("y", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "SettlementType",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("diceCount")
        .addRequiredItem("dieSides")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("diceCount", new SwaggerSchema().`type`("integer"))
        .addProperty("dieSides", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "SettlementLayout",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("width")
        .addRequiredItem("height")
        .addRequiredItem("gridSize")
        .addRequiredItem("outline")
        .addRequiredItem("seed")
        .addProperty("width", new SwaggerSchema().`type`("integer"))
        .addProperty("height", new SwaggerSchema().`type`("integer"))
        .addProperty("gridSize", new SwaggerSchema().`type`("integer"))
        .addProperty("outline", arrayRefSchema("Point"))
        .addProperty("seed", new SwaggerSchema().`type`("integer").format("int64"))
    )
    components.addSchemas(
      "Drink",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("cost")
        .addRequiredItem("effect")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("cost", new SwaggerSchema().`type`("string"))
        .addProperty("effect", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "Food",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("cost")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("cost", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "Tavern",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("knownFor")
        .addRequiredItem("drinks")
        .addRequiredItem("food")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("knownFor", new SwaggerSchema().`type`("string"))
        .addProperty("drinks", arrayRefSchema("Drink"))
        .addProperty("food", arrayRefSchema("Food"))
    )
    components.addSchemas(
      "Npc",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("ancestry")
        .addRequiredItem("age")
        .addRequiredItem("alignment")
        .addRequiredItem("wealth")
        .addRequiredItem("appearance")
        .addRequiredItem("mannerism")
        .addRequiredItem("secret")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("ancestry", new SwaggerSchema().`type`("string"))
        .addProperty("age", new SwaggerSchema().`type`("string"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
        .addProperty("wealth", new SwaggerSchema().`type`("string"))
        .addProperty("appearance", new SwaggerSchema().`type`("string"))
        .addProperty("mannerism", new SwaggerSchema().`type`("string"))
        .addProperty("secret", new SwaggerSchema().`type`("string"))
        .addProperty("background", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("personality", new SwaggerSchema().`type`("string").nullable(true))
    )
    components.addSchemas(
      "Shop",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("shopType")
        .addRequiredItem("knownFor")
        .addRequiredItem("interestingCustomer")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("shopType", new SwaggerSchema().`type`("string"))
        .addProperty("knownFor", new SwaggerSchema().`type`("string"))
        .addProperty("interestingCustomer", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "PointOfInterest",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("id")
        .addRequiredItem("name")
        .addRequiredItem("kind")
        .addRequiredItem("location")
        .addProperty("id", new SwaggerSchema().`type`("integer"))
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("kind", new SwaggerSchema().`type`("string"))
        .addProperty("location", refSchema("Point"))
        .addProperty("tavern", refSchema("Tavern").nullable(true))
        .addProperty("shop", refSchema("Shop").nullable(true))
        .addProperty("npc", refSchema("Npc").nullable(true))
        .addProperty("buildingId", new SwaggerSchema().`type`("integer").nullable(true))
    )
    components.addSchemas(
      "Building",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("id")
        .addRequiredItem("footprint")
        .addRequiredItem("usage")
        .addProperty("id", new SwaggerSchema().`type`("integer"))
        .addProperty("footprint", arrayRefSchema("Point"))
        .addProperty("usage", new SwaggerSchema().`type`("string"))
        .addProperty("poiId", new SwaggerSchema().`type`("integer").nullable(true))
    )
    components.addSchemas(
      "Plaza",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("center")
        .addRequiredItem("radius")
        .addProperty("center", refSchema("Point"))
        .addProperty("radius", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "District",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("id")
        .addRequiredItem("roll")
        .addRequiredItem("districtType")
        .addRequiredItem("alignment")
        .addRequiredItem("seatOfGovernment")
        .addRequiredItem("position")
        .addRequiredItem("boundary")
        .addRequiredItem("plazas")
        .addRequiredItem("pointsOfInterest")
        .addRequiredItem("buildings")
        .addProperty("id", new SwaggerSchema().`type`("integer"))
        .addProperty("roll", new SwaggerSchema().`type`("integer"))
        .addProperty("districtType", new SwaggerSchema().`type`("string"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
        .addProperty("seatOfGovernment", new SwaggerSchema().`type`("boolean"))
        .addProperty("position", refSchema("Point"))
        .addProperty("boundary", arrayRefSchema("Point"))
        .addProperty("plazas", arrayRefSchema("Plaza"))
        .addProperty("pointsOfInterest", arrayRefSchema("PointOfInterest"))
        .addProperty("buildings", arrayRefSchema("Building"))
    )
    components.addSchemas(
      "Settlement",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("settlementType")
        .addRequiredItem("alignment")
        .addRequiredItem("districts")
        .addRequiredItem("seatOfGovernment")
        .addRequiredItem("layout")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("settlementType", refSchema("SettlementType"))
        .addProperty("alignment", new SwaggerSchema().`type`("string"))
        .addProperty("districts", arrayRefSchema("District"))
        .addProperty("seatOfGovernment", new SwaggerSchema().`type`("integer"))
        .addProperty("layout", refSchema("SettlementLayout"))
        .example(
          exampleMap(
            "name" -> "Greyhaven",
            "settlementType" -> exampleMap(
              "name" -> "Town",
              "diceCount" -> new java.lang.Integer(8),
              "dieSides" -> new java.lang.Integer(10)
            ),
            "alignment" -> "Neutral",
            "districts" -> exampleList(),
            "seatOfGovernment" -> new java.lang.Integer(1),
            "layout" -> exampleMap(
              "width" -> new java.lang.Integer(1024),
              "height" -> new java.lang.Integer(768),
              "gridSize" -> new java.lang.Integer(16),
              "outline" -> exampleList(),
              "seed" -> new java.lang.Long(123456789L)
            )
          )
        )
    )
    components.addSchemas(
      "DungeonSize",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("diceCount")
        .addRequiredItem("dieSides")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("diceCount", new SwaggerSchema().`type`("integer"))
        .addProperty("dieSides", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "DungeonRoom",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("id")
        .addRequiredItem("roll")
        .addRequiredItem("roomType")
        .addRequiredItem("details")
        .addRequiredItem("position")
        .addRequiredItem("width")
        .addRequiredItem("height")
        .addRequiredItem("objectiveRoom")
        .addProperty("id", new SwaggerSchema().`type`("integer"))
        .addProperty("roll", new SwaggerSchema().`type`("integer"))
        .addProperty("roomType", new SwaggerSchema().`type`("string"))
        .addProperty("details", stringArraySchema())
        .addProperty("position", refSchema("Point"))
        .addProperty("width", new SwaggerSchema().`type`("integer"))
        .addProperty("height", new SwaggerSchema().`type`("integer"))
        .addProperty("objectiveRoom", new SwaggerSchema().`type`("boolean"))
    )
    components.addSchemas(
      "DungeonCorridor",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("start")
        .addRequiredItem("end")
        .addProperty("start", refSchema("Point"))
        .addProperty("end", refSchema("Point"))
    )
    components.addSchemas(
      "DungeonLayout",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("width")
        .addRequiredItem("height")
        .addRequiredItem("gridSize")
        .addRequiredItem("outline")
        .addRequiredItem("entrances")
        .addRequiredItem("seed")
        .addProperty("width", new SwaggerSchema().`type`("integer"))
        .addProperty("height", new SwaggerSchema().`type`("integer"))
        .addProperty("gridSize", new SwaggerSchema().`type`("integer"))
        .addProperty("outline", arrayRefSchema("Point"))
        .addProperty("entrances", arrayRefSchema("Point"))
        .addProperty("seed", new SwaggerSchema().`type`("integer").format("int64"))
    )
    components.addSchemas(
      "Dungeon",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("siteType")
        .addRequiredItem("size")
        .addRequiredItem("dangerLevel")
        .addRequiredItem("rooms")
        .addRequiredItem("corridors")
        .addRequiredItem("layout")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("siteType", new SwaggerSchema().`type`("string"))
        .addProperty("size", refSchema("DungeonSize"))
        .addProperty("dangerLevel", new SwaggerSchema().`type`("string"))
        .addProperty("rooms", arrayRefSchema("DungeonRoom"))
        .addProperty("corridors", arrayRefSchema("DungeonCorridor"))
        .addProperty("layout", refSchema("DungeonLayout"))
        .example(
          exampleMap(
            "name" -> "Crypt of Ash",
            "siteType" -> "Tomb",
            "size" -> exampleMap(
              "name" -> "Small",
              "diceCount" -> new java.lang.Integer(5),
              "dieSides" -> new java.lang.Integer(10)
            ),
            "dangerLevel" -> "Risky",
            "rooms" -> exampleList(),
            "corridors" -> exampleList(),
            "layout" -> exampleMap(
              "width" -> new java.lang.Integer(800),
              "height" -> new java.lang.Integer(600),
              "gridSize" -> new java.lang.Integer(20),
              "outline" -> exampleList(),
              "entrances" -> exampleList(),
              "seed" -> new java.lang.Long(987654321L)
            )
          )
        )
    )
    components.addSchemas(
      "HexMapLayout",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("columns")
        .addRequiredItem("rows")
        .addProperty("columns", new SwaggerSchema().`type`("integer"))
        .addProperty("rows", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "HexPointOfInterest",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("id")
        .addRequiredItem("location")
        .addRequiredItem("development")
        .addRequiredItem("offsetX")
        .addRequiredItem("offsetY")
        .addProperty("id", new SwaggerSchema().`type`("integer"))
        .addProperty("location", new SwaggerSchema().`type`("string"))
        .addProperty("development", new SwaggerSchema().`type`("string"))
        .addProperty("cataclysm", new SwaggerSchema().`type`("string").nullable(true))
        .addProperty("offsetX", new SwaggerSchema().`type`("number"))
        .addProperty("offsetY", new SwaggerSchema().`type`("number"))
    )
    components.addSchemas(
      "HexOverlay",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("kind")
        .addRequiredItem("orientation")
        .addRequiredItem("baseTerrain")
        .addProperty("kind", new SwaggerSchema().`type`("string"))
        .addProperty("orientation", new SwaggerSchema().`type`("string"))
        .addProperty("baseTerrain", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "HexCell",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("id")
        .addRequiredItem("column")
        .addRequiredItem("row")
        .addRequiredItem("terrain")
        .addRequiredItem("terrainStep")
        .addProperty("id", new SwaggerSchema().`type`("integer"))
        .addProperty("column", new SwaggerSchema().`type`("integer"))
        .addProperty("row", new SwaggerSchema().`type`("integer"))
        .addProperty("terrain", new SwaggerSchema().`type`("string"))
        .addProperty("terrainStep", new SwaggerSchema().`type`("integer"))
        .addProperty("pointOfInterest", refSchema("HexPointOfInterest").nullable(true))
        .addProperty("overlay", refSchema("HexOverlay").nullable(true))
    )
    components.addSchemas(
      "HexMap",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("name")
        .addRequiredItem("climate")
        .addRequiredItem("dangerLevel")
        .addRequiredItem("layout")
        .addRequiredItem("hexes")
        .addRequiredItem("activeColumn")
        .addRequiredItem("activeRow")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("climate", new SwaggerSchema().`type`("string"))
        .addProperty("dangerLevel", new SwaggerSchema().`type`("string"))
        .addProperty("layout", refSchema("HexMapLayout"))
        .addProperty("hexes", arrayRefSchema("HexCell"))
        .addProperty("activeColumn", new SwaggerSchema().`type`("integer"))
        .addProperty("activeRow", new SwaggerSchema().`type`("integer"))
        .example(
          exampleMap(
            "name" -> "Western Reach",
            "climate" -> "Temperate",
            "dangerLevel" -> "Unsafe",
            "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
            "hexes" -> exampleList(
              exampleMap(
                "id" -> new java.lang.Integer(1),
                "column" -> new java.lang.Integer(0),
                "row" -> new java.lang.Integer(0),
                "terrain" -> "Forest",
                "terrainStep" -> new java.lang.Integer(1)
              )
            ),
            "activeColumn" -> new java.lang.Integer(0),
            "activeRow" -> new java.lang.Integer(0)
          )
        )
    )
    components.addSchemas(
      "HexNextRequest",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("map")
        .addRequiredItem("direction")
        .addProperty("map", refSchema("HexMap"))
        .addProperty("direction", new SwaggerSchema().`type`("string").description("One of NW, NE, E, SE, SW, W"))
        .example(
          exampleMap(
            "map" -> exampleMap(
              "name" -> "Western Reach",
              "climate" -> "Temperate",
              "dangerLevel" -> "Unsafe",
              "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
              "hexes" -> exampleList(),
              "activeColumn" -> new java.lang.Integer(0),
              "activeRow" -> new java.lang.Integer(0)
            ),
            "direction" -> "NE"
          )
        )
    )
    components.addSchemas(
      "HexRenderRequest",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("map")
        .addRequiredItem("type")
        .addProperty("map", refSchema("HexMap"))
        .addProperty("type", new SwaggerSchema().`type`("string").description("One of pdf, png, json"))
        .example(
          exampleMap(
            "map" -> exampleMap(
              "name" -> "Western Reach",
              "climate" -> "Temperate",
              "dangerLevel" -> "Unsafe",
              "layout" -> exampleMap("columns" -> new java.lang.Integer(3), "rows" -> new java.lang.Integer(3)),
              "hexes" -> exampleList(),
              "activeColumn" -> new java.lang.Integer(0),
              "activeRow" -> new java.lang.Integer(0)
            ),
            "type" -> "pdf"
          )
        )
    )

    val _ = openAPI.components(components)
  }

  private def addGetPath(
      openAPI: OpenAPI,
      path: String,
      summary: String,
      tag: String,
      responseDescription: String,
      responseSchema: SwaggerSchema[_],
      contentType: String = "application/json",
      queryParameters: List[Parameter] = Nil,
      responseExample: Option[AnyRef] = None
  ): Unit = {
    val operation = new Operation()
      .summary(summary)
      .addTagsItem(tag)

    if (queryParameters.nonEmpty) {
      queryParameters.foreach(operation.addParametersItem)
    }

    val mediaType = new MediaType().schema(responseSchema)
    responseExample.foreach(mediaType.example)

    val responses = new ApiResponses()
    responses.addApiResponse(
      "200",
      new ApiResponse()
        .description(responseDescription)
        .content(
          new Content().addMediaType(
            contentType,
            mediaType
          )
        )
    )
    operation.responses(responses)

    val pathItem = new PathItem().get(operation)
    val _ = openAPI.path(path, pathItem)
  }

  private def addPostPath(
      openAPI: OpenAPI,
      path: String,
      summary: String,
      tag: String,
      requestSchemaRef: String,
      responseSchemaRef: String,
      responseCode: String = "200",
      responseDescription: String = "OK",
      responseContentTypes: List[String] = List("application/json"),
      errorSchemaRef: String = "Error",
      requestExample: Option[AnyRef] = None,
      responseExample: Option[AnyRef] = None
  ): Unit = {
    val operation = new Operation()
      .summary(summary)
      .addTagsItem(tag)
      .requestBody(createJsonRequestBody(requestSchemaRef, summary, requestExample))

    val responses = new ApiResponses()
    val content = new Content()
    responseContentTypes.foreach { contentType =>
      val schema =
        if (contentType == "application/json") refSchema(responseSchemaRef)
        else binarySchema()
      val mediaType = new MediaType().schema(schema)
      if (contentType == "application/json") {
        responseExample.foreach(mediaType.example)
      }
      content.addMediaType(contentType, mediaType)
    }
    responses.addApiResponse(
      responseCode,
      new ApiResponse()
        .description(responseDescription)
        .content(content)
    )
    responses.addApiResponse(
      "400",
      new ApiResponse()
        .description("Invalid request")
        .content(
          new Content().addMediaType(
            "application/json",
            new MediaType().schema(refSchema(errorSchemaRef))
          )
        )
    )

    operation.responses(responses)
    val pathItem = new PathItem().post(operation)
    val _ = openAPI.path(path, pathItem)
  }

  private def createJsonRequestBody(
      schemaRef: String,
      description: String,
      example: Option[AnyRef]
  ): RequestBody = {
    val mediaType = new MediaType().schema(refSchema(schemaRef))
    example.foreach(mediaType.example)

    new RequestBody()
      .description(description)
      .required(true)
      .content(
        new Content().addMediaType(
          "application/json",
          mediaType
        )
      )
  }

  private def queryParam(name: String, description: String, schemaType: String): Parameter =
    new Parameter()
      .in("query")
      .name(name)
      .required(false)
      .description(description)
      .schema(new SwaggerSchema().`type`(schemaType))

  private def refSchema(name: String): SwaggerSchema[_] =
    new SwaggerSchema[Object]().$ref(s"#/components/schemas/$name")

  private def arrayRefSchema(name: String): SwaggerSchema[_] =
    new SwaggerSchema[Object]().`type`("array").items(refSchema(name))

  private def stringArraySchema(): SwaggerSchema[_] =
    new SwaggerSchema[Object]().`type`("array").items(new SwaggerSchema().`type`("string"))

  private def binarySchema(): SwaggerSchema[_] =
    new SwaggerSchema[Object]().`type`("string").format("binary")

  private def textSchema(): SwaggerSchema[_] =
    new SwaggerSchema[Object]().`type`("string")

  private def exampleMap(entries: (String, AnyRef)*): java.util.Map[String, AnyRef] = {
    val map = new java.util.LinkedHashMap[String, AnyRef]()
    entries.foreach { case (key, value) => map.put(key, value) }
    map
  }

  private def exampleList(values: AnyRef*): java.util.List[AnyRef] = {
    val list = new java.util.ArrayList[AnyRef]()
    values.foreach(list.add)
    list
  }
}
