package net.hydrakecat.yacht

import com.google.common.truth.Truth.assertThat
import net.hydrakecat.yacht.DiceDistSubsetEnumerator.listSubsets
import org.junit.jupiter.api.Test

class DiceDistSubsetEnumeratorTest {
    @Test
    fun listSubsets_different_5dice() {
        val keeps = listSubsets(intArrayOf(1, 1, 1, 1, 0, 1)).map { it.wrap() }
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
        val keeps = listSubsets(intArrayOf(3, 1, 1, 0, 0, 0)).map { it.wrap() }
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
}

private fun wrapIntArrayOf(vararg elements: Int) = intArrayOf(*elements).wrap()
private fun IntArray.wrap() = IntArrayWrapper(this)

// Wrapper class is necessary to make the test comparison easier and test failure messages human-friendly
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
