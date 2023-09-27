package dev.andante.audience.mixin;

import dev.andante.audience.resource.server.ResourcePackRequestCallback;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin {
    @Unique @Nullable
    private UUID uuid;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData, CallbackInfo ci) {
        this.uuid = clientData.gameProfile().getId();
    }

    /**
     * Performs the stored resource pack request callback if present.
     */
    @Inject(
            method = "onResourcePackStatus",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void performCallback(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (this.uuid == null) {
            return;
        }

        ResourcePackRequestCallback callback = ResourcePackRequestCallback.Companion.getCallback(this.uuid);
        if (callback != null) {
            ResourcePackStatusC2SPacket.Status status = packet.getStatus();

            // callback
            callback.onStatus(status);

            // clear on final response
            if (status != ResourcePackStatusC2SPacket.Status.ACCEPTED) {
                ResourcePackRequestCallback.Companion.clearCallback(this.uuid);
            }
        }
    }

    /**
     * Removes the stored resource pack request callback on disconnect.
     */
    @Inject(method = "onDisconnected", at = @At("TAIL"))
    private void removeCallbackOnDisconnected(Text reason, CallbackInfo ci) {
        if (this.uuid != null) {
            ResourcePackRequestCallback.Companion.clearCallback(this.uuid);
        }
    }
}
