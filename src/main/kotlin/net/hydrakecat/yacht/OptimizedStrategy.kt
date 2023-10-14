package net.hydrakecat.yacht

import net.hydrakecat.yacht.DiceDistCalculator.rolledDiceDist
import net.hydrakecat.yacht.DiceDistSubsetEnumerator.listSubsets
import net.hydrakecat.yacht.ScoreCalculator.calculateScore
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.reader
import kotlin.io.path.writer
import kotlin.math.max


// Number of dice
const val N = 5

// Num faces of the dice
const val M = 6

// Num rolls in a turn
const val R = 3

// Upper section bonus criteria
const val UPPER_BONUS_MIN = 63

const val UPPER_BONUS = 35

class OptimizedStrategy {
    private val memoizedBetweenTurnsE = HashMap<BetweenTurnsState, Double>()

    // List of possible number distribution when rolling N dice
    // e.g., [[N, 0, 0, 0, 0, 0], [N - 1, 1, 0, 0, 0, 0], ..., [0, 0, 0, 0, 0, N]]
    private val dists: Array<IntArray>

    // Probability to see the ith distribution of numbers when rolling N dice
    private val prob: DoubleArray

    // Number of possible choices to keep when the ith distribution of N dice is given
    private val numChoices: IntArray

    // List of distributions of dice that is chosen when the ith distribution is given and the jth choice is selected
    // For example, if [N, 0, 0, 0, 0, 0] is given and the first choice is keeping only one 1, choice[0][0] = [1, 0, 0, 0, 0, 0]
    private val choice: Array<Array<IntArray>>

    // List of distributions of N dice that could be derived from the ith distribution when choosing the jth choice
    private val transDists: Array<Array<IntArray>>

    // Probability to transit from the ith distribution to the kth distribution when choosing the jth choice
    private val transDistProb: Array<Array<DoubleArray>>

    // Score when selecting the ith distribution, j upper section score level and the kth Category
    private val scores: Array<Array<IntArray>>

    // New upper section score level when selecting the ith distribution, j upper section score level and the kth Category
    private val usls: Array<Array<IntArray>>

    init {
        val numbersWithProbList = rolledDiceDist(N)
        val n = numbersWithProbList.size
        dists = Array(n) { IntArray(M) }
        prob = DoubleArray(n)
        numChoices = IntArray(n)
        var maxNumChoices = 0
        for (i in numbersWithProbList.indices) {
            dists[i] = numbersWithProbList[i].dist
            prob[i] = numbersWithProbList[i].probability
            numChoices[i] = listSubsets(dists[i]).size
            maxNumChoices = max(maxNumChoices, numChoices[i])
        }
        transDists = Array(n) { Array(maxNumChoices) { IntArray(0) } }
        transDistProb = Array(n) { Array(maxNumChoices) { DoubleArray(0) } }
        choice = Array(n) { Array(maxNumChoices) { IntArray(0) } }
        scores = Array(n) { Array(UPPER_BONUS_MIN + 1) { IntArray(Category.entries.size) } }
        usls = Array(n) { Array(UPPER_BONUS_MIN + 1) { IntArray(Category.entries.size) } }
        for (i in 0..<n) {
            listSubsets(dists[i]).forEachIndexed { j, kept ->
                choice[i][j] = kept
                val distList = mutableListOf<Int>()
                val probList = mutableListOf<Double>()
                // the jth choice is selected, and we keep the dist of kept
                for ((d, p) in rolledDiceDist(N - kept.sum())) {
                    val ns = merge(kept, d)
                    val k = dists.indexOfFirst {
                        it.contentEquals(ns)
                    }
                    if (k < 0) {
                        throw IllegalStateException("Dist ${ns.contentToString()} not found")
                    }
                    distList.add(k)
                    probList.add(p)
                }
                transDists[i][j] = distList.toIntArray()
                transDistProb[i][j] = probList.toDoubleArray()
            }
            repeat(UPPER_BONUS_MIN + 1) { j ->
                for (category in Category.entries) {
                    val k = category.ordinal
                    val (score, bonus, newUsl) = calculateScore(category, dists[i], j)
                    scores[i][j][k] = score + bonus
                    usls[i][j][k] = newUsl.coerceAtMost(UPPER_BONUS_MIN)
                }
            }
        }
    }

    private fun merge(array: IntArray, other: IntArray): IntArray {
        val r = array.copyOf()
        for (i in r.indices) {
            r[i] += other[i]
        }
        return r
    }

    // public for testing
    internal fun computeExpectedScore(
        availableCategories: Set<Category>,
        totalUpperSectionScore: Int = 0,
    ): Double {
        val upperScoreLevel = totalUpperSectionScore.coerceAtMost(UPPER_BONUS_MIN)
        return computeExpectedScore(BetweenTurnsState(availableCategories, upperScoreLevel))
    }

    fun save(fileName: String?) {
        if (fileName == null) return
        val categories = Category.entries
        computeExpectedScore(categories.toSet())
        val path = Path.of(fileName)
        if (path.notExists()) {
            println("Saving to $fileName")
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
                        val d = memoizedBetweenTurnsE[BetweenTurnsState(c.toCategories(), us)]
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

    fun load(fileName: String?) {
        if (fileName == null) return
        val path = Path.of(fileName)
        if (path.exists()) {
            println("Loading from $fileName")
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
                            memoizedBetweenTurnsE[BetweenTurnsState(categories, us)] = d
                        }
                    }
                }
            }
        }
    }

    private fun computeExpectedScore(state: BetweenTurnsState): Double {
        return memoizedBetweenTurnsE.getOrPut(state) {
            if (state.availableCategories.isEmpty()) {
                return@getOrPut 0.0
            }
            computeWithinTurnExpectedScore(
                state.availableCategories,
                state.upperScoreLevel,
            )
        }
    }

    // Returns the expected score when [categories] are available and upper section total score level (0 - UPPER_BONUS_MIN).
    private fun computeWithinTurnExpectedScore(
        categories: Set<Category>,
        usl: Int,
    ): Double {
        val dp = Array(2) { DoubleArray(dists.size) }
        for (i in dists.indices) {
            dp[1][i] = 0.0
            for (c in categories) {
                dp[1][i] = max(
                    dp[1][i], scoreIfChoose(categories, usl, i, c)
                )
            }
        }
        var current = 0
        var next = 1
        repeat(R - 1) {
            dp[current] = dp[next].copyOf()
            for (i in dists.indices) {
                repeat(numChoices[i]) { k ->
                    var score = 0.0
                    for (j in transDists[i][k].indices) {
                        score += dp[next][transDists[i][k][j]] * transDistProb[i][k][j]
                    }
                    dp[current][i] = max(dp[current][i], score)
                }
            }
            current = 1 - current
            next = 1 - next
        }
        var e = 0.0
        for (i in dists.indices) {
            e += dp[next][i] * prob[i]
        }
        return e
    }

    // Returns the expected score if choosing [category] when the dice number distribution is [dist]
    private fun scoreIfChoose(
        availableCategories: Set<Category>,
        usl: Int,
        distId: Int,
        category: Category,
    ): Double {
        return scores[distId][usl][category.ordinal] + computeExpectedScore(
            BetweenTurnsState(availableCategories - category, usls[distId][usl][category.ordinal])
        )
    }

    // For the given board, returns the best [n] choice
    fun chooseBest(
        n: Int,
        categories: Set<Category>,
        upperTotalScore: Int,
        numRemainRolls: Int,
        faces: IntArray,
    ): List<Choice> {
        check(categories.isNotEmpty()) { "No categories available" }
        check(upperTotalScore in 0..105) { "Invalid upper section total score must be in [0, 105]: $upperTotalScore" }
        check(numRemainRolls in 0..R) { "Invalid number of remaining rolls: $R" }
        check(faces.size == N && faces.all { it in 1..M }) { "Invalid faces: ${faces.contentToString()}" }

        val dist = faces.toDist()
        val distId = dists.indexOfFirst { it.contentEquals(dist) }
        val usl = upperTotalScore.coerceAtMost(
            UPPER_BONUS_MIN
        )
        if (numRemainRolls == 0) {
            return categories.map { c ->
                Pair(c, scoreIfChoose(categories, usl, distId, c))
            }.sortedByDescending { it.second }.take(n).map { Choice.Select(it.first, it.second) }
        }
        val dp = Array(2) { DoubleArray(dists.size) }
        for (i in dists.indices) {
            dp[1][i] = 0.0
            for (c in categories) {
                dp[1][i] = max(
                    dp[1][i], scoreIfChoose(categories, usl, i, c)
                )
            }
        }
        var current = 0
        var next = 1
        repeat(numRemainRolls - 1) {
            dp[current] = dp[next].copyOf()
            for (i in dists.indices) {
                repeat(numChoices[i]) { k ->
                    var score = 0.0
                    for (j in transDists[i][k].indices) {
                        score += dp[next][transDists[i][k][j]] * transDistProb[i][k][j]
                    }
                    dp[current][i] = max(dp[current][i], score)
                }
            }
            current = 1 - current
            next = 1 - next
        }

        // Find the optimal keep for the given distribution
        val i = distId
        val keptScorePairs = (0..<numChoices[i]).map { k ->
            var score = 0.0
            for (j in transDists[i][k].indices) {
                score += dp[next][transDists[i][k][j]] * transDistProb[i][k][j]
            }
            Pair(choice[i][k], score)
        }
        return keptScorePairs.sortedWith { o1, o2 -> if (o1.second < o2.second) 1 else if (o1.second > o2.second) -1 else o2.first.sum() - o1.first.sum() }
            .take(n).map { (k, score) ->
                if (k.contentEquals(dist)) {
                    // Keeping all the dice mean we want to select the Category here
                    val category = categories.maxBy { scoreIfChoose(categories, usl, distId, it) }
                    Choice.Select(category, score)
                } else {
                    Choice.Keep(k.toFaces().toList(), score)
                }
            }
    }
}

// From dist to faces
private fun IntArray.toFaces(): IntArray {
    return flatMapIndexed { num, freq ->
        List(freq) { num + 1 }
    }.sorted().toIntArray()
}


// From faces to dist
private fun IntArray.toDist(): IntArray {
    return IntArray(M) { num ->
        this.count { it == num + 1 }
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

/**
 * The state between the turns.
 *
 * @param availableCategories set of available categories
 * @param upperScoreLevel level of the total score of the upper sections (from [Category.ACES] to
 * [Category.SIXES]) to compute the upper section bonus.  The value ranges from 0 to
 * [UPPER_BONUS_MIN] since the value greater than [UPPER_BONUS_MIN] does not need to be taken into
 * account to compute the estimated score.
 */
private data class BetweenTurnsState(
    val availableCategories: Set<Category>,
    val upperScoreLevel: Int,
) {
    init {
        require(upperScoreLevel in 0..UPPER_BONUS_MIN) {
            "Invalid upper score level: $upperScoreLevel"
        }
    }
}

sealed interface Choice {
    data class Keep(val dice: List<Int>, val expect: Double) : Choice

    data class Select(val category: Category, val expect: Double) : Choice
}