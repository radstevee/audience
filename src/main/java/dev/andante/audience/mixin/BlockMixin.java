package dev.andante.audience.mixin;

import dev.andante.audience.block.BlockStateLike;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin implements BlockStateLike {
    @Shadow private BlockState defaultState;

    @Unique
    @NotNull
    @Override
    public BlockState getAudienceBlockState() {
        return this.defaultState;
    }
}
