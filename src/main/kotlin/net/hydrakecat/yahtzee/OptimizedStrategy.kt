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

    private val rollsDistWithProbability = Array(N + 1) { ArrayList<Pair<IntArray, Double>>() }

    private val rollsDistSubset =
        Array(N + 1) { Array(N + 1) { Array(N + 1) { Array(N + 1) { Array(N + 1) { Array(N + 1) { ArrayList<IntArray>() } } } } } }

    init {
        for (n in 0..N) {
            for ((dist, probability) in rolledDiceDist(n)) {
                rollsDistWithProbability[n].add(Pair(dist.dist, probability))
            }
        }

        for (i in 0..N) {
            for (j in 0..N - i) {
                for (k in 0..N - i - j) {
                    for (l in 0..N - i - j - k) {
                        for (m in 0..N - i - j - k - l) {
                            val n = N - i - j - k - l - m
                            val d = intArrayOf(i, j, k, l, m, n)
                            rollsDistSubset[i][j][k][l][m][n].addAll(chooseKeep(d))
                        }
                    }
                }
            }
        }
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
        val s = N + 1
        val dp =
            Array(R) { Array(s) { Array(s) { Array(s) { Array(s) { Array(s) { DoubleArray(s) } } } } } }
        for (i in 0..N) {
            for (j in 0..N - i) {
                for (k in 0..N - i - j) {
                    for (l in 0..N - i - j - k) {
                        for (m in 0..N - i - j - k - l) {
                            val n = N - i - j - k - l - m
                            dp[0][i][j][k][l][m][n] = 0.0
                            for (c in categories) {
                                var scoreGained = c.score(intArrayOf(i, j, k, l, m, n))
                                var expectedUpperScoreLevel = upperScoreLevel
                                if (UPPER_SECTION_CATEGORIES.contains(c)) {
                                    expectedUpperScoreLevel += scoreGained
                                    expectedUpperScoreLevel =
                                        expectedUpperScoreLevel.coerceAtMost(UPPER_BONUS_MIN)
                                    if (UPPER_BONUS_MIN in (upperScoreLevel + 1)..expectedUpperScoreLevel) {
                                        scoreGained += UPPER_BONUS
                                    }
                                }
                                val score = scoreGained + computeExpectedScore(
                                    BetweenTurnsState(categories - c, expectedUpperScoreLevel)
                                )
                                dp[0][i][j][k][l][m][n] = max(dp[0][i][j][k][l][m][n], score)
                            }
                        }
                    }
                }
            }
        }
        for (t in 1..<R) {
            for (i in 0..N) {
                for (j in 0..N - i) {
                    for (k in 0..N - i - j) {
                        for (l in 0..N - i - j - k) {
                            for (m in 0..N - i - j - k - l) {
                                val n = N - i - j - k - l - m
                                for (keep in rollsDistSubset[i][j][k][l][m][n]) {
                                    var score = 0.0
                                    for (rwp in rollsDistWithProbability[N - keep.sum()]) {
                                        val d = rwp.first.merge(keep)
                                        score += dp[t - 1][d[0]][d[1]][d[2]][d[3]][d[4]][d[5]] * rwp.second
                                    }
                                    dp[t][i][j][k][l][m][n] = max(dp[t][i][j][k][l][m][n], score)
                                }
                            }
                        }
                    }
                }
            }
        }
        var e = 0.0
        for ((rolls, probability) in rolledDiceDist(N)) {
            val d = rolls.dist
            e += dp[2][d[0]][d[1]][d[2]][d[3]][d[4]][d[5]] * probability
        }
        return e
    }


    private fun IntArray.merge(other: IntArray): IntArray {
        val r = copyOf()
        for (i in r.indices) {
            r[i] += other[i]
        }
        return r
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
