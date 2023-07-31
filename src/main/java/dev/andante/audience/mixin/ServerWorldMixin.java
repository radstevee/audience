package dev.andante.audience.mixin;

import dev.andante.audience.Audience;
import dev.andante.audience.player.PlayerList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements Audience {
    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Override
    public PlayerList getAudiencePlayers() {
        return new PlayerList(getPlayers());
    }
}
