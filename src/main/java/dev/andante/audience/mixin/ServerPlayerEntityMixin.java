package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import dev.andante.audience.mixinterface.AudiencePlayerAccessor;
import dev.andante.audience.player.PlayerList;
import dev.andante.audience.player.PlayerReference;
import dev.andante.audience.resource.server.ResourcePackProperties;
import dev.andante.audience.resource.server.ResourcePackRequestCallback;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements Audience, PlayerReference, AudiencePlayerAccessor {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    /**
     * The last resource pack sent to the player.
     */
    @Unique
    @Nullable
    private ResourcePackProperties lastResourcePack = null;

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

    @Nullable
    @Override
    public ResourcePackProperties getLastResourcePack() {
        return this.lastResourcePack;
    }

    @Override
    public void setResourcePack(ResourcePackProperties properties, @Nullable ResourcePackRequestCallback callback) {
        if (properties == this.lastResourcePack) {
            if (callback != null) {
                callback.onStatus(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED);
            }

            return;
        }

        if (properties != null) {
            this.networkHandler.sendPacket(
                    new ResourcePackSendS2CPacket(
                            properties.getUrl(),
                            properties.getHash(),
                            properties.getRequired(),
                            properties.getPrompt()
                    )
            );
        }

        this.lastResourcePack = properties;
        ResourcePackRequestCallback.Companion.setCallback(networkHandler.player, callback);
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        AudiencePlayerAccessor oldPlayerAccessor = (AudiencePlayerAccessor) oldPlayer;
        this.lastResourcePack = oldPlayerAccessor.getLastResourcePack();
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
