package dev.andante.audience.resource.server

import com.sun.net.httpserver.HttpExchange
import dev.andante.audience.resource.BuiltResourcePack
import java.io.IOException
import java.nio.charset.StandardCharsets

fun interface ResourcePackRequestHandler {
    @Throws(IOException::class)
    fun onRequest(request: ResourcePackRequest?, exchange: HttpExchange)

    fun onException(exception: Exception) {
        System.err.println("Exception caught when serving a resource-pack")
        exception.printStackTrace()
    }

    @Throws(IOException::class)
    fun onInvalidRequest(exchange: HttpExchange) {
        val response = "Please use a Minecraft client".toByteArray(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(400, response.size.toLong())
        exchange.responseBody.write(response)
    }

    companion object {
        fun of(pack: BuiltResourcePack, validOnly: Boolean = false): ResourcePackRequestHandler {
            return object : ResourcePackRequestHandler {
                @Throws(IOException::class)
                override fun onRequest(request: ResourcePackRequest?, exchange: HttpExchange) {
                    if (request == null && validOnly) {
                        super.onInvalidRequest(exchange)
                    } else {
                        val data = pack.bytes
                        exchange.responseHeaders["Content-Type"] = "application/zip"
                        exchange.sendResponseHeaders(200, data.size.toLong())
                        exchange.responseBody.write(data)
                    }
                }

                @Throws(IOException::class)
                override fun onInvalidRequest(exchange: HttpExchange) {
                    onRequest(null, exchange)
                }
            }
        }
    }
}
