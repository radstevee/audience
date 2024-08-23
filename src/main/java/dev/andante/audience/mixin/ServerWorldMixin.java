package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import dev.andante.audience.player.PlayerSet;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements Audience {
    private ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow @Final private MinecraftServer server;

    @Override
    public PlayerSet getAudiencePlayers() {
        RegistryKey<World> registryKey = this.getRegistryKey();

        PlayerManager playerManager = server.getPlayerManager();
        List<ServerPlayerEntity> players = playerManager.getPlayerList();
        List<ServerPlayerEntity> worldPlayers = players
                .stream()
                .filter(
                        player -> {
                            ServerWorld world = player.getServerWorld();
                            RegistryKey<World> playerWorldKey = world.getRegistryKey();
                            return playerWorldKey == registryKey;
                        }
                )
                .toList();

        return new PlayerSet(worldPlayers);
    }
}
