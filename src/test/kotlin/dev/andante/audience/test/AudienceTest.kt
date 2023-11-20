package dev.andante.audience.test

import dev.andante.audience.Audience
import dev.andante.audience.resource.ResourcePack
import dev.andante.audience.resource.server.ResourcePackServer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.item.Items
import net.minecraft.server.MinecraftServer.ServerResourcePackProperties
import net.minecraft.util.TypedActionResult
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.readBytes

object AudienceTest : ModInitializer {
    private val byteArray = Path.of("resources.zip").readBytes()
    private val resourcePack = ResourcePack(byteArray)
    private val properties: ServerResourcePackProperties

    private val otherByteArray = Path.of("resources2.zip").readBytes()
    private val otherResourcePack = ResourcePack(otherByteArray)
    private val otherProperties: ServerResourcePackProperties

    private val resourcePackServer = ResourcePackServer("localhost", 25566).apply {
        properties = registerResourcePack(resourcePack)
        otherProperties = registerResourcePack(otherResourcePack)
    }

    override fun onInitialize() {
        LoggerFactory.getLogger("Audience Test").info("Initializing")
        resourcePackServer.startServer()
        println("Started server on port ${resourcePackServer.port}")

        UseItemCallback.EVENT.register { player, _, hand ->
            player as Audience
            val stack = player.getStackInHand(hand)
            if (stack.isOf(Items.STICK)) {
                player.setResourcePack(properties) { status ->
                    println("MCC: $status")
                }
            } else if (stack.isOf(Items.AMETHYST_SHARD)) {
                player.setResourcePack(otherProperties) { status ->
                    println("Brawls: $status")
                }
            }
            println(stack)
            TypedActionResult.pass(stack)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            resourcePackServer.stopServer()
        }
    }
}
