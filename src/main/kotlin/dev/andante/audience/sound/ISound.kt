package dev.andante.audience.sound

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

/**
 * An interface used to reference sounds.
 */
interface ISound {
    /**
     * The id of the sound.
     */
    val id: Identifier

    /**
     * The sound packet to play this sound.
     */
    val packet: PlaySoundS2CPacket

    /**
     * Creates a packet from the given [pos], defaulting to [Vec3d.ZERO].
     */
    fun packet(pos: Vec3d = Vec3d.ZERO): PlaySoundS2CPacket
}
