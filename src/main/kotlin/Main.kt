import net.hydrakecat.yahtzee.OptimizedStrategy
import kotlin.system.measureTimeMillis

fun main() {
    val strategy = OptimizedStrategy()
    val elapsedTime = measureTimeMillis {
        println(strategy.computeExpectedScore(13, "expected_scores.txt"))
    }
    println("Elapsed time: ${elapsedTime / 1000} sec")
}