package dev.andante.audience.title

/**
 * A set of times for a fade, in ticks.
 */
data class TickTimes(
    val fadeIn: Int,
    val stay: Int,
    val fadeOut: Int
) {
    val duration: Int = fadeIn + stay + fadeOut
}
