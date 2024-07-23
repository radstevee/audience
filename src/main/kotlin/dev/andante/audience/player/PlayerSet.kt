package dev.andante.audience.player

import com.mojang.serialization.Codec
import dev.andante.audience.Audience
import net.minecraft.server.network.ServerPlayerEntity
import java.util.function.Consumer

/**
 * A set of players stored as references.
 */
class PlayerSet() : HashSet<StandalonePlayerReference>(), Audience {
    constructor(players: Collection<PlayerReference>) : this() {
        addAll(players.map(PlayerReference::getHardReference))
    }

    /**
     * @return a set containing only elements matching the given [predicate]
     */
    inline fun filter(predicate: (PlayerReference) -> Boolean): PlayerSet {
        return PlayerSet(filterTo(HashSet(), predicate))
    }

    /**
     * Combines this set and another set.
     * @return a new set
     */
    operator fun plus(other: PlayerSet): PlayerSet {
        val set = PlayerSet(this)
        set.addAll(other)
        return set
    }

    /**
     * Subtracts the [other] iterable from this set.
     * @return a new set
     */
    fun subtract(other: Iterable<PlayerReference>): PlayerSet {
        val set = toMutableSet().subtract(other.toSet())
        return PlayerSet(set)
    }

    /**
     * Joins this set into a string of the players' names.
     */
    fun joinToNamedString(): String {
        return joinToString { it.playerName }
    }

    /**
     * Converts this set into a set of player entities.
     * @return all online players in this set
     */
    fun toPlayers(): List<ServerPlayerEntity> {
        return mapNotNull(PlayerReference::getPlayer)
    }

    /**
     * Copies the player set.
     * @return a copy of this player set
     */
    fun copy(): PlayerSet {
        return PlayerSet(this)
    }

    /**
     * Performs [action] for each player in this set.
     */
    fun forEachPlayer(action: Consumer<in ServerPlayerEntity>) {
        toPlayers().forEach(action)
    }

    override fun getAudiencePlayers(): PlayerSet {
        return this
    }

    companion object {
        /**
         * The codec of this class.
         */
        val CODEC: Codec<PlayerSet> = StandalonePlayerReference.CODEC.listOf().xmap(::PlayerSet) { it.toList() }
    }
}
