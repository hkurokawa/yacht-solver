package net.hydrakecat.yacht

import java.util.*

/**
 * Calculates the gained score when choosing [Category] for the given dice distribution.
 * It also returns whether the Upper Section Bonus is taken.
 **/
object ScoreCalculator {
    fun calculateScore(
        category: Category,
        dist: IntArray,
        upperSectionTotalScore: Int
    ): ScoreResult {
        check(
            dist.size == M && dist.sum() == N
        ) { "The distribution is not as expected: ${dist.contentToString()}" }

        val score = calculateScore(category, dist)
        var newUpperSectionTotalScore = upperSectionTotalScore
        var bonus = 0
        if (UPPER_SECTION_CATEGORIES.contains(category)) {
            newUpperSectionTotalScore += score
            // If the previous total upper section score is below the boundary and the new total
            // upper section score is equal to or above the boundary, we get the bonus
            if (upperSectionTotalScore < UPPER_BONUS_MIN && UPPER_BONUS_MIN <= newUpperSectionTotalScore) {
                bonus = UPPER_BONUS
            }
        }
        return ScoreResult(score, bonus, newUpperSectionTotalScore)
    }

    private fun calculateScore(category: Category, dist: IntArray): Int {
        return when (category) {
            Category.ACES -> dist[0] * 1
            Category.TWOS -> dist[1] * 2
            Category.THREES -> dist[2] * 3
            Category.FOURS -> dist[3] * 4
            Category.FIVES -> dist[4] * 5
            Category.SIXES -> dist[5] * 6

            Category.FOUR_OF_A_KIND -> {
                if (dist.any { it >= 4 }) {
                    dist.computeSumFaces()
                } else 0
            }

            Category.FULL_HOUSE -> {
                if (dist.count { it >= 2 } == 2 && dist.count { it == 0 } == M - 2) {
                    dist.computeSumFaces()
                } else 0
            }

            Category.SMALL_STRAIGHT -> {
                for (i in 0..2) if (dist.slice(i..i + 3).all { it > 0 }) return 15
                0
            }

            Category.LARGE_STRAIGHT -> {
                for (i in 0..1) {
                    if (dist.slice(i..i + 4).all { it > 0 }) return 30
                }
                0
            }

            Category.YACHT -> {
                if (dist.any { it >= N }) 50 else 0
            }

            Category.CHANCE -> {
                dist.computeSumFaces()
            }
        }
    }
}

data class ScoreResult(val score: Int, val bonus: Int, val upperSectionTotalScore: Int)

private fun IntArray.computeSumFaces(): Int {
    check(this.size == M && this.all { it in 0..N }) { throw IllegalStateException("Invalid dice: ${this.contentToString()}") }
    return this.mapIndexed { index, i -> (index + 1) * i }.sum()
}

private val UPPER_SECTION_CATEGORIES = EnumSet.range(Category.ACES, Category.SIXES)
