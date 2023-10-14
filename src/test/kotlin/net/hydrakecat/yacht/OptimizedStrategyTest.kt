package net.hydrakecat.yacht

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test


class OptimizedStrategyTest {
    private val strategy = OptimizedStrategy()

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
