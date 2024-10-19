package dev.andante.audience.mixin;

import com.mojang.authlib.GameProfile;
import dev.andante.audience.Audience;
import dev.andante.audience.player.PlayerReference;
import dev.andante.audience.player.PlayerSet;
import dev.andante.audience.player.StandalonePlayerReference;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(GameProfile.class)
public abstract class GameProfileMixin implements Audience, PlayerReference {
    @Override
    public @NotNull PlayerSet getAudiencePlayers() {
        StandalonePlayerReference reference = this.getHardReference();
        return new PlayerSet(List.of(reference));
    }

    @Override
    public @NotNull UUID getReferenceUuid() {
        GameProfile that = (GameProfile) (Object) this;
        return that.getId();
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other)) {
            return true;
        }

        if (other instanceof PlayerReference reference) {
            return reference.getReferenceUuid() == this.getReferenceUuid();
        }

        return false;
    }
}
