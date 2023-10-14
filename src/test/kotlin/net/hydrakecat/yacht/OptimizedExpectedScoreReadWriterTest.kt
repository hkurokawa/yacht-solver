package net.hydrakecat.yacht

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import net.hydrakecat.yacht.OptimizedExpectedScoreReadWriter.load
import net.hydrakecat.yacht.OptimizedExpectedScoreReadWriter.save
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString

class OptimizedExpectedScoreReadWriterTest {
    private val file = Path.of("test_optimized_expected_score")

    @AfterEach
    fun tearDown() {
        file.deleteIfExists()
    }

    @Test
    fun save_and_load() = runBlocking {
        val expected = Array(1 shl Category.entries.size) { DoubleArray(UPPER_BONUS_MIN + 1) }
        var d = 0.0
        repeat(1 shl Category.entries.size) { c ->
            repeat(UPPER_BONUS_MIN + 1) { us ->
                expected[c][us] = d
                d += 1.0
            }
        }
        save(file.pathString) { categories, us ->
            expected[categories.toInt()][us]
        }
        val actual = Array(1 shl Category.entries.size) { DoubleArray(UPPER_BONUS_MIN + 1) }
        load(file.pathString) { categories, us, score ->
            actual[categories.toInt()][us] = score
        }
        assertThat(actual).isEqualTo(expected)
    }


}

private fun Set<Category>.toInt(): Int {
    var ret = 0
    for (c in this) {
        ret = ret or (1 shl c.ordinal)
    }
    return ret
}
