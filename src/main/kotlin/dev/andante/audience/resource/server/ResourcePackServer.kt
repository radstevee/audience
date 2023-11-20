package dev.andante.audience.resource.server

import com.sun.net.httpserver.HttpServer
import dev.andante.audience.resource.ResourcePack
import net.minecraft.server.MinecraftServer.ServerResourcePackProperties
import net.minecraft.text.Text
import java.net.InetSocketAddress
import java.util.UUID

/**
 * A wrapper around an http server for serving resource packs.
 */
class ResourcePackServer(
    /**
     * The root address of this server.
     */
    rootAddress: String,

    /**
     * The port of this server.
     */
    port: Int = 0
) {
    /**
     * The socket address of this server.
     */
    private val socketAddress = InetSocketAddress(rootAddress, port)

    /**
     * The underlying http server.
     */
    private val httpServer: HttpServer = HttpServer.create(socketAddress, 0)

    /**
     * The compiled root address.
     */
    val address = "http://$rootAddress:$port"

    /**
     * The port of this server.
     */
    val port = socketAddress.port

    /**
     * Starts the http server.
     */
    fun startServer() {
        httpServer.start()
    }

    /**
     * Stops the http server.
     */
    fun stopServer(delay: Int = 0) {
        httpServer.stop(delay)
    }

    /**
     * Registers a resource pack to be handled by this server.
     */
    fun registerResourcePack(resourcePack: ResourcePack): ServerResourcePackProperties {
        val hash = resourcePack.hash
        httpServer.createContext("/$hash", ResourcePackRequestHandler(resourcePack))
        return createResourcePackProperties(resourcePack)
    }

    /**
     * Creates a resource pack properties object based on [resourcePack] and this server.
     */
    fun createResourcePackProperties(
        resourcePack: ResourcePack,
        required: Boolean = true,
        prompt: Text? = null
    ): ServerResourcePackProperties {
        val url = getUrl(resourcePack)
        return ServerResourcePackProperties(UUID.nameUUIDFromBytes(resourcePack.bytes), url, resourcePack.hash, required, prompt)
    }

    /**
     * Gets the address of the given [resourcePack].
     * @return a url string
     */
    fun getUrl(resourcePack: ResourcePack): String {
        val hash = resourcePack.hash
        return "$address/$hash"
    }
}
