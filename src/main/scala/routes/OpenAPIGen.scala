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
      contentType = "text/plain"
    )
    addGetPath(
      openAPI,
      "/health",
      summary = "Health check",
      tag = "System",
      responseDescription = "OK",
      responseSchema = textSchema(),
      contentType = "text/plain"
    )
  }

  private def addContentEndpoints(openAPI: OpenAPI): Unit = {
    addGetPath(openAPI, "/names", "List names", "Content", "Names list", arrayRefSchema("Name"))
    addPostPath(
      openAPI,
      "/names",
      "Create a name",
      "Content",
      requestSchemaRef = "NameCreate",
      responseSchemaRef = "Name",
      responseCode = "201",
      responseDescription = "Created name"
    )

    addGetPath(openAPI, "/races", "List races", "Content", "Race list", arrayRefSchema("GenericObject"))
    addGetPath(
      openAPI,
      "/personalities",
      "List personalities",
      "Content",
      "Personalities list",
      arrayRefSchema("GenericObject")
    )
    addPostPath(
      openAPI,
      "/personalities",
      "Create a personality",
      "Content",
      requestSchemaRef = "PersonalityCreate",
      responseSchemaRef = "GenericObject",
      responseCode = "201",
      responseDescription = "Created personality"
    )
    addGetPath(
      openAPI,
      "/backgrounds",
      "List backgrounds",
      "Content",
      "Background list",
      arrayRefSchema("GenericObject")
    )
    addGetPath(openAPI, "/classes", "List character classes", "Content", "Class list", arrayRefSchema("GenericObject"))
    addGetPath(openAPI, "/spells", "List spells", "Content", "Spell list", arrayRefSchema("GenericObject"))
    addGetPath(openAPI, "/items", "List items", "Content", "Item list", arrayRefSchema("GenericObject"))
    addGetPath(openAPI, "/titles", "List titles", "Content", "Title list", arrayRefSchema("GenericObject"))
    addGetPath(openAPI, "/deities", "List deities", "Content", "Deity list", arrayRefSchema("GenericObject"))
    addGetPath(openAPI, "/languages", "List languages", "Content", "Language list", arrayRefSchema("GenericObject"))
    addGetPath(openAPI, "/monsters", "List monsters", "Content", "Monster list", arrayRefSchema("GenericObject"))
    addGetPath(
      openAPI,
      "/settlement-names",
      "List settlement names",
      "Content",
      "Settlement name list",
      arrayRefSchema("GenericObject")
    )

    addGetPath(
      openAPI,
      "/random-character",
      "Generate a random character",
      "Characters",
      "Character JSON",
      refSchema("GenericObject"),
      queryParameters = List(
        queryParam("zeroLevel", "Use true or 1 to generate a 0-level character", "string")
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
      refSchema("GenericObject")
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
      refSchema("GenericObject")
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
      refSchema("HexMap")
    )
    addPostPath(
      openAPI,
      "/hexes/next",
      "Move the active hex and expand the map if needed",
      "Hexes",
      requestSchemaRef = "HexNextRequest",
      responseSchemaRef = "HexMap",
      responseDescription = "Updated hex map",
      errorSchemaRef = "Error"
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
      errorSchemaRef = "Error"
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
    )
    components.addSchemas(
      "Name",
      new SwaggerSchema()
        .`type`("object")
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
    )
    components.addSchemas(
      "HexMapLayout",
      new SwaggerSchema()
        .`type`("object")
        .addProperty("columns", new SwaggerSchema().`type`("integer"))
        .addProperty("rows", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "HexPointOfInterest",
      new SwaggerSchema()
        .`type`("object")
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
        .addProperty("kind", new SwaggerSchema().`type`("string"))
        .addProperty("orientation", new SwaggerSchema().`type`("string"))
        .addProperty("baseTerrain", new SwaggerSchema().`type`("string"))
    )
    components.addSchemas(
      "HexCell",
      new SwaggerSchema()
        .`type`("object")
        .addProperty("id", new SwaggerSchema().`type`("integer"))
        .addProperty("column", new SwaggerSchema().`type`("integer"))
        .addProperty("row", new SwaggerSchema().`type`("integer"))
        .addProperty("terrain", new SwaggerSchema().`type`("string"))
        .addProperty("terrainStep", new SwaggerSchema().`type`("integer"))
        .addProperty("pointOfInterest", refSchema("HexPointOfInterest"))
        .addProperty("overlay", refSchema("HexOverlay"))
    )
    components.addSchemas(
      "HexMap",
      new SwaggerSchema()
        .`type`("object")
        .addProperty("name", new SwaggerSchema().`type`("string"))
        .addProperty("climate", new SwaggerSchema().`type`("string"))
        .addProperty("dangerLevel", new SwaggerSchema().`type`("string"))
        .addProperty("layout", refSchema("HexMapLayout"))
        .addProperty("hexes", arrayRefSchema("HexCell"))
        .addProperty("activeColumn", new SwaggerSchema().`type`("integer"))
        .addProperty("activeRow", new SwaggerSchema().`type`("integer"))
    )
    components.addSchemas(
      "HexNextRequest",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("map")
        .addRequiredItem("direction")
        .addProperty("map", refSchema("HexMap"))
        .addProperty("direction", new SwaggerSchema().`type`("string").description("One of NW, NE, E, SE, SW, W"))
    )
    components.addSchemas(
      "HexRenderRequest",
      new SwaggerSchema()
        .`type`("object")
        .addRequiredItem("map")
        .addRequiredItem("type")
        .addProperty("map", refSchema("HexMap"))
        .addProperty("type", new SwaggerSchema().`type`("string").description("One of pdf, png, json"))
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
      queryParameters: List[Parameter] = Nil
  ): Unit = {
    val operation = new Operation()
      .summary(summary)
      .addTagsItem(tag)

    if (queryParameters.nonEmpty) {
      queryParameters.foreach(operation.addParametersItem)
    }

    val responses = new ApiResponses()
    responses.addApiResponse(
      "200",
      new ApiResponse()
        .description(responseDescription)
        .content(
          new Content().addMediaType(
            contentType,
            new MediaType().schema(responseSchema)
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
      errorSchemaRef: String = "Error"
  ): Unit = {
    val operation = new Operation()
      .summary(summary)
      .addTagsItem(tag)
      .requestBody(createJsonRequestBody(requestSchemaRef, summary))

    val responses = new ApiResponses()
    val content = new Content()
    responseContentTypes.foreach { contentType =>
      val schema =
        if (contentType == "application/json") refSchema(responseSchemaRef)
        else binarySchema()
      content.addMediaType(contentType, new MediaType().schema(schema))
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

  private def createJsonRequestBody(schemaRef: String, description: String): RequestBody =
    new RequestBody()
      .description(description)
      .required(true)
      .content(
        new Content().addMediaType(
          "application/json",
          new MediaType().schema(refSchema(schemaRef))
        )
      )

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

  private def binarySchema(): SwaggerSchema[_] =
    new SwaggerSchema[Object]().`type`("string").format("binary")

  private def textSchema(): SwaggerSchema[_] =
    new SwaggerSchema[Object]().`type`("string")
}
