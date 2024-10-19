package dev.andante.audience.player

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.Uuids
import java.util.UUID
import java.util.function.Function

/**
 * An instantiatiable, unattached player reference.
 */
class StandalonePlayerReference(private val referenceImplUuid: UUID) : PlayerReference {
    override fun getHardReference(): StandalonePlayerReference {
        // return this, as all standlone references are hard references
        return this
    }

    override fun getReferenceUuid(): UUID {
        return referenceImplUuid
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is PlayerReference) {
            return false
        }

        return other.referenceUuid == referenceUuid
    }

    override fun hashCode(): Int {
        return referenceUuid.hashCode()
    }

    override fun toString(): String {
        return "StandalonePlayerReference[$referenceUuid]"
    }

    companion object {
        /**
         * The legacy codec for this class. Places the data within an unnecessary compound.
         */
        private val LEGACY_CODEC: Codec<StandalonePlayerReference> = RecordCodecBuilder.create { instance ->
            instance.group(
                Uuids.CODEC.fieldOf("uuid").forGetter(PlayerReference::getReferenceUuid)
            ).apply(instance, ::StandalonePlayerReference)
        }

        /**
         * The modern codec which maps the UUID codec to the standalone player reference.
         */
        private val XMAP_CODEC: Codec<StandalonePlayerReference> = Uuids.CODEC.xmap(
            ::StandalonePlayerReference,
            StandalonePlayerReference::getReferenceUuid
        )

        /**
         * The codec for this class.
         */
        val CODEC: Codec<StandalonePlayerReference> = Codec.either(XMAP_CODEC, LEGACY_CODEC).xmap(
            { it.map(Function.identity(), Function.identity()) },
            { Either.left(it) }
        )

        /**
         * This game profile as a player reference.
         */
        val GameProfile.playerReference get() = this as PlayerReference
    }
}
