package net.hydrakecat.yahtzee

import com.google.common.truth.Truth.assertThat
import java.util.*
import kotlin.test.Test


class OptimizedStrategyTest {
    private val strategy = OptimizedStrategy()

    @Test
    fun score_threeOfAKind_12224_returns11() {
        assertThat(Category.THREE_OF_A_KIND.score(intArrayOf(1, 3, 0, 1, 0, 0))).isEqualTo(11)
    }

    @Test
    fun score_threeOfAKind_12256_returns0() {
        assertThat(Category.THREE_OF_A_KIND.score(intArrayOf(1, 2, 0, 0, 1, 1))).isEqualTo(0)
    }

    @Test
    fun score_fourOfAKind_55556_returns26() {
        assertThat(Category.FOUR_OF_A_KIND.score(intArrayOf(0, 0, 0, 0, 4, 1))).isEqualTo(26)
    }

    @Test
    fun score_fourOfAKind_44466_returns0() {
        assertThat(Category.FOUR_OF_A_KIND.score(intArrayOf(0, 0, 0, 3, 0, 2))).isEqualTo(0)
    }

    @Test
    fun score_fullHouse_44466_returns25() {
        assertThat(Category.FULL_HOUSE.score(intArrayOf(0, 0, 0, 3, 0, 2))).isEqualTo(25)
    }

    @Test
    fun score_fullHouse_11366_returns0() {
        assertThat(Category.FULL_HOUSE.score(intArrayOf(2, 0, 1, 0, 0, 2))).isEqualTo(0)
    }

    @Test
    fun score_smallStraight_23345_returns30() {
        assertThat(Category.SMALL_STRAIGHT.score(intArrayOf(0, 1, 2, 1, 1, 0))).isEqualTo(30)
    }

    @Test
    fun score_smallStraight_12356_returns0() {
        assertThat(Category.SMALL_STRAIGHT.score(intArrayOf(1, 1, 1, 0, 1, 1))).isEqualTo(0)
    }

    @Test
    fun score_largeStraight_23456_returns40() {
        assertThat(Category.LARGE_STRAIGHT.score(intArrayOf(0, 1, 1, 1, 1, 1))).isEqualTo(40)
    }

    @Test
    fun score_largeStraight_12344_returns0() {
        assertThat(Category.LARGE_STRAIGHT.score(intArrayOf(1, 1, 1, 2, 0, 0))).isEqualTo(0)
    }

    @Test
    fun score_yacht_22222_returns50() {
        assertThat(Category.YACHT.score(intArrayOf(0, 5, 0, 0, 0, 0))).isEqualTo(50)
    }

    @Test
    fun score_yacht_12222_returns0() {
        assertThat(Category.YACHT.score(intArrayOf(1, 4, 0, 0, 0, 0))).isEqualTo(0)
    }

    @Test
    fun chooseKeep_different_5dices() {
        val keeps = strategy.chooseKeep(intArrayOf(1, 1, 1, 1, 0, 1)).map { it.wrap() }
        assertThat(keeps)
            .containsExactlyElementsIn(
                listOf(
                    wrapIntArrayOf(0, 0, 0, 0, 0, 0),
                    wrapIntArrayOf(1, 0, 0, 0, 0, 0),
                    wrapIntArrayOf(0, 1, 0, 0, 0, 0),
                    wrapIntArrayOf(0, 0, 1, 0, 0, 0),
                    wrapIntArrayOf(0, 0, 0, 1, 0, 0),
                    wrapIntArrayOf(0, 0, 0, 0, 0, 1),
                    wrapIntArrayOf(1, 1, 0, 0, 0, 0),
                    wrapIntArrayOf(1, 0, 1, 0, 0, 0),
                    wrapIntArrayOf(1, 0, 0, 1, 0, 0),
                    wrapIntArrayOf(1, 0, 0, 0, 0, 1),
                    wrapIntArrayOf(0, 1, 1, 0, 0, 0),
                    wrapIntArrayOf(0, 1, 0, 1, 0, 0),
                    wrapIntArrayOf(0, 1, 0, 0, 0, 1),
                    wrapIntArrayOf(0, 0, 1, 1, 0, 0),
                    wrapIntArrayOf(0, 0, 1, 0, 0, 1),
                    wrapIntArrayOf(0, 0, 0, 1, 0, 1),
                    wrapIntArrayOf(1, 1, 1, 0, 0, 0),
                    wrapIntArrayOf(1, 1, 0, 1, 0, 0),
                    wrapIntArrayOf(1, 1, 0, 0, 0, 1),
                    wrapIntArrayOf(1, 0, 1, 1, 0, 0),
                    wrapIntArrayOf(1, 0, 1, 0, 0, 1),
                    wrapIntArrayOf(1, 0, 0, 1, 0, 1),
                    wrapIntArrayOf(0, 1, 1, 1, 0, 0),
                    wrapIntArrayOf(0, 1, 1, 0, 0, 1),
                    wrapIntArrayOf(0, 1, 0, 1, 0, 1),
                    wrapIntArrayOf(0, 0, 1, 1, 0, 1),
                    wrapIntArrayOf(1, 1, 1, 1, 0, 0),
                    wrapIntArrayOf(1, 1, 1, 0, 0, 1),
                    wrapIntArrayOf(1, 1, 0, 1, 0, 1),
                    wrapIntArrayOf(1, 0, 1, 1, 0, 1),
                    wrapIntArrayOf(0, 1, 1, 1, 0, 1),
                    wrapIntArrayOf(1, 1, 1, 1, 0, 1),
                )
            )
    }

    @Test
    fun chooseKeep_1duplicates_5dices() {
        val keeps = strategy.chooseKeep(intArrayOf(3, 1, 1, 0, 0, 0)).map { it.wrap() }
        assertThat(keeps)
            .containsExactlyElementsIn(
                listOf(
                    wrapIntArrayOf(0, 0, 0, 0, 0, 0),
                    wrapIntArrayOf(1, 0, 0, 0, 0, 0),
                    wrapIntArrayOf(0, 1, 0, 0, 0, 0),
                    wrapIntArrayOf(0, 0, 1, 0, 0, 0),
                    wrapIntArrayOf(2, 0, 0, 0, 0, 0),
                    wrapIntArrayOf(1, 1, 0, 0, 0, 0),
                    wrapIntArrayOf(1, 0, 1, 0, 0, 0),
                    wrapIntArrayOf(0, 1, 1, 0, 0, 0),
                    wrapIntArrayOf(3, 0, 0, 0, 0, 0),
                    wrapIntArrayOf(2, 1, 0, 0, 0, 0),
                    wrapIntArrayOf(2, 0, 1, 0, 0, 0),
                    wrapIntArrayOf(1, 1, 1, 0, 0, 0),
                    wrapIntArrayOf(3, 1, 0, 0, 0, 0),
                    wrapIntArrayOf(3, 0, 1, 0, 0, 0),
                    wrapIntArrayOf(2, 1, 1, 0, 0, 0),
                    wrapIntArrayOf(3, 1, 1, 0, 0, 0),
                )
            )
    }

    @Test
    fun rolledDiceDist_2dices() {
        val rollsDist: List<Roll> = strategy.rolledDiceDist(2)
        assertThat(rollsDist)
            .containsExactlyElementsIn(
                listOf(
                    Roll(intArrayOf(2, 0, 0, 0, 0, 0), 1 / 36.0),
                    Roll(intArrayOf(0, 2, 0, 0, 0, 0), 1 / 36.0),
                    Roll(intArrayOf(0, 0, 2, 0, 0, 0), 1 / 36.0),
                    Roll(intArrayOf(0, 0, 0, 2, 0, 0), 1 / 36.0),
                    Roll(intArrayOf(0, 0, 0, 0, 2, 0), 1 / 36.0),
                    Roll(intArrayOf(0, 0, 0, 0, 0, 2), 1 / 36.0),
                    Roll(intArrayOf(1, 1, 0, 0, 0, 0), 1 / 18.0),
                    Roll(intArrayOf(1, 0, 1, 0, 0, 0), 1 / 18.0),
                    Roll(intArrayOf(1, 0, 0, 1, 0, 0), 1 / 18.0),
                    Roll(intArrayOf(1, 0, 0, 0, 1, 0), 1 / 18.0),
                    Roll(intArrayOf(1, 0, 0, 0, 0, 1), 1 / 18.0),
                    Roll(intArrayOf(0, 1, 1, 0, 0, 0), 1 / 18.0),
                    Roll(intArrayOf(0, 1, 0, 1, 0, 0), 1 / 18.0),
                    Roll(intArrayOf(0, 1, 0, 0, 1, 0), 1 / 18.0),
                    Roll(intArrayOf(0, 1, 0, 0, 0, 1), 1 / 18.0),
                    Roll(intArrayOf(0, 0, 1, 1, 0, 0), 1 / 18.0),
                    Roll(intArrayOf(0, 0, 1, 0, 1, 0), 1 / 18.0),
                    Roll(intArrayOf(0, 0, 1, 0, 0, 1), 1 / 18.0),
                    Roll(intArrayOf(0, 0, 0, 1, 1, 0), 1 / 18.0),
                    Roll(intArrayOf(0, 0, 0, 1, 0, 1), 1 / 18.0),
                    Roll(intArrayOf(0, 0, 0, 0, 1, 1), 1 / 18.0)
                )
            )
    }

    @Test
    fun computeExpectedScore_onlyYachtAvailable() {
        val actual = strategy.computeExpectedScore(EnumSet.of(Category.YACHT), 0)

        // The probability of Yacht is 4.60% and the estimated score equals to 50 * 4.60%
        assertThat(actual).isEqualTo(2.3014321262849475)
    }

    @Test
    fun computeExpectedScore_onlyLowerSectionsAvailable() {
        val actual = strategy.computeExpectedScore(
            EnumSet.range(Category.THREE_OF_A_KIND, Category.CHANCE),
            0
        )
        assertThat(actual).isEqualTo(139.97129244237908)
    }

    @Test
    fun computeExpectedScore_onlyUpperSectionsAvailable() {
        val actual =
            strategy.computeExpectedScore(EnumSet.range(Category.ACES, Category.SIXES), 0)
        assertThat(actual).isEqualTo(71.95152973371323)
    }

//    @Test
//    fun computeExpectedScore_allSectionsAvailable() {
//        val actual =
//            strategy.computeExpectedScore(EnumSet.allOf(Category::class.java), 0)
//        assertThat(actual).isEqualTo(245.87)
//    }
}

private fun wrapIntArrayOf(vararg elements: Int) = intArrayOf(*elements).wrap()
private fun IntArray.wrap() = IntArrayWrapper(this)

// Wrapper class just for the test comparison and human-friendly test failure messages
private class IntArrayWrapper(private val array: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntArrayWrapper) return false

        if (!array.contentEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int {
        return array.contentHashCode()
    }

    override fun toString(): String {
        return array.contentToString()
    }
}