package dev.andante.audience.resource.server

import java.util.UUID

/**
 * Represents a request for the server resource pack.
 */
data class ResourcePackRequest(
    /**
     * The uuid of the requesting player.
     */
    val uuid: UUID,

    /**
     * The username of the requesting player.
     */
    val username: String,

    /**
     * The version of the requesting client.
     */
    val clientVersion: String,

    /**
     * The version id of the requesting client.
     */
    val clientVersionId: String,

    /**
     * The requesting client's pack format.
     */
    val packFormat: Int,
)
