package dev.andante.audience.title

import net.minecraft.network.packet.s2c.play.BundleS2CPacket
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.text.Text

/**
 * A title to be displayed on a player's screen.
 */
class Title (
    /**
     * The title to be displayed.
     */
    titleText: Text,

    /**
     * The subtitle to be displayed.
     */
    subtitleText: Text,

    /**
     * The fade timings.
     */
    times: TickTimes
) {
    val packets = Packets(
        TitleS2CPacket(titleText),
        SubtitleS2CPacket(subtitleText),
        TitleFadeS2CPacket(times.fadeIn, times.stay, times.fadeOut)
    )

    data class Packets(
        /**
         * A packet sent to set up the title text and display the packet.
         */
        val title: TitleS2CPacket,
        /**
         * A packet sent to set up the subtitle text.
         */
        val subtitle: SubtitleS2CPacket,

        /**
         * A packet sent to set up the fade times.
         */
        val times: TitleFadeS2CPacket

    ) {
        /**
         * A bundled packet of all title packets.
         */
        val bundled: BundleS2CPacket = BundleS2CPacket(listOf(times, subtitle, title))
    }
}
