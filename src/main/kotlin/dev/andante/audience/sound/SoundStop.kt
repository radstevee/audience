package dev.andante.audience.sound

import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier

/**
 * Represents a sound to be stopped on the client.
 */
data class SoundStop(
    /**
     * The id of the sound to stop.
     */
    val id: Identifier?,

    /**
     * The category of the sound to stop.
     */
    val category: SoundCategory? = null
) {
    /**
     * The sound packet to stop this sound.
     */
    val packet: StopSoundS2CPacket = StopSoundS2CPacket(id, category)
}
