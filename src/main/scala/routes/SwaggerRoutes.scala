package routes

import zio._
import zio.http._

object SwaggerRoutes {

  val openapiRoute: Route[Any, Nothing] =
    Method.GET / "api" / "openapi.json" -> handler {
      ZIO.succeed(
        Response
          .text(OpenAPIGen.generateSpec())
          .addHeader(Header.ContentType(MediaType.application.json))
      )
    }

  val swaggerUIRoute: Route[Any, Nothing] =
    Method.GET / "api" / "docs" -> handler {
      ZIO.succeed(
        Response
          .text(swaggerUIHtml)
          .addHeader(Header.ContentType(MediaType.text.html))
      )
    }

  val routes: Routes[Any, Nothing] = Routes(openapiRoute, swaggerUIRoute)

  private val swaggerUIHtml: String =
    """<!DOCTYPE html>
      |<html lang="en">
      |<head>
      |    <meta charset="UTF-8">
      |    <title>Shadowdark REST API Documentation</title>
      |    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.18.2/swagger-ui.css" />
      |    <link rel="icon" type="image/png" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.18.2/favicon-32x32.png" sizes="32x32" />
      |    <style>
      |        html {
      |            box-sizing: border-box;
      |            overflow-y: scroll;
      |        }
      |        *, *:before, *:after {
      |            box-sizing: inherit;
      |        }
      |        body {
      |            margin: 0;
      |            padding: 0;
      |        }
      |    </style>
      |</head>
      |<body>
      |<div id="swagger-ui"></div>
      |<script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.18.2/swagger-ui-bundle.js" charset="UTF-8"></script>
      |<script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5.18.2/swagger-ui-standalone-preset.js" charset="UTF-8"></script>
      |<script>
      |    window.onload = function() {
      |        window.ui = SwaggerUIBundle({
      |            url: "/api/openapi.json",
      |            dom_id: '#swagger-ui',
      |            deepLinking: true,
      |            presets: [
      |                SwaggerUIBundle.presets.apis,
      |                SwaggerUIStandalonePreset
      |            ],
      |            plugins: [
      |                SwaggerUIBundle.plugins.DownloadUrl
      |            ],
      |            layout: "StandaloneLayout"
      |        });
      |    };
      |</script>
      |</body>
      |</html>
      |""".stripMargin
}
