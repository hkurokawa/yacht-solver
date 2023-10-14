package net.hydrakecat.yacht

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test


class OptimizedStrategyTest {
    private val strategy = OptimizedStrategy()

    @Test
    fun score_fourOfAKind_55556_returns26() {
        assertThat(Category.FOUR_OF_A_KIND.scoreDist(intArrayOf(0, 0, 0, 0, 4, 1))).isEqualTo(26)
    }

    @Test
    fun score_fourOfAKind_44466_returns0() {
        assertThat(Category.FOUR_OF_A_KIND.scoreDist(intArrayOf(0, 0, 0, 3, 0, 2))).isEqualTo(0)
    }

    @Test
    fun score_fullHouse_44466_returns24() {
        assertThat(Category.FULL_HOUSE.scoreDist(intArrayOf(0, 0, 0, 3, 0, 2))).isEqualTo(24)
    }

    @Test
    fun score_fullHouse_11366_returns0() {
        assertThat(Category.FULL_HOUSE.scoreDist(intArrayOf(2, 0, 1, 0, 0, 2))).isEqualTo(0)
    }

    @Test
    fun score_smallStraight_23345_returns15() {
        assertThat(Category.SMALL_STRAIGHT.scoreDist(intArrayOf(0, 1, 2, 1, 1, 0))).isEqualTo(15)
    }

    @Test
    fun score_smallStraight_12356_returns0() {
        assertThat(Category.SMALL_STRAIGHT.scoreDist(intArrayOf(1, 1, 1, 0, 1, 1))).isEqualTo(0)
    }

    @Test
    fun score_largeStraight_23456_returns30() {
        assertThat(Category.LARGE_STRAIGHT.scoreDist(intArrayOf(0, 1, 1, 1, 1, 1))).isEqualTo(30)
    }

    @Test
    fun score_largeStraight_12344_returns0() {
        assertThat(Category.LARGE_STRAIGHT.scoreDist(intArrayOf(1, 1, 1, 2, 0, 0))).isEqualTo(0)
    }

    @Test
    fun score_yacht_22222_returns50() {
        assertThat(Category.YACHT.scoreDist(intArrayOf(0, 5, 0, 0, 0, 0))).isEqualTo(50)
    }

    @Test
    fun score_yacht_12222_returns0() {
        assertThat(Category.YACHT.scoreDist(intArrayOf(1, 4, 0, 0, 0, 0))).isEqualTo(0)
    }

    @Test
    fun computeExpectedScore_onlyAcesAvailable() {
        val actual = strategy.computeExpectedScore(1)
        assertThat(actual).isEqualTo(2.1064814814814756)
    }

    @Test
    fun computeExpectedScore_onlyAcesTwosAvailable() {
        val actual = strategy.computeExpectedScore(2)
        assertThat(actual).isEqualTo(7.122428516419336)
    }

    @Test
    fun computeExpectedScore_onlyAcesTwosThreesAvailable() {
        val actual = strategy.computeExpectedScore(3)
        assertThat(actual).isEqualTo(15.215186473691855)
    }

    @Test
    fun computeExpectedScore_onlyUpperSectionsAvailable() {
        val actual = strategy.computeExpectedScore(6)
        assertThat(actual).isEqualTo(71.95152973371323)
    }

//    @Test
//    fun computeExpectedScore_onlyYachtAvailable() {
//        val actual = strategy.computeExpectedScore(EnumSet.of(Category.YACHT), 0)
//
//        // The probability of Yacht is 4.60% and the estimated score equals to 50 * 4.60%
//        assertThat(actual).isEqualTo(2.3014321262849475)
//    }
//    @Test
//    fun computeExpectedScore_onlyLowerSectionsAvailable() {
//        val actual = strategy.computeExpectedScore(
//            EnumSet.range(Category.THREE_OF_A_KIND, Category.CHANCE),
//            0
//        )
//        assertThat(actual).isEqualTo(139.97129244237908)
//    }

    @Test
    fun computeExpectedScore_allSectionsAvailable() {
        val actual = strategy.computeExpectedScore(Category.entries.size)
        assertThat(actual).isEqualTo(191.76087975216507)
    }
}
