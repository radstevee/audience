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

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj instanceof PlayerReference reference) {
            return reference.getReferenceUuid() == this.getReferenceUuid();
        }

        return false;
    }
}
