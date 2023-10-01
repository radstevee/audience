package dev.andante.audience.mixin;

import dev.andante.audience.block.BlockStateLike;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public class BlockStateMixin implements BlockStateLike {
    @Unique
    @NotNull
    @Override
    public BlockState getAudienceBlockState() {
        return (BlockState) (Object) this;
    }
}
