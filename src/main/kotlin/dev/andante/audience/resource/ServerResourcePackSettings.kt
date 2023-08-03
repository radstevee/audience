package dev.andante.audience.resource

import net.minecraft.text.Text

/**
 * The settings of a server resource pack.
 */
data class ServerResourcePackSettings(
    /**
     * The url to the pack.
     */
    val url: String,

    /**
     * The hash of the pack data.
     */
    val hash: String,

    /**
     * Whether the client is forced to download the pack.
     */
    val required: Boolean = true,

    /**
     * The prompt for the forced pack.
     */
    val prompt: Text = Text.empty()
)
