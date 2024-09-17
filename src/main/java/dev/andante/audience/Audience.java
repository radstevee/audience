package dev.andante.audience;

import dev.andante.audience.player.PlayerSet;
import dev.andante.audience.sound.ISound;
import dev.andante.audience.sound.SoundStop;
import dev.andante.audience.title.Title;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;

import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

@SuppressWarnings("unused")
public interface Audience {
    /**
     * The players of this audience.
     */
    default PlayerSet getAudiencePlayers() {
        return new PlayerSet();
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
        packet(new ClearTitleS2CPacket(true));
    }

    /**
     * Sends the given action bar to all audience players.
     */
    default void actionBar(Text text) {
        forEachAudience(player -> player.sendMessage(text, true));
    }

    /**
     * Sends the given action bar to all audience players.
     */
    default void actionBar(Function<ServerPlayerEntity, Text> text) {
        forEachAudience(player -> player.actionBar(text.apply(player)));
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
    default void sound(ISound sound) {
        positionedSound(sound, ServerPlayerEntity::getPos);
    }

    /**
     * Broadcasts the given sound to the audience from the given position.
     */
    default void sound(ISound sound, Vec3d pos) {
        PlaySoundS2CPacket packet = sound.createPacket(pos);
        packet(packet);
    }

    /**
     * Broadcasts the given sound to the audience from the given entity.
     */
    default void sound(ISound sound, Entity entity) {
        sound(sound, entity.getPos());
    }

    /**
     * Broadcasts the given sound to the audience at the position provided by [positionSupplier].
     */
    default void positionedSound(ISound sound, Function<ServerPlayerEntity, Vec3d> positionSupplier) {
        packet(player -> sound.createPacket(positionSupplier.apply(player)));
    }

    /**
     * Broadcasts the given sound to the audience at the given position.
     */
    default void positionedSound(ISound sound, Vec3d position) {
        packet(sound.createPacket(position));
    }

    /**
     * Stops the given sound on all audience players.
     */
    default void stopSound(SoundStop soundStop) {
        packet(soundStop.getPacket());
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
        forEachAudience(expectedPlayer -> {
            MinecraftServer server = expectedPlayer.server;
            PlayerManager playerManager = server.getPlayerManager();
            ServerPlayerEntity player = playerManager.getPlayer(expectedPlayer.getUuid());
            if (player != null) {
                ServerPlayNetworkHandler handler = player.networkHandler;
                if (player.notInAnyWorld) {
                    player.notInAnyWorld = false;
                    handler.player = playerManager.respawnPlayer(player, true, Entity.RemovalReason.KILLED);
                } else {
                    if (!(player.getHealth() > 0)) {
                        handler.player = playerManager.respawnPlayer(player, false, Entity.RemovalReason.KILLED);
                    }
                }
            }
        });
    }

    /**
     * Sets all audience player's world borders to make a red tint on their vignette.
     */
    default void setTintPercentage(float percentage) {
        float clampedPercentage = clamp(percentage, 0.0f, 1.0f);
        packet(player -> {
            WorldBorder border = new WorldBorder();

            border.setCenter(player.getX(), player.getZ());

            double baseSize = 10_000_000;
            border.setSize(baseSize);

            double warningDistance = clampedPercentage * (baseSize * 10);
            border.setWarningBlocks(floor(warningDistance));

            border.setWarningTime(0);

            return new WorldBorderInitializeS2CPacket(border);
        });
    }

    /**
     * Sets all audience players' world borders.
     */
    default void setWorldBorder(WorldBorder border) {
        packet(new WorldBorderInitializeS2CPacket(border));
    }

    /**
     * Resets all audience players' world borders to that of their world.
     */
    default void resetWorldBorder() {
        packet(player -> {
            ServerWorld world = player.getServerWorld();
            WorldBorder border = world.getWorldBorder();
            return new WorldBorderInitializeS2CPacket(border);
        });
    }

    /**
     * Sends the given packet to all audience players.
     */
    default void packet(Packet<?> packet) {
        forEachAudience(player -> player.networkHandler.sendPacket(packet));
    }

    /**
     * Sends the given packet to all audience players.
     */
    default void packet(Function<ServerPlayerEntity, Packet<?>> packet) {
        forEachAudience(player -> player.networkHandler.sendPacket(packet.apply(player)));
    }

    /**
     * Calls the given action for each audience player.
     */
    default void forEachAudience(Consumer<ServerPlayerEntity> action) {
        getAudiencePlayers().toPlayers().forEach(action);
    }
}
