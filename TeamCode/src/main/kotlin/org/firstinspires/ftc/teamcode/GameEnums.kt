package org.firstinspires.ftc.teamcode

/**
 * The locations of our team's spike marks.
 */
enum class SpikeMark {
    LEFT,
    CENTER,
    RIGHT,
}

/**
 * A team alliance.
 */
enum class Alliance {
    RED,
    BLUE
}

/**
 * The absolute side of the alliance.
 */
enum class AllianceSide(@JvmField val pos: Int) {
    AUDIENCE_SIDE(2),
    BACKDROP_SIDE(4)
}