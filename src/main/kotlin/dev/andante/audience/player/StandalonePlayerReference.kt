package dev.andante.audience.player

import java.util.UUID

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
        return "$referenceUuid[$playerName]"
    }
}
