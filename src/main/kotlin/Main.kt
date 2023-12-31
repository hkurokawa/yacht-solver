import kotlinx.coroutines.runBlocking
import net.hydrakecat.yacht.*
import net.hydrakecat.yacht.ScoreCalculator.calculateScore
import java.util.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("Usage: java -jar <jar file> save|auto|manual <file name>")
    }
    val method = args.getOrNull(0) ?: "auto"
    val strategy = OptimizedStrategy()
    runBlocking {
        strategy.init()
        if (method == "save") {
            strategy.save(args.getOrNull(1) ?: DEFAULT_FILE_NAME)
            return@runBlocking
        }
        strategy.load(args.getOrNull(1) ?: DEFAULT_FILE_NAME)
    }
    val n = 3
    val board = Board()
    when(method) {
        "auto" -> auto(strategy, n, board)
        "manual" -> manual(strategy, n, board)
        "save" -> return
        else -> throw IllegalArgumentException("Unexpected method name: $method")
    }
    println()
    println("[Final Result]")
    println(board)
}

private fun auto(
    strategy: OptimizedStrategy,
    n: Int,
    board: Board
) {
    for (turn in 1..Category.entries.size) run loop@{
        println()
        println("[Turn $turn]")
        var faces = rollDice()
        repeat(R) { roll ->
            println("Roll ${roll + 1}: ${faces.contentToString()}")
            val choices = strategy.chooseBest(
                n,
                board.availableCategories(),
                board.sumUpperSectionScore(),
                R - 1 - roll,
                faces
            )
            println("Best choice: $choices")
            when (val choice = choices[0]) {
                is Choice.Keep -> {
                    println("Keep ${choice.dice}")
                    faces =
                        (choice.dice.toIntArray() + rollDice(faces.size - choice.dice.size).toList()).sortedArray()
                }

                is Choice.Select -> {
                    val (delta, bonus) = board.score(choice.category, faces)
                    println("${choice.category} is selected.  Scored $delta" + (if (bonus > 0) " + US Bonus! ($bonus)" else "") + ", Total: ${board.totalScore()} (${board.sumUpperSectionScore()})")
                    return@loop
                }
            }
        }
    }
}

private fun manual(
    strategy: OptimizedStrategy,
    n: Int,
    board: Board,
) {
    for (turn in 1..Category.entries.size) run loop@{
        println()
        println("[Turn $turn]")
        var faces = readFaces()
        for (roll in 1..R) {
            println("Roll ${roll}: ${faces.contentToString()}")
            val choices = strategy.chooseBest(
                n,
                board.availableCategories(),
                board.sumUpperSectionScore(),
                R - roll,
                faces
            )
            println("Best choice: $choices")
            if (roll == R) break
            if (readChoice()) break
            faces = readFaces()
        }
        val c = readCategory(board.availableCategories())
        val (delta, bonus) = board.score(c, faces)
        println("$c is selected.  Scored $delta" + (if (bonus > 0) " ($bonus)" else "") + ", Total: ${board.totalScore()} (${board.sumUpperSectionScore()})")
    }
}

fun readCategory(availableCategories: Set<Category>): Category {
    while (true) {
        println("Available Categories: $availableCategories")
        print("Input category: ")
        val l = readln()
        for (c in availableCategories) {
            if (c.name.lowercase() == l.lowercase()) {
                return c
            }
        }
        println("No such category.")
    }
}

private fun readChoice(): Boolean {
    while (true) {
        print("Input C: Choose category or K: Keep some and roll the dice: ")
        val l = readln()
        when (l) {
            "C" -> return true
            "K" -> return false
            else -> {
                println("Invalid input.")
            }
        }
    }
}

fun readFaces(): IntArray {
    var faces: List<Int>
    while (true) {
        print("Input the dice numbers: ")
        val l = readln().trim().replace(" ", "")
        if (l.length != N) {
            println("Invalid number of numbers.  Must be $N numbers")
            continue
        }
        faces = l.toCharArray().map { it - '0' }
        if (faces.any { it < 1 || it > M }) {
            println("Invalid number is contained. Must be between 1 and $M")
            continue
        }
        break
    }
    return faces.sorted().toIntArray()
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
        val (score, bonus, _) = calculateScore(category, faces.toDist(), sumUpperSectionScore())
        if (bonus > 0) {
            this.bonus = bonus
        }
        scores[category] = score
        return Pair(score, bonus)
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (c in EnumSet.range(Category.ACES, Category.SIXES)) {
            result.append(String.format("%15s %3d%n", c, scores[c]))
        }
        result.append(String.format("%15s %3d (%3d)%n", "US", bonus, sumUpperSectionScore()))
        for (c in EnumSet.complementOf(EnumSet.range(Category.ACES, Category.SIXES))) {
            result.append(String.format("%15s %3d%n", c, scores[c]))
        }
        result.append(String.format("\n%15s %3d%n", "Total Score", totalScore()))
        return result.toString()
    }
}

// From faces to dist
private fun IntArray.toDist(): IntArray {
    return IntArray(M) { num ->
        this.count { it == num + 1 }
    }
}

private const val DEFAULT_FILE_NAME = "expected_scores.txt"
