package dev.andante.audience.player

import com.mojang.serialization.Codec
import dev.andante.audience.Audience
import net.minecraft.server.network.ServerPlayerEntity
import java.util.function.Consumer

/**
 * A list of players stored as references.
 */
class PlayerList() : HashSet<StandalonePlayerReference>(), Audience {
    constructor(players: Collection<PlayerReference>) : this() {
        addAll(players.map(PlayerReference::getHardReference))
    }

    /**
     * @return a list containing only elements matching the given [predicate]
     */
    inline fun filter(predicate: (PlayerReference) -> Boolean): PlayerList {
        return PlayerList(filterTo(HashSet(), predicate))
    }

    /**
     * Combines this list and another list.
     * @return a new list
     */
    operator fun plus(other: PlayerList): PlayerList {
        val list = PlayerList(this)
        list.addAll(other)
        return list
    }

    /**
     * Joins this list into a string of the players' names.
     */
    fun joinToNamedString(): String {
        return joinToString { it.playerName }
    }

    /**
     * Converts this list into a list of player entities.
     * @return all online players in this list
     */
    fun toPlayers(): List<ServerPlayerEntity> {
        return mapNotNull(PlayerReference::getPlayer)
    }

    /**
     * Copies the player list.
     * @return a copy of this player list
     */
    fun copy(): PlayerList {
        return PlayerList(this)
    }

    /**
     * Performs [action] for each player in this list.
     */
    fun forEachPlayer(action: Consumer<in ServerPlayerEntity>) {
        toPlayers().forEach(action)
    }

    override fun getAudiencePlayers(): PlayerList {
        return this
    }

    companion object {
        /**
         * The codec of this class.
         */
        val CODEC: Codec<PlayerList> = StandalonePlayerReference.CODEC.listOf().xmap(::PlayerList) { it.toList() }
    }
}
