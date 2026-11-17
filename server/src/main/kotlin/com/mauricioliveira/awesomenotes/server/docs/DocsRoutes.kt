package com.mauricioliveira.awesomenotes.server.docs

import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Route

fun Route.docsRoutes() {
    staticResources(remotePath = "/openapi", basePackage = "openapi")
    swaggerUI(path = "docs", swaggerFile = "openapi/openapi.yaml")
}