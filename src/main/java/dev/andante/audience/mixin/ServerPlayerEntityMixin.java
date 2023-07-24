package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements Audience {
    @Override
    public @NotNull List<ServerPlayerEntity> getAudiencePlayers() {
        return Collections.singletonList((ServerPlayerEntity) (Object) this);
    }
}
