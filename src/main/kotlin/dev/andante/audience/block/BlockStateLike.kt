package dev.andante.audience.block

import net.minecraft.block.BlockState

/**
 * An interface which allows an object to be represented by a block state.
 */
interface BlockStateLike {
    /**
     * The block state representation of this object.
     */
    fun getAudienceBlockState(): BlockState {
        throw AssertionError("Not implemented in mixin")
    }
}
