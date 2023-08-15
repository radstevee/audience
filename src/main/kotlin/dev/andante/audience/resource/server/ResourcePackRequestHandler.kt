package dev.andante.audience.resource.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import dev.andante.audience.resource.ResourcePack
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpGet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Handles requests for a [ResourcePackServer].
 */
class ResourcePackRequestHandler(
    /**
     * The resource pack to be sent/received.
     */
    private val resourcePack: ResourcePack
) : HttpHandler {
    /**
     * The length of the bytes in [resourcePack].
     */
    private val responseLength = resourcePack.bytes.size.toLong()

    override fun handle(exchange: HttpExchange?) {
        // throw npe if null
        exchange ?: run { return }

        // check for get
        if (exchange.requestMethod != HttpGet.METHOD_NAME) {
            exchange.close()
            return
        }

        // handle request
        try {
            sendResourcePack(exchange)
        } catch (exception: Exception) {
            onException(exception)
        } finally {
            exchange.close()
        }
    }

    /**
     * Sends the resource pack as a response to the request.
     */
    @Throws(IOException::class)
    private fun sendResourcePack(exchange: HttpExchange) {
        // write zip as response
        exchange.responseHeaders[HttpHeaders.CONTENT_TYPE] = "application/zip"
        exchange.sendResponseHeaders(200, responseLength)
        exchange.responseBody.write(resourcePack.bytes)
    }

    /**
     * Handles exceptions caught during the exchange.
     */
    private fun onException(exception: Exception) {
        logger.error("Exception caught when serving a resource pack", exception)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("Resource Pack Request Handler")
    }
}
