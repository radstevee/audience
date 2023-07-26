package dev.andante.audience.player

import com.google.gson.JsonArray
import dev.andante.audience.Audience
import net.minecraft.server.network.ServerPlayerEntity
import java.util.function.Consumer

/**
 * A list of players stored as UUIDs.
 */
class PlayerList() : HashSet<PlayerReference>(), Audience {
    constructor(players: Collection<PlayerReference>) : this() {
        addAll(players)
    }

    /**
     * Converts this list into a list of Brawls player entities.
     * @return all online players in this list
     */
    private fun toPlayers(): List<ServerPlayerEntity> {
        return mapNotNull(PlayerReference::getPlayer)
    }

    /**
     * Performs [action] for each player in this list.
     */
    fun forEachPlayer(action: Consumer<in ServerPlayerEntity>) {
        toPlayers().forEach(action)
    }

    /**
     * @return a list containing only elements matching the given [predicate]
     */
    inline fun filter(predicate: (PlayerReference) -> Boolean): PlayerList {
        return PlayerList(filterTo(ArrayList(), predicate))
    }

    /**
     * Combines this list and another list.
     * @return a new list
     */
    operator fun plus(other: PlayerList): PlayerList {
        val list = PlayerList()
        list.addAll(this)
        list.addAll(other)
        return list
    }

    /**
     * Joins this list into a string of the players' names.
     */
    fun joinToNamedString(): String {
        return joinToString(transform = PlayerReference::getPlayerName)
    }

    override fun getAudiencePlayers(): List<ServerPlayerEntity> {
        return toPlayers()
    }

    /**
     * Converts this player list to its json representation.
     */
    fun toJsonArray(): JsonArray {
        val json = JsonArray()
        forEach { uuid -> json.add(uuid.toString()) }
        return json
    }
}
