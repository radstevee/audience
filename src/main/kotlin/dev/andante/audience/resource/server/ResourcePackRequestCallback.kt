package dev.andante.audience.resource.server

import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket

/**
 * A callback function for a S2C resource pack request.
 */
fun interface ResourcePackRequestCallback {
    /**
     * Called when the server receives [ResourcePackStatusC2SPacket]
     * after a resource pack was sent to the client.
     */
    fun onStatus(status: ResourcePackStatusC2SPacket.Status)
}
