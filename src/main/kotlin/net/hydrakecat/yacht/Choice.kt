package net.hydrakecat.yacht

/** Represents a choice a [Strategy] recommends to take when playing Yacht. */
sealed interface Choice {
    /** Keeping the [dice] faces having the expected score [expect]. */
    data class Keep(val dice: List<Int>, val expect: Double) : Choice

    /** Selecting [category] having the expected score [expect]. */
    data class Select(val category: Category, val expect: Double) : Choice
}