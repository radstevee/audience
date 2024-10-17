package dev.andante.audience

import dev.andante.audience.resource.ResourcePackHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

object AudienceInitializer : ModInitializer {
    private lateinit var _minecraftServer: MinecraftServer

    /**
     * The Minecraft server instance.
     */
    val server: MinecraftServer get() = _minecraftServer

    override fun onInitialize() {
        // register event to capture server
        ServerLifecycleEvents.SERVER_STARTING.register { _minecraftServer = it }

        ResourcePackHandler.register()
    }
}
