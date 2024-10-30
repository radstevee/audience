package dev.andante.audience.resource

import io.netty.channel.ChannelHandlerContext
import net.mcbrawls.inject.api.http.HttpByteBuf
import net.mcbrawls.inject.api.http.HttpByteBuf.httpBuf
import net.mcbrawls.inject.api.http.HttpInjector
import net.mcbrawls.inject.api.http.HttpRequest

object ResourcePackHandler : HttpInjector() {
    private val resourcePacks: MutableMap<String, ByteArray> = mutableMapOf()

    override fun intercept(ctx: ChannelHandlerContext, request: HttpRequest): HttpByteBuf {
        val response = httpBuf(ctx)

        val path = request.requestURI.removePrefix("/")
        val pack = resourcePacks[path] ?: return response

        if (!ctx.channel().isActive) {
            return response
        }

        response.writeStatusLine("1.1", 200, "OK")
        response.writeHeader("Content-Type", "application/zip")
        response.writeHeader("Content-Length", pack.size.toString())
        response.writeBytes(pack)

        return response
    }

    /**
     * Adds a resource pack to be served.
     */
    fun add(vararg packs: ByteResourcePack) {
        packs.forEach { pack ->
            resourcePacks[pack.hash] = pack.bytes
        }
    }

    /**
     * Removes a resource pack from service.
     * @return whether a pack was removed
     */
    fun remove(pack: ByteResourcePack): Boolean {
        return remove(pack.hash)
    }

    /**
     * Removes a resource pack from service.
     * @return whether a pack was removed
     */
    fun remove(hash: String): Boolean {
        return resourcePacks.remove(hash) != null
    }
}
