package us.rugulo.matchstats.data

enum class MatchSegmentType(val value: Int) {
    FIRST_HALF(1),
    SECOND_HALF(2),
    ET_FIRST_HALF(3),
    ET_SECOND_HALF(4);

    companion object {
        fun fromInt(value: Int) = MatchSegmentType.entries.first {it.value == value}
    }
}