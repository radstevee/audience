package dev.andante.audience

import dev.andante.audience.player.PlayerList

/**
 * An audience composed of other audiences.
 */
class CompoundAudience(
    /**
     * All audiences inside this compound.
     */
    private vararg val audiences: Audience
) : Audience {
    override fun getAudiencePlayers(): PlayerList {
        val players = audiences.flatMap(Audience::getAudiencePlayers)
        return PlayerList(players)
    }
}
