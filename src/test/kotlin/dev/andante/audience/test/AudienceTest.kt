package dev.andante.audience.test

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.andante.audience.Audience
import dev.andante.audience.player.PlayerSet
import dev.andante.audience.player.StandalonePlayerReference
import dev.andante.audience.resource.ResourcePack
import dev.andante.audience.resource.ResourcePackServer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.math.sin
import kotlin.time.measureTimedValue

object AudienceTest : ModInitializer {
    private const val TEST_RESOURCE_PACK: Boolean = false

    private val logger = LoggerFactory.getLogger("Audience Test")

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

        if (TEST_RESOURCE_PACK) {
            val byteArray = try {
                Path.of("resources.zip").readBytes()
            } catch (exception: Exception) {
                logger.error("No resources.zip", exception)
                throw exception
            }

            val resourcePack = ResourcePack(byteArray)

            val otherByteArray = try {
                Path.of("resources2.zip").readBytes()
            } catch (exception: Exception) {
                logger.error("No resources2.zip", exception)
                throw exception
            }

            val otherResourcePack = ResourcePack(otherByteArray)

            val resourcePackServer = ResourcePackServer("localhost", 25566).apply {
                registerResourcePack(resourcePack)
                registerResourcePack(otherResourcePack)
            }

            resourcePackServer.startServer()
            println("Started server on port ${resourcePackServer.port}")

            ServerLifecycleEvents.SERVER_STOPPING.register {
                resourcePackServer.stopServer()
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
    }
}
