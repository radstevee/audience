package dev.andante.audience.mixinterface

import dev.andante.audience.mixin.ServerPlayerEntityMixin
import dev.andante.audience.resource.server.ResourcePackProperties
import dev.andante.audience.resource.server.ResourcePackRequestCallback

/**
 * Used to access variables from the mixin [ServerPlayerEntityMixin].
 * @see ServerPlayerEntityMixin
 */
interface AudiencePlayerAccessor {
    fun getLastResourcePack(): ResourcePackProperties?

    fun getResourcePackRequestCallback(): ResourcePackRequestCallback?
    fun clearResourcePackRequestCallback(): ResourcePackRequestCallback?
}
