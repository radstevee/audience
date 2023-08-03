package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import dev.andante.audience.player.PlayerList;
import dev.andante.audience.player.PlayerReference;
import dev.andante.audience.resource.ServerResourcePackSettings;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements Audience, PlayerReference {
    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Unique private ServerResourcePackSettings latestResourcePack = null;

    @Override
    public @NotNull PlayerList getAudiencePlayers() {
        ServerPlayerEntity that = (ServerPlayerEntity) (Object) this;
        return new PlayerList(Collections.singletonList(that));
    }

    @Override
    public @NotNull UUID getReferenceUuid() {
        ServerPlayerEntity that = (ServerPlayerEntity) (Object) this;
        return that.getUuid();
    }

    @Override
    public void setResourcePack(@Nullable ServerResourcePackSettings resourcePack) {
        if (resourcePack == this.latestResourcePack) {
            return;
        }

        if (resourcePack != null) {
            this.networkHandler.sendPacket(new ResourcePackSendS2CPacket(
                    resourcePack.getUrl(),
                    resourcePack.getHash(),
                    resourcePack.getRequired(),
                    resourcePack.getPrompt()
            ));
        }

        this.latestResourcePack = resourcePack;
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
