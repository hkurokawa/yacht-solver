package net.hydrakecat.yacht

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import java.util.*
import kotlin.test.Test


class OptimizedStrategyTest {
    private val strategy = OptimizedStrategy()

    @BeforeEach
    fun setup(): Unit = runBlocking {
        strategy.init()
        strategy.load("expected_scores.txt")
    }

    @Test
    fun computeExpectedScore_onlyAcesAvailable() {
        val actual = strategy.computeExpectedScore(EnumSet.of(Category.ACES))
        assertThat(actual).isEqualTo(2.1064814814814756)
    }

    @Test
    fun computeExpectedScore_onlyAcesTwosAvailable() {
        val actual = strategy.computeExpectedScore(EnumSet.of(Category.ACES, Category.TWOS))
        assertThat(actual).isEqualTo(7.122428516419336)
    }

    @Test
    fun computeExpectedScore_onlyAcesTwosThreesAvailable() {
        val actual = strategy.computeExpectedScore(EnumSet.range(Category.ACES, Category.THREES))
        assertThat(actual).isEqualTo(15.215186473691855)
    }

    @Test
    fun computeExpectedScore_onlyUpperSectionsAvailable() {
        val actual = strategy.computeExpectedScore(EnumSet.range(Category.ACES, Category.SIXES))
        assertThat(actual).isEqualTo(71.95152973371323)
    }

    @Test
    fun computeExpectedScore_onlyYachtAvailable() {
        val actual = strategy.computeExpectedScore(EnumSet.of(Category.YACHT))

        // The probability of Yacht is 4.60% and the estimated score equals to 50 * 4.60%
        assertThat(actual).isEqualTo(2.3014321262849475)
    }

    @Test
    fun computeExpectedScore_onlyLowerSectionsAvailable() {
        val actual = strategy.computeExpectedScore(
            EnumSet.range(Category.FOUR_OF_A_KIND, Category.CHANCE),
        )
        assertThat(actual).isEqualTo(88.63452704057464)
    }

    @Test
    @Disabled
    fun computeExpectedScore_allSectionsAvailable() {
        val actual = strategy.computeExpectedScore(Category.entries.toSet())
        assertThat(actual).isEqualTo(191.76087975216507)
    }

    @Test
    fun chooseBest_noRemainingRolls_ACEStoFOURSAvailable_44444_chooseFOURS() {
        val actual =
            strategy.chooseBest(
                n = 1,
                categories = EnumSet.range(Category.ACES, Category.FOURS),
                upperTotalScore = 0,
                numRemainRolls = 0,
                faces = intArrayOf(4, 4, 4, 4, 4)
            )
        // The expected score is 20 (when selecting FOURS) + total expected score when ACES - THREES are available
        assertThat(actual).containsExactly(Choice.Select(Category.FOURS, 35.21518647369186))
    }

    @Test
    fun chooseBest_2remainingRolls_onlyYACHTAvailable_12345() {
        val actual =
            strategy.chooseBest(
                n = 1,
                categories = EnumSet.of(Category.YACHT),
                upperTotalScore = 0,
                numRemainRolls = 2,
                faces = intArrayOf(1, 2, 3, 4, 5)
            )
        assertThat(actual).containsExactly(Choice.Keep(listOf(1), 0.6315729309556476))
    }

    @Test
    fun chooseBest_KeepAndSelectHasTheSameExpectedScore_shouldPreferSelect() {
        // This is from the actual game play.  FULL HOUSE and LARGE STRAIGHT were taken and the 2nd
        // roll was [3, 4, 5, 6, 6].  In this case, the strategy should choose SMALL STRAIGHT
        // immediately without thinking about the 3rd dice roll.
        val actual =
            strategy.chooseBest(
                n = 3,
                categories = EnumSet.complementOf(
                    EnumSet.of(
                        Category.FULL_HOUSE,
                        Category.LARGE_STRAIGHT
                    )
                ),
                upperTotalScore = 0,
                numRemainRolls = 1,
                faces = intArrayOf(3, 4, 5, 6, 6)
            )
        assertThat(actual).containsExactly(
            Choice.Select(Category.SMALL_STRAIGHT, 144.4186587339747),
            Choice.Keep(listOf(3, 4, 5, 6), 144.4186587339747),
            Choice.Keep(listOf(6, 6), 143.39057619595297),
        ).inOrder()
    }
}
