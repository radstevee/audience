package dev.andante.audience.sound

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

/**
 * Represents an in-game sound that can be played to the client.
 */
data class Sound(
    /**
     * The id of the sound.
     */
    override val id: Identifier,

    /**
     * The category of the sound. Defaults to the sound effects category.
     */
    val category: SoundCategory = SoundCategory.VOICE,

    /**
     * The volume of the sound.
     */
    val volume: Float = 1.0f,

    /**
     * The pitch of the sound.
     */
    val pitch: Float = 1.0f
) : ISound {
    /**
     * The registry entry of this sound.
     */
    private val entry = RegistryEntry.of(SoundEvent.of(id))

    /**
     * The sound packet to play this sound.
     */
    override val packet: PlaySoundS2CPacket = packet()

    /**
     * Creates a packet from the given [pos], defaulting to [Vec3d.ZERO].
     */
    override fun packet(pos: Vec3d): PlaySoundS2CPacket {
        return PlaySoundS2CPacket(entry, category, pos.x, pos.y, pos.z, volume, pitch, 0L)
    }

    /**
     * Returns this sound on the dedicated music slider.
     */
    fun asMusic(): Sound {
        return category(SoundCategory.RECORDS)
    }

    /**
     * Modifies the category of this sound.
     * @return a new sound
     */
    fun category(category: SoundCategory): Sound {
        return copy(category = category)
    }

    /**
     * Modifies the volume of this sound.
     * @return a new sound
     */
    fun volume(volume: Float): Sound {
        return copy(volume = volume)
    }

    /**
     * Modifies the pitch of this sound.
     * @return a new sound
     */
    fun pitch(pitch: Float): Sound {
        return copy(pitch = pitch)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        if (other !is Sound) {
            return false
        }

        return other.id == id && other.category == category && other.volume == volume && other.pitch == pitch
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + pitch.hashCode()
        return result
    }
}
