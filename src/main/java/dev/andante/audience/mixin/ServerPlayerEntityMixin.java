package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import dev.andante.audience.mixinterface.AudiencePlayerAccessor;
import dev.andante.audience.player.PlayerList;
import dev.andante.audience.player.PlayerReference;
import dev.andante.audience.resource.server.ResourcePackProperties;
import dev.andante.audience.resource.server.ResourcePackRequestCallback;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
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

    /**
     * The current callback to perform when the client responds to the resource pack.
     */
    @Unique
    @Nullable
    private ResourcePackRequestCallback resourcePackRequestCallback = null;

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

    @Nullable
    @Override
    public ResourcePackRequestCallback getResourcePackRequestCallback() {
        return this.resourcePackRequestCallback;
    }

    @Nullable
    @Override
    public ResourcePackRequestCallback clearResourcePackRequestCallback() {
        ResourcePackRequestCallback callback = this.resourcePackRequestCallback;
        this.resourcePackRequestCallback = null;
        return callback;
    }

    @Override
    public void setResourcePack(ResourcePackProperties properties, @Nullable ResourcePackRequestCallback callback) {
        if (properties == this.lastResourcePack) {
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
        this.resourcePackRequestCallback = callback;
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        AudiencePlayerAccessor oldPlayerAccessor = (AudiencePlayerAccessor) oldPlayer;
        this.lastResourcePack = oldPlayerAccessor.getLastResourcePack();
        this.resourcePackRequestCallback = oldPlayerAccessor.getResourcePackRequestCallback();
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
