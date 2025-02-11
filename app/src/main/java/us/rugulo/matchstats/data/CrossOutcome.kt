package us.rugulo.matchstats.data

enum class CrossOutcome(val value: Int) {
    SHOT(1),
    CONTROLLED(2),
    CLEARED(3),
    CORNER(4),
    OUT_OF_PLAY(5),
    OTHER_WING(6)
}