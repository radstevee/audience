package dev.andante.audience.mixin;

import dev.andante.audience.mixinterface.AudiencePlayerAccessor;
import dev.andante.audience.resource.server.ResourcePackRequestCallback;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    /**
     * Performs the stored resource pack request callback if present.
     */
    @Inject(
            method = "onResourcePackStatus",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void performCallback(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        ServerPlayerEntity player = this.player;
        AudiencePlayerAccessor playerAccessor = (AudiencePlayerAccessor) player;
        ResourcePackRequestCallback callback = playerAccessor.getResourcePackRequestCallback();
        if (callback != null) {
            ResourcePackStatusC2SPacket.Status status = packet.getStatus();

            // callback
            callback.onStatus(status);

            // clear on final response
            if (status != ResourcePackStatusC2SPacket.Status.ACCEPTED) {
                playerAccessor.clearResourcePackRequestCallback();
            }
        }
    }
}
