package dev.andante.audience;

import dev.andante.audience.sound.Sound;
import dev.andante.audience.sound.SoundStop;
import dev.andante.audience.title.Title;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus.Experimental;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface Audience {
    /**
     * The players of this audience.
     */
    default List<ServerPlayerEntity> getAudiencePlayers() {
        return Collections.emptyList();
    }

    /**
     * Teleports the audience to the given world using the given position and rotation.
     */
    default void teleport(ServerWorld world, Vec3d position, Vec2f rotation) {
        forEachAudience(player -> player.teleport(world, position.x, position.y, position.z, rotation.x, rotation.y));
    }

    /**
     * Teleports the audience to the given world using the given position function.
     */
    default void teleport(ServerWorld world, Function<ServerPlayerEntity, Vec3d> positionFunction) {
        forEachAudience( player -> {
            Vec3d position = positionFunction.apply(player);
            player.teleport(world, position.x, position.y, position.z, 0.0f, 0.0f);
        });
    }

    /**
     * Teleports the audience to the given world using the given position and rotation function.
     */
    default void teleportWithRotation(ServerWorld world, Function<ServerPlayerEntity, Pair<Vec3d, Vec2f>> positionRotationFunction) {
        forEachAudience(player -> {
            Pair<Vec3d, Vec2f> positionAndRotation = positionRotationFunction.apply(player);
            Vec3d position = positionAndRotation.getLeft();
            Vec2f rotation = positionAndRotation.getRight();
            player.teleport(world, position.x, position.y, position.z, rotation.x, rotation.y);
        });
    }

    /**
     * Broadcasts the given message to the audience.
     */
    default void message(Text text) {
        forEachAudience(player -> player.sendMessage(text));
    }

    /**
     * Broadcasts the given message to the audience.
     */
    default void message(Function<ServerPlayerEntity, Text> text) {
        forEachAudience(player -> player.sendMessage(text.apply(player)));
    }

    /**
     * Broadcasts the given title to the audience.
     */
    default void title(Title title) {
        BundleS2CPacket packet = title.getPackets().getBundled();
        forEachAudience(player -> player.networkHandler.sendPacket(packet));
    }

    /**
     * Broadcasts the given title to the audience.
     */
    default void title(Function<ServerPlayerEntity, Title> title) {
        forEachAudience(player -> {
            BundleS2CPacket packet = title.apply(player).getPackets().getBundled();
            player.networkHandler.sendPacket(packet);
        });
    }

    /**
     * Clears the titles of all audience players.
     */
    default void clearTitle() {
        sendAllPacket(new ClearTitleS2CPacket(true));
    }

    /**
     * Sends the given action bar to all audience players.
     */
    default void actionBar(Text text) {
        forEachAudience(player -> player.sendMessage(text, true));
    }

    /**
     * Clears the action bar of all audience players.
     */
    default void clearActionBar() {
        actionBar(Text.empty());
    }

    /**
     * Broadcasts the given sound to the audience.
     */
    default void sound(Sound sound) {
        sendAllPacket(sound.getPacket());
    }

    /**
     * Broadcasts the given sound to the audience at the position provided by [positionSupplier].
     */
    default void positionedSound(Sound sound, Function<ServerPlayerEntity, Vec3d> positionSupplier) {
        sendAllPacket(player -> sound.packet(positionSupplier.apply(player)));
    }

    /**
     * Broadcasts the given sound to the audience at the given position.
     */
    default void positionedSound(Sound sound, Vec3d position) {
        sendAllPacket(sound.packet(position));
    }

    /**
     * Stops the given sound on all audience players.
     */
    default void stopSound(SoundStop soundStop) {
        sendAllPacket(soundStop.getPacket());
    }

    /**
     * Teleports all audience players to the given [world] and [position].
     */
    default void teleport(ServerWorld world, Vec3d position) {
        forEachAudience(player -> player.teleport(world, position.x, position.y, position.z, 0.0f, 0.0f));
    }

    /**
     * Forces all audience players to respawn.
     */
    default void respawn() {
        forEachAudience(player -> {
            ServerPlayNetworkHandler handler = player.networkHandler;
            PlayerManager playerManager = player.server.getPlayerManager();
            if (player.notInAnyWorld) {
                player.notInAnyWorld = false;
                handler.player = playerManager.respawnPlayer(player, true);
            } else {
                if (!(player.getHealth() > 0)) {
                    handler.player = playerManager.respawnPlayer(player, false);
                }
            }
        });
    }

    /**
     * Calls the given action for each audience player.
     */
    default void forEachAudience(Consumer<ServerPlayerEntity> action) {
        getAudiencePlayers().forEach(action);
    }

    /**
     * Sends the given packet to all audience players.
     */
    @Experimental
    default void sendAllPacket(Packet<?> packet) {
        forEachAudience(player -> player.networkHandler.sendPacket(packet));
    }

    /**
     * Sends the given packet to all audience players.
     */
    @Experimental
    default void sendAllPacket(Function<ServerPlayerEntity, Packet<?>> packet) {
        forEachAudience(player -> player.networkHandler.sendPacket(packet.apply(player)));
    }
}
