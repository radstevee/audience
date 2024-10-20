package dev.andante.audience.test

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.andante.audience.Audience
import dev.andante.audience.player.PlayerReference
import dev.andante.audience.player.PlayerSet
import dev.andante.audience.player.StandalonePlayerReference
import dev.andante.audience.resource.ByteResourcePack
import dev.andante.audience.resource.ResourcePackHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.SendResourcePackTask
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.math.sin
import kotlin.time.measureTimedValue

object AudienceTest : ModInitializer {
    private val logger = LoggerFactory.getLogger("Audience Test")

    val resourcePackBytes = Path("resources.zip").readBytes()
    val resourcePackBytesTwo = Path("resources2.zip").readBytes()

    override fun onInitialize() {
        logger.info("Initializing")

        ServerTickEvents.END_SERVER_TICK.register { server ->
            val playerManager = server.playerManager
            playerManager.playerList.forEach { player ->
                val audience = player as Audience
                val value = 1.0f - sin(player.age / 10.0f)
                audience.setTintPercentage(value)
            }
        }

        // test player reference codec IO (ensure possible migration of old uuids)
        val input = "{\"uuid\":\"50c7e7c5-2407-4102-875f-e4b1f49ea61a\"}"
        val inputJson = JsonParser.parseString(input)
        val inputReferenceResult = StandalonePlayerReference.CODEC.decode(JsonOps.INSTANCE, inputJson)
        val inputReference = inputReferenceResult.resultOrPartial(logger::error)
            .orElseThrow()
            .first

        println(inputReference)

        val output = StandalonePlayerReference.CODEC.encodeStart(JsonOps.INSTANCE, inputReference)
        val outputJson = output.resultOrPartial(logger::error).orElseThrow()
        println(outputJson)

        val list = PlayerSet(listOf(inputReference))
        val listJson = PlayerSet.CODEC.encodeStart(JsonOps.INSTANCE, list).resultOrPartial(logger::error).orElseThrow()
        println(listJson)

        val measuredApi = measureTimedValue(inputReference::getMojangApiPlayerName)
        println("Took ${measuredApi.duration.inWholeMilliseconds} ms to fetch \"${measuredApi.value}\"")

        val packOne = ByteResourcePack(resourcePackBytes)
        val packTwo = ByteResourcePack(resourcePackBytesTwo)
        ResourcePackHandler.add(packOne)
        ResourcePackHandler.add(packTwo)
        println(packOne.hash)
        println(packTwo.hash)

        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register { handler, server ->
            val reference = handler.debugProfile as PlayerReference
            println(reference.hardReference)
        }

        ServerConfigurationConnectionEvents.CONFIGURE.register { handler, server ->
            handler.addTask(SendResourcePackTask(MinecraftServer.ServerResourcePackProperties(UUID.randomUUID(), "http://localhost:25565/${packOne.hash}", packOne.hash, true, Text.literal("EEEE"))))
        }
    }
}
