package net.hydrakecat.yacht

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.reader
import kotlin.io.path.writer

object OptimizedExpectedScoreReadWriter {
    private val backgroundDispatcher = Dispatchers.IO

    suspend fun save(fileName: String, expectedScore: (Set<Category>, Int) -> Double?) {
        val categories = Category.entries
        val path = Path.of(fileName)
        if (path.notExists()) {
            println("Saving to $fileName")
            withContext(backgroundDispatcher) {
                path.writer(
                    options = arrayOf(
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                    )
                ).use { writer ->
                    val printer = PrintWriter(writer)
                    printer.println("## N: $N, M: $M, R: $R")
                    printer.println("## Categories: $categories")
                    printer.println("## Total upper section score to reach bonus: $UPPER_BONUS_MIN")
                    for (c in 0..<(1 shl categories.size)) {
                        printer.print(
                            String.format(
                                "%${categories.size}s",
                                Integer.toBinaryString(c)
                            ).replace(" ", "0")
                        )
                        for (us in 0..UPPER_BONUS_MIN) {
                            val d = expectedScore(c.toCategories(), us)
                            if (d == null) {
                                printer.printf(" %21s", Double.NaN)
                            } else {
                                printer.printf(" %21.17f", d)
                            }
                        }
                        printer.println()
                    }
                }
            }
        }
    }

    suspend fun load(fileName: String, onExpectedScore: (Set<Category>, Int, Double) -> Unit) {
        val path = Path.of(fileName)
        if (path.exists()) {
            println("Loading from $fileName")
            withContext(backgroundDispatcher) {
                path.reader(options = arrayOf(StandardOpenOption.READ)).use { reader ->
                    val scanner = Scanner(reader)
                    var line: String
                    while (scanner.hasNextLine()) {
                        line = scanner.nextLine()
                        if (line.startsWith("#")) continue
                        val numbers = line.split(Regex(" +"))
                        val categories = Integer.parseInt(numbers[0], 2).toCategories()
                        numbers.drop(1).forEachIndexed { us, s ->
                            val d = s.toDouble()
                            if (d.isFinite()) {
                                onExpectedScore(categories, us, d)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Int.toCategories(): Set<Category> {
    var c = this
    val r = mutableSetOf<Category>()
    while (c != 0) {
        var lsb = c.takeLowestOneBit()
        c = c xor lsb
        var i = -1
        while (lsb > 0) {
            lsb = lsb shr 1
            i++
        }
        r.add(Category.entries[i])
    }
    return r
}
