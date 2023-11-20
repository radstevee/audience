package dev.andante.audience.player;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.andante.audience.Audience;
import dev.andante.audience.AudienceInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * A safe reference to a player.
 */
public interface PlayerReference extends Audience {
    /**
     * The codec of this class.
     */
    Codec<PlayerReference> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Uuids.CODEC.fieldOf("uuid").forGetter(PlayerReference::getReferenceUuid)
            ).apply(instance, StandalonePlayerReference::new)
    );

    /**
     * @return the UUID of the player
     */
    @NotNull
    default UUID getReferenceUuid() {
        throw new AssertionError();
    }

    /**
     * Returns a standalone version of this player reference.
     *
     * <p>
     * Useful for storing instances of a player reference in a memory leak-safe way.
     * For example, {@link ServerPlayerEntity} objects are instances of {@link PlayerReference},
     * but {@link ServerPlayerEntity} objects should not persist after the player leaves or respawns,
     * whereas {@link PlayerReference} objects can.
     * </p>
     *
     * @return the cached version of this player reference
     */
    default StandalonePlayerReference getHardReference() {
        UUID uuid = this.getReferenceUuid();
        return PlayerReferences.INSTANCE.getOrCreate(uuid);
    }

    /**
     * The online player for this reference.
     */
    @Nullable
    default ServerPlayerEntity getPlayer() {
        MinecraftServer server = AudienceInitializer.INSTANCE.getServer();
        PlayerManager playerManager = server.getPlayerManager();
        UUID uuid = this.getReferenceUuid();
        return playerManager.getPlayer(uuid);
    }

    /**
     * The name of this player. If they are not online, 'Unknown'.
     */
    default String getPlayerName() {
        ServerPlayerEntity player = this.getPlayer();
        if (player == null) {
            // attempt to retrieve username from user cache
            MinecraftServer server = AudienceInitializer.INSTANCE.getServer();
            UserCache userCache = server.getUserCache();
            if (userCache != null) {
                UUID uuid = this.getReferenceUuid();
                Optional<GameProfile> maybeProfile = userCache.getByUuid(uuid);
                if (maybeProfile.isPresent()) {
                    GameProfile profile = maybeProfile.get();
                    return profile.getName();
                }
            }

            // concede and return unknown
            return "Unknown";
        } else {
            // return online name
            return player.getGameProfile().getName();
        }
    }

    /**
     * @return whether the player referenced is online
     */
    default boolean isPlayerOnline() {
        return this.getPlayer() != null;
    }

    @Override
    default PlayerList getAudiencePlayers() {
        ServerPlayerEntity player = this.getPlayer();
        if (player == null) {
            return new PlayerList();
        }

        return new PlayerList(Collections.singletonList(player));
    }
}
