package dev.andante.audience

import dev.andante.audience.player.PlayerSet

/**
 * An audience composed of other audiences.
 */
class CompoundAudience(
    /**
     * All audiences inside this compound.
     */
    private vararg val audiences: Audience
) : Audience {
    override fun getAudiencePlayers(): PlayerSet {
        val players = audiences.flatMap(Audience::getAudiencePlayers)
        return PlayerSet(players)
    }
}
