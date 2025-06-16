package com.sakethh.linkora

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


/*
in RoutingNode's `buildPipeline()`, the following is implemented:

    for (index in 0..handlers.lastIndex) {
        pipeline.intercept(Call) {
            val call = call as RoutingPipelineCall
            val routingCall = RoutingCall(call)
            val routingContext = RoutingContext(routingCall)
            if (call.isHandled) return@intercept
            handlers[index].invoke(routingContext)
        }
    }

so if a call is handled here in this `Security.kt` file,
the handler of our routes (defined in routing files) is never reached;
which is exactly what we want.

I've gone through the Ktor auth library (just the surface), and it defines an extension function 
`Route.authenticate` (same name) in `AuthenticationInterceptors.kt` that does almost the same thing. 
It doesn't directly send a 403 like I'm doing here, but for our scenario, this is fine.
*/
val CustomBearerAuth = createRouteScopedPlugin(name = "CustomBearerAuth") {
    onCall { call ->
        val rawHeader = call.request.headers[HttpHeaders.Authorization]
        when {
            rawHeader == null -> {
                call.respond(status = HttpStatusCode.Unauthorized, message = "Missing Authorization header")
            }

            rawHeader.startsWith("Bearer ", ignoreCase = true).not() -> {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "Invalid Authorization header format. Expected 'Bearer <token>'."
                )
            }

            else -> {
                val authToken = rawHeader.substringAfter("Bearer").trim()
                when {
                    authToken.isBlank() -> {
                        println("[Auth] Empty token @${call.request.path()}")
                        call.respond(status = HttpStatusCode.Unauthorized, message = "Bearer token is missing.")
                    }

                    authToken != ServerConfiguration.readConfig().serverAuthToken.trim() -> {
                        println("[Auth] Invalid token: \"$authToken\" @${call.request.path()}")
                        call.respond(status = HttpStatusCode.Unauthorized, message = "Invalid token.")
                    }

                    authToken == ServerConfiguration.readConfig().serverAuthToken.trim() -> {
                        println("[Auth] Authenticated @${call.request.path()}")
                    }

                }
            }
        }
    }
}

class AuthenticationRouteSelector() : RouteSelector() {
    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }
}

fun Route.authenticate(initOnSuccess: Route.() -> Unit) {
    val authenticatedRoute = createChild(selector = AuthenticationRouteSelector())
    authenticatedRoute.install(CustomBearerAuth)
    authenticatedRoute.initOnSuccess()
}

/*

i should have just used `authHeader` and it probably would've fixed the special characters in token = 401 issue

but this does pretty much the same thing, and I learnt a couple of new things, so hell yeahh

--------------------------------------------


with the auth lib, something like this would have worked:

install(Authentication) {
        bearer(name = Security.BEARER.name) {

            // `getAuthHeader` uses `call.request.parseAuthorizationHeader()` by default,
            // we gotta change this
            // to make sure our parsing is the one that's used and not `parseAuthorizationHeader`,
            // which doesn't parse as expected if the token contains any special characters
            authHeader { call ->
                val rawAuthHeader = call.request.headers[HttpHeaders.Authorization] ?: return@authHeader null
                if (!rawAuthHeader.startsWith(prefix = "Bearer ", ignoreCase = true)) return@authHeader null

                HttpAuthHeader.Parameterized(
                    authScheme = "Bearer", parameters = listOf(
                        HeaderValueParam(
                            name = "authToken", value = rawAuthHeader.substringAfter("Bearer").trim()
                        )
                    )
                )
            }
* */