package dev.andante.audience.test

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.andante.audience.player.PlayerList
import dev.andante.audience.player.StandalonePlayerReference
import dev.andante.audience.resource.ResourcePack
import dev.andante.audience.resource.ResourcePackServer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer.ServerResourcePackProperties
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.readBytes

object AudienceTest : ModInitializer {
    private val logger = LoggerFactory.getLogger("Audience Test")

    private val byteArray = try {
        Path.of("resources.zip").readBytes()
    } catch (exception: Exception) {
        logger.error("No resources.zip", exception)
        throw exception
    }

    private val resourcePack = ResourcePack(byteArray)
    private val properties: ServerResourcePackProperties

    private val otherByteArray = try {
        Path.of("resources2.zip").readBytes()
    } catch (exception: Exception) {
        logger.error("No resources2.zip", exception)
        throw exception
    }

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

        ServerLifecycleEvents.SERVER_STOPPING.register {
            resourcePackServer.stopServer()
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

        val list = PlayerList(listOf(inputReference))
        val listJson = PlayerList.CODEC.encodeStart(JsonOps.INSTANCE, list).resultOrPartial(logger::error).orElseThrow()
        println(listJson)
    }
}
