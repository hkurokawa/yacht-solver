package net.hydrakecat.yacht

import com.google.common.truth.Truth.assertThat
import net.hydrakecat.yacht.DiceDistCalculator.rolledDiceDist
import org.junit.jupiter.api.Test

class DiceDistCalculatorTest {

    @Test
    fun rolledDiceDist_2dice() {
        val rollsDist: List<DistWithProbability> = rolledDiceDist(2)
        assertThat(rollsDist)
            .containsExactlyElementsIn(
                listOf(
                    DistWithProbability(intArrayOf(2, 0, 0, 0, 0, 0), 1 / 36.0),
                    DistWithProbability(intArrayOf(0, 2, 0, 0, 0, 0), 1 / 36.0),
                    DistWithProbability(intArrayOf(0, 0, 2, 0, 0, 0), 1 / 36.0),
                    DistWithProbability(intArrayOf(0, 0, 0, 2, 0, 0), 1 / 36.0),
                    DistWithProbability(intArrayOf(0, 0, 0, 0, 2, 0), 1 / 36.0),
                    DistWithProbability(intArrayOf(0, 0, 0, 0, 0, 2), 1 / 36.0),
                    DistWithProbability(intArrayOf(1, 1, 0, 0, 0, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(1, 0, 1, 0, 0, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(1, 0, 0, 1, 0, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(1, 0, 0, 0, 1, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(1, 0, 0, 0, 0, 1), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 1, 1, 0, 0, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 1, 0, 1, 0, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 1, 0, 0, 1, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 1, 0, 0, 0, 1), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 0, 1, 1, 0, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 0, 1, 0, 1, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 0, 1, 0, 0, 1), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 0, 0, 1, 1, 0), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 0, 0, 1, 0, 1), 1 / 18.0),
                    DistWithProbability(intArrayOf(0, 0, 0, 0, 1, 1), 1 / 18.0)
                )
            )
    }
}