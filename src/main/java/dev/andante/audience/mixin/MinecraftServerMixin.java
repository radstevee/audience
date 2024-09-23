package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import dev.andante.audience.player.PlayerSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements Audience {
    @Shadow private PlayerManager playerManager;

    @Unique
    @Override
    public PlayerSet getAudiencePlayers() {
        List<ServerPlayerEntity> players = playerManager.getPlayerList();
        return new PlayerSet(players);
    }
}
