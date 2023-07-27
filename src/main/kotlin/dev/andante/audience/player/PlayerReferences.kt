package dev.andante.audience.player

import com.yundom.kache.Builder
import com.yundom.kache.Kache
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import java.util.UUID

/**
 * An object for managing [PlayerReference] objects.
 */
object PlayerReferences {
    /**
     * All players who have ever joined the server.
     */
    private val cachedPlayers: Kache<UUID, StandalonePlayerReference> = Builder.build()

    init {
        // register join event
        ServerPlayConnectionEvents.INIT.register { handler, _ -> getOrCreate(handler.player.uuid) }
    }

    /**
     * Gets a player reference from the cache, or creates and caches one.
     */
    fun getOrCreate(uuid: UUID): StandalonePlayerReference {
        return if (cachedPlayers.exist(uuid)) {
            cachedPlayers.get(uuid)!!
        } else {
            val reference = StandalonePlayerReference(uuid)
            cachedPlayers.put(uuid, reference)
            reference
        }
    }
}
