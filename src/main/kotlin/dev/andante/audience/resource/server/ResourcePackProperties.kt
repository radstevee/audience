package dev.andante.audience.resource.server

import net.minecraft.text.Text

/**
 * The properties of a server resource pack request.
 */
data class ResourcePackProperties(
    /**
     * The url to retrieve the resource pack from.
     */
    val url: String,

    /**
     * The SHA-1 hash of the resource pack file.
     */
    val hash: String,

    /**
     * Whether the resource pack is required to be accepted.
     */
    val required: Boolean,

    /**
     * The prompt displayed when the resource pack is sent to the client.
     */
    val prompt: Text? = null
) {
    override fun hashCode(): Int {
        return hash.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other?.javaClass != javaClass) return false

        other as ResourcePackProperties
        return other.hash == hash
    }
}
