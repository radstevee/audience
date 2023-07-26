package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import dev.andante.audience.player.PlayerReference;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements Audience, PlayerReference {
    @Override
    public @NotNull List<ServerPlayerEntity> getAudiencePlayers() {
        ServerPlayerEntity that = (ServerPlayerEntity) (Object) this;
        return Collections.singletonList(that);
    }

    @Override
    public @NotNull UUID getReferenceUuid() {
        ServerPlayerEntity that = (ServerPlayerEntity) (Object) this;
        return that.getUuid();
    }
}
