package net.hydrakecat.yacht

import com.google.common.truth.Truth.assertThat
import net.hydrakecat.yacht.ScoreCalculator.calculateScore
import org.junit.jupiter.api.Test

class ScoreCalculatorTest {
    @Test
    fun calculateScore_fourOfAKind_55556_returns26() {
        assertThat(Category.FOUR_OF_A_KIND.score(intArrayOf(0, 0, 0, 0, 4, 1))).isEqualTo(26)
    }

    @Test
    fun calculateScore_fourOfAKind_44466_returns0() {
        assertThat(Category.FOUR_OF_A_KIND.score(intArrayOf(0, 0, 0, 3, 0, 2))).isEqualTo(0)
    }

    @Test
    fun calculateScore_fullHouse_44466_returns24() {
        assertThat(Category.FULL_HOUSE.score(intArrayOf(0, 0, 0, 3, 0, 2))).isEqualTo(24)
    }

    @Test
    fun calculateScore_fullHouse_11366_returns0() {
        assertThat(Category.FULL_HOUSE.score(intArrayOf(2, 0, 1, 0, 0, 2))).isEqualTo(0)
    }

    @Test
    fun calculateScore_smallStraight_23345_returns15() {
        assertThat(Category.SMALL_STRAIGHT.score(intArrayOf(0, 1, 2, 1, 1, 0))).isEqualTo(15)
    }

    @Test
    fun calculateScore_smallStraight_12356_returns0() {
        assertThat(Category.SMALL_STRAIGHT.score(intArrayOf(1, 1, 1, 0, 1, 1))).isEqualTo(0)
    }

    @Test
    fun calculateScore_largeStraight_23456_returns30() {
        assertThat(Category.LARGE_STRAIGHT.score(intArrayOf(0, 1, 1, 1, 1, 1))).isEqualTo(30)
    }

    @Test
    fun calculateScore_largeStraight_12344_returns0() {
        assertThat(Category.LARGE_STRAIGHT.score(intArrayOf(1, 1, 1, 2, 0, 0))).isEqualTo(0)
    }

    @Test
    fun calculateScore_yacht_22222_returns50() {
        assertThat(Category.YACHT.score(intArrayOf(0, 5, 0, 0, 0, 0))).isEqualTo(50)
    }

    @Test
    fun calculateScore_yacht_12222_returns0() {
        assertThat(Category.YACHT.score(intArrayOf(1, 4, 0, 0, 0, 0))).isEqualTo(0)
    }

    @Test
    fun calculateScore_aces_11111_totalUpperSectionScore60_returns5andBonus() {
        assertThat(calculateScore(Category.ACES, intArrayOf(5, 0, 0, 0, 0, 0), 60)).isEqualTo(
            ScoreResult(5, 35, 65)
        )
    }

    @Test
    fun calculateScore_aces_11123_totalUpperSectionScore60_returns3andBonus() {
        assertThat(calculateScore(Category.ACES, intArrayOf(3, 1, 1, 0, 0, 0), 60)).isEqualTo(
            ScoreResult(3, 35, 63)
        )
    }

    @Test
    fun calculateScore_aces_11123_totalUpperSectionScore59_returns3() {
        assertThat(calculateScore(Category.ACES, intArrayOf(3, 1, 1, 0, 0, 0), 59)).isEqualTo(
            ScoreResult(3, 0, 62)
        )
    }

    @Test
    fun calculateScore_aces_11123_totalUpperSectionScore63_returns3() {
        assertThat(calculateScore(Category.ACES, intArrayOf(3, 1, 1, 0, 0, 0), 63)).isEqualTo(
            ScoreResult(3, 0, 66)
        )
    }

    private fun Category.score(dist: IntArray) = calculateScore(this, dist, 0).score
}