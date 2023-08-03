package dev.andante.audience.resource.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import dev.andante.audience.resource.BuiltResourcePack
import java.io.IOException
import java.net.InetSocketAddress
import java.util.UUID

class ResourcePackServer private constructor(
    val httpServer: HttpServer,
    path: String,
    private val handler: ResourcePackRequestHandler
) {
    init {
        httpServer.createContext(path, ::handleRequest)
    }

    fun start() {
        httpServer.start()
    }

    fun stop(delay: Int) {
        httpServer.stop(delay)
    }

    @Throws(IOException::class)
    private fun handleRequest(exchange: HttpExchange) {
        if ("GET" != exchange.requestMethod) {
            exchange.close()
        } else {
            val headers = exchange.requestHeaders
            val username = headers.getFirst("X-Minecraft-Username")
            val rawUuid = headers.getFirst("X-Minecraft-UUID")
            val clientVersion = headers.getFirst("X-Minecraft-Version")
            val clientVersionId = headers.getFirst("X-Minecraft-Version-ID")
            val rawPackFormat = headers.getFirst("X-Minecraft-Pack-Format")
            if (username != null && rawUuid != null && clientVersion != null && clientVersionId != null && rawPackFormat != null) {
                val (uuid: UUID, packFormat: Int) = try {
                    uuidFromUndashedString(rawUuid) to rawPackFormat.toInt()
                } catch (exception: IllegalArgumentException) {
                    handler.onInvalidRequest(exchange)
                    exchange.close()
                    return
                }

                val request = ResourcePackRequest(uuid, username, clientVersion, clientVersionId, packFormat)
                try {
                    exchange.use { handler.onRequest(request, exchange) }
                } catch (exception: Exception) {
                    handler.onException(exception)
                }
            } else {
                handler.onInvalidRequest(exchange)
                exchange.close()
            }
        }
    }

    companion object {
        private val UNDASHED_STRING_REGEX = Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
        private const val DASHED_STRING_REPLACEMENT = "$1-$2-$3-$4-$5"

        private fun uuidFromUndashedString(str: String): UUID {
            return UUID.fromString(str.replace(UNDASHED_STRING_REGEX, DASHED_STRING_REPLACEMENT))
        }

        fun create(hostname: String, port: Int, pack: BuiltResourcePack): ResourcePackServer {
            val address = InetSocketAddress(hostname, port)
            val server = HttpServer.create(address, 0)
            return ResourcePackServer(server, "/", ResourcePackRequestHandler.of(pack))
        }
    }
}
