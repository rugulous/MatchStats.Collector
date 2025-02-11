package us.rugulo.matchstats.data

enum class StatType(val value: Int) {
    CROSS(1),
    SHOT(2),
    CORNER(3);

    companion object {
        fun fromInt(value: Int?) = StatType.entries.firstOrNull {it.value == value}
    }
}