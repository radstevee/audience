package dev.andante.audience.player

import com.mojang.serialization.Codec
import dev.andante.audience.Audience
import net.minecraft.server.network.ServerPlayerEntity
import java.util.function.Consumer

/**
 * A list of players stored as references.
 */
class PlayerList() : AbstractMutableSet<PlayerReference>(), Audience {
    private val backingSet: MutableSet<StandalonePlayerReference> = mutableSetOf()

    constructor(players: Collection<PlayerReference>) : this() {
        addAll(players)
    }

    override val size: Int get() = backingSet.size

    override fun iterator(): MutableIterator<PlayerReference> {
        return backingSet.iterator()
    }

    /**
     * Adds a hard reference to the list, from [element].
     */
    override fun add(element: PlayerReference): Boolean {
        return backingSet.add(element.hardReference)
    }

    override fun remove(element: PlayerReference): Boolean {
        return backingSet.remove(element.hardReference)
    }

    override fun contains(element: PlayerReference): Boolean {
        return backingSet.contains(element.hardReference)
    }

    override fun clear() {
        backingSet.clear()
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
        return PlayerList(backingSet + other)
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
     * Performs [action] for each player in this list.
     */
    fun forEachPlayer(action: Consumer<in ServerPlayerEntity>) {
        toPlayers().forEach(action)
    }

    override fun getAudiencePlayers(): List<ServerPlayerEntity> {
        return toPlayers()
    }

    companion object {
        /**
         * The codec of this class.
         */
        val CODEC: Codec<PlayerList> = PlayerReference.CODEC.listOf().xmap(::PlayerList) { it.backingSet.toList() }
    }
}
