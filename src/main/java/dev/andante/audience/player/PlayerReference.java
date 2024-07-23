package dev.andante.audience.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import dev.andante.audience.Audience;
import dev.andante.audience.AudienceInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * A safe reference to a player.
 */
public interface PlayerReference extends Audience {
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
     * The name of this player. If they are not online and the username cannot be found, "Unknown".
     */
    default String getPlayerName(boolean useApi) {
        ServerPlayerEntity player = this.getPlayer();
        if (player == null) {
            // check user cache
            String userCacheName = getUserCachePlayerName();
            if (userCacheName != null) {
                return userCacheName;
            } else {
                if (useApi) {
                    // check mojang api
                    String apiName = getMojangApiPlayerName();
                    if (apiName != null) {
                        return apiName;
                    }
                }

                // concede and return unknown
                return "Unknown";
            }
        } else {
            // return online name
            return player.getGameProfile().getName();
        }
    }

    /**
     * The name of this player. If they are not online and the username cannot be found, "Unknown".
     */
    default String getPlayerName() {
        return this.getPlayerName(false);
    }

    /**
     * The name of this player as stored in the server's user cache.
     */
    @Nullable
    default String getUserCachePlayerName() {
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

        return null;
    }

    @Nullable
    default String getMojangApiPlayerName() {
        UUID uuid = this.getReferenceUuid();
        String urlString = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;
        try {
            URL url = new URL(urlString);
            try (InputStream stream = url.openStream()) {
                InputStreamReader reader = new InputStreamReader(stream);
                JsonElement jsonElement = JsonParser.parseReader(reader);
                if (jsonElement instanceof JsonObject json) {
                    if (json.has("name")) {
                        return json.get("name").getAsString();
                    }
                }
            }
        } catch (IOException | UnsupportedOperationException | IllegalStateException | JsonParseException ignored) {
        }

        return null;
    }

    /**
     * @return whether the player referenced is online
     */
    default boolean isPlayerOnline() {
        return this.getPlayer() != null;
    }

    @Override
    default PlayerSet getAudiencePlayers() {
        ServerPlayerEntity player = this.getPlayer();
        if (player == null) {
            return new PlayerSet();
        }

        return new PlayerSet(Collections.singletonList(player));
    }
}
