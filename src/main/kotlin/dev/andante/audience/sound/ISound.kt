package dev.andante.audience.sound

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random

/**
 * An interface used to reference sounds.
 */
interface ISound {
    /**
     * The id of the sound.
     */
    val id: Identifier

    /**
     * The category of the sound.
     */
    val category: SoundCategory get() = SoundCategory.VOICE

    /**
     * Retrieves a registry entry of this sound.
     */
    val entry: RegistryEntry<SoundEvent> get() = RegistryEntry.of(SoundEvent.of(id))

    /**
     * The volume of the sound.
     */
    val volume: Float

    /**
     * The pitch of the sound.
     */
    val pitch: Float

    /**
     * Creates a packet from the given parameters.
     */
    fun createPacket(pos: Vec3d = Vec3d.ZERO): PlaySoundS2CPacket {
        return PlaySoundS2CPacket(entry, category, pos.x, pos.y, pos.z, volume, pitch, random.nextLong())
    }

    companion object {
        private val random: Random = Random.create()
    }
}
