import net.hydrakecat.yahtzee.*
import java.util.*

fun main() {
    val strategy = OptimizedStrategy()
    strategy.load("expected_scores.txt")

    val n = 3
    val board = Board()
    for (turn in 1..Category.entries.size) run loop@{
        println()
        println("[Turn $turn]")
        var faces = rollDice()
        repeat(3) { roll ->
            println("Roll ${roll + 1}: ${faces.contentToString()}")
            val choices = strategy.chooseBest(
                n,
                board.availableCategories(),
                board.sumUpperSectionScore(),
                2 - roll,
                faces
            )
            println("Best choice: $choices")
            when (val choice = choices[0]) {
                is Choice.Keep -> {
                    println("Keep ${choice.dices}")
                    faces =
                        (choice.dices.toIntArray() + rollDice(faces.size - choice.dices.size).toList()).sortedArray()
                }

                is Choice.Select -> {
                    val (delta, bonus) = board.score(choice.category, faces)
                    println("${choice.category} is selected.  Scored $delta" + (if (bonus > 0) " ($bonus)" else "") + ", Total: ${board.totalScore()} (${board.sumUpperSectionScore()})")
                    return@loop
                }
            }
        }
    }
    println()
    println("[Final Result]")
    println(board)
}

private val random = Random()

private fun rollDice(n: Int = 5, m: Int = 6): IntArray {
    return IntArray(n) {
        random.nextInt(1, m + 1)
    }.sortedArray()
}

private data class Board(
    val scores: MutableMap<Category, Int> = mutableMapOf(),
    var bonus: Int = 0
) {
    fun availableCategories(): Set<Category> = Category.entries.toSet() - scores.keys

    fun sumUpperSectionScore() =
        EnumSet.range(Category.ACES, Category.SIXES).sumOf { scores.getOrDefault(it, 0) }

    fun totalScore() = scores.values.sum() + bonus
    fun score(category: Category, faces: IntArray): Pair<Int, Int> {
        val delta = category.score(faces)
        scores[category] = delta
        var bonusEarned = false
        if (EnumSet.range(Category.ACES, Category.SIXES).contains(category)) {
            if (sumUpperSectionScore() < UPPER_BONUS_MIN && sumUpperSectionScore() + delta >= UPPER_BONUS_MIN) {
                bonus = UPPER_BONUS
                bonusEarned = true
            }
        }
        return Pair(delta, if (bonusEarned) bonus else 0)
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (c in EnumSet.range(Category.ACES, Category.SIXES)) {
            result.append(String.format("%15s %3d%n", c, scores[c]))
        }
        result.append(String.format("%15s %3d (%3d)%n", "US", bonus, sumUpperSectionScore()))
        for (c in EnumSet.range(Category.THREE_OF_A_KIND, Category.CHANCE)) {
            result.append(String.format("%15s %3d%n", c, scores[c]))
        }
        result.append(String.format("\n%15s %3d%n", "Total Score", totalScore()))
        return result.toString()
    }
}