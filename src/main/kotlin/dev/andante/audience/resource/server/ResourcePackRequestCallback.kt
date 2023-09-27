package dev.andante.audience.resource.server

import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

/**
 * A callback function for a S2C resource pack request.
 */
fun interface ResourcePackRequestCallback {
    /**
     * Called when the server receiives [ResourcePackStatusC2SPacket]
     * after a resource pack was sent to the client.
     */
    fun onStatus(status: ResourcePackStatusC2SPacket.Status)

    companion object {
        private val registeredCallbacks: MutableMap<UUID, ResourcePackRequestCallback> = mutableMapOf()

        /**
         * Sets the callback of [player].
         */
        fun setCallback(player: ServerPlayerEntity, callback: ResourcePackRequestCallback?) {
            if (callback == null) {
                registeredCallbacks.remove(player.uuid)
            } else {
                registeredCallbacks[player.uuid] = callback
            }
        }

        /**
         * Retrieves the callback of the player with [uuid].
         * @return a resource pack request callback
         */
        fun getCallback(uuid: UUID): ResourcePackRequestCallback? {
            return registeredCallbacks[uuid]
        }

        /**
         * Clears the callback of the player with [uuid].
         * @return a resource pack request callback
         */
        fun clearCallback(uuid: UUID) {
            registeredCallbacks.remove(uuid)
        }
    }
}
