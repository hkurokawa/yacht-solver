package net.hydrakecat.yahtzee

import java.util.*
import kotlin.math.max
import kotlin.math.pow


// Number of dices
private const val N = 5

// Num faces of the dice
private const val M = 6

// Num rolls in a turn
private const val R = 3

// Upper section bonus criteria
private const val UPPER_BONUS_MIN = 63

private const val UPPER_BONUS = 35

class OptimizedStrategy {
    private val memoizedBetweenTurnsE = HashMap<BetweenTurnsState, Double>()

    // List of possible number distribution when rolling N dices
    private val dists: Array<IntArray>

    // Probability to see the ith distribution of numbers when rolling N dices
    private val prob: DoubleArray

    // Number of possible choices to keep from the ith distribution of N dices
    private val numChoices: IntArray

    // List of distributions of N dices from the ith distribution when choosing the jth choice
    private val transDists: Array<Array<IntArray>>

    // Probability to transit from the ith distribution to the kth distribution when choosing the jth choice
    private val transDistProb: Array<Array<DoubleArray>>

    init {
        val numbersWithProbList = rolledDiceDist(N)
        val n = numbersWithProbList.size
        dists = Array(n) { IntArray(M) }
        prob = DoubleArray(n)
        numChoices = IntArray(n)
        var C = 0
        for (i in numbersWithProbList.indices) {
            dists[i] = numbersWithProbList[i].roll.dist
            prob[i] = numbersWithProbList[i].probability
            numChoices[i] = chooseKeep(dists[i]).size
            C = max(C, numChoices[i])
        }
        transDists = Array(n) { Array(C) { IntArray(0) } }
        transDistProb = Array(n) { Array(C) { DoubleArray(0) } }
        for (i in 0..<n) {
            chooseKeep(dists[i]).forEachIndexed { j, kept ->
                val distList = mutableListOf<Int>()
                val probList = mutableListOf<Double>()
                // the jth choice is selected and we keep the dist of kept
                for ((d, p) in rolledDiceDist(N - kept.sum())) {
                    val ns = merge(kept, d.dist)
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
        }
    }

    private fun merge(array: IntArray, other: IntArray): IntArray {
        val r = array.copyOf()
        for (i in r.indices) {
            r[i] += other[i]
        }
        return r
    }

    fun computeExpectedScore(
        numAvailableFirstCategories: Int
    ): Double {
        return computeExpectedScore(
            BetweenTurnsState(
                EnumSet.range(
                    Category.ACES,
                    Category.entries[numAvailableFirstCategories - 1]
                ), 0
            )
        )
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

    private fun computeWithinTurnExpectedScore(
        categories: Set<Category>,
        upperScoreLevel: Int,
    ): Double {
        val dp = Array(2) { DoubleArray(dists.size) }
        for (i in dists.indices) {
            dp[1][i] = 0.0
            for (c in categories) {
                var scoreGained = c.score(dists[i])
                var nextUpperScoreLevel = upperScoreLevel
                if (UPPER_SECTION_CATEGORIES.contains(c)) {
                    nextUpperScoreLevel += scoreGained
                    nextUpperScoreLevel = nextUpperScoreLevel.coerceAtMost(UPPER_BONUS_MIN)
                    if (UPPER_BONUS_MIN in (upperScoreLevel + 1)..nextUpperScoreLevel) {
                        scoreGained += UPPER_BONUS
                    }
                }
                val score = scoreGained + computeExpectedScore(
                    BetweenTurnsState(categories - c, nextUpperScoreLevel)
                )
                dp[1][i] = max(dp[1][i], score)
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


    // Returns all the possible dices to keep
    fun chooseKeep(rolledDiceDist: IntArray): List<IntArray> {
        val list: MutableList<IntArray> = ArrayList()
        for (i in 0..Arrays.stream(rolledDiceDist).sum()) {
            list.addAll(chooseKeepIter(rolledDiceDist, 0, IntArray(M), i))
        }
        return list
    }

    private fun chooseKeepIter(
        remaining: IntArray, currentIdx: Int, candidate: IntArray,
        numToChoose: Int,
    ): List<IntArray> {
        var idx = currentIdx
        if (numToChoose == 0) {
            return listOf(candidate.copyOf(candidate.size))
        }
        while (idx < M && remaining[idx] == 0) idx++
        if (idx >= M) return emptyList()
        val list: MutableList<IntArray> = ArrayList()
        for (i in 0..remaining[idx]) {
            // If choose i dices from the currentIdx
            candidate[idx] += i
            remaining[idx] -= i
            list.addAll(chooseKeepIter(remaining, idx + 1, candidate, numToChoose - i))
            candidate[idx] -= i
            remaining[idx] += i
        }
        return list
    }

    // Returns all the possible rolled dices for num dices with its probability
    fun rolledDiceDist(num: Int): List<RollWithProbability> {
        val rolls = rolledDiceIter(0, IntArray(M), num)
        return rolls.map { roll: IntArray ->
            check(
                roll.sum() == num
            ) { "Total num of rolls should be equal to $num : ${roll.contentToString()}" }
            var p = 1.0
            var n = num
            for (c in roll) {
                if (c == 0) continue
                p *= ncr(n, c)
                n -= c
            }
            p /= M.toDouble().pow(num.toDouble())
            RollWithProbability(roll, p)
        }
    }

    private fun ncr(n: Int, r: Int): Double {
        if (n < r) return 0.0
        if (n - r < r) return ncr(n, n - r)
        var a = 1.0
        for (i in n downTo n - r + 1) a *= i.toDouble()
        for (i in r downTo 2) a /= i.toDouble()
        return a
    }

    private fun rolledDiceIter(startValue: Int, roll: IntArray, numDices: Int): List<IntArray> {
        if (numDices == 0) {
            return listOf(roll)
        }
        val list = mutableListOf<IntArray>()
        for (i in startValue..<M) {
            val u = roll.copyOf(roll.size)
            u[i]++
            list.addAll(rolledDiceIter(i, u, numDices - 1))
        }
        return list
    }
}

private val UPPER_SECTION_CATEGORIES = EnumSet.range(Category.ACES, Category.SIXES)

enum class Category {
    ACES, TWOS, THREES, FOURS, FIVES, SIXES, THREE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE, SMALL_STRAIGHT, LARGE_STRAIGHT, YACHT, CHANCE;

    fun score(rolledDice: IntArray): Int {
        check(
            rolledDice.size == M && rolledDice.sum() == N
        ) { "The distribution is not as expected: ${rolledDice.contentToString()}" }

        return when (this) {
            ACES -> rolledDice[0] * 1
            TWOS -> rolledDice[1] * 2
            THREES -> rolledDice[2] * 3
            FOURS -> rolledDice[3] * 4
            FIVES -> rolledDice[4] * 5
            SIXES -> rolledDice[5] * 6
            THREE_OF_A_KIND -> {
                if (rolledDice.any { it >= 3 }) {
                    rolledDice.computeSumFaces()
                } else 0
            }

            FOUR_OF_A_KIND -> {
                if (rolledDice.any { it >= 4 }) {
                    rolledDice.computeSumFaces()
                } else 0
            }

            FULL_HOUSE -> {
                if (rolledDice.count { it >= 2 } == 2 && rolledDice.count { it == 0 } == M - 2) {
                    25
                } else 0
            }

            SMALL_STRAIGHT -> {
                for (i in 0..2) if (rolledDice.slice(i..i + 3).all { it > 0 }) return 30
                0
            }

            LARGE_STRAIGHT -> {
                for (i in 0..1) {
                    if (rolledDice.slice(i..i + 4).all { it > 0 }) return 40
                }
                0
            }

            YACHT -> {
                if (rolledDice.any { it >= N }) 50 else 0
            }

            CHANCE -> {
                rolledDice.computeSumFaces()
            }
        }
    }
}

private fun IntArray.computeSumFaces(): Int {
    check(this.size == M && this.all { it in 0..N }) { throw IllegalStateException("Invalid dices: ${this.contentToString()}") }
    return this.mapIndexed { index, i -> (index + 1) * i }.sum()
}

data class Roll(val dist: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Roll) return false

        if (!dist.contentEquals(other.dist)) return false

        return true
    }

    override fun hashCode(): Int {
        return dist.contentHashCode()
    }

    override fun toString(): String {
        return dist.contentToString()
    }

    operator fun plus(other: Roll): Roll {
        val s = this.dist.copyOf()
        for (i in other.dist.indices) {
            s[i] += other.dist[i]
        }
        return Roll(s)
    }
}

data class RollWithProbability(val roll: Roll, val probability: Double) {
    constructor(dist: IntArray, probability: Double) : this(Roll(dist), probability)
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
