package net.hydrakecat.yacht

import kotlin.math.pow

/** Returns all the possible dice distributions and their probabilities when rolling `num` dice. */
object DiceDistCalculator {
    fun rolledDiceDist(num: Int): List<DistWithProbability> {
        val dists = diceDistIter(0, IntArray(M), num)
        return dists.map { dist: IntArray ->
            check(
                dist.sum() == num
            ) { "Total num of dist should be equal to $num : ${dist.contentToString()}" }
            // Calculate the probability of having dist when rolling num dice
            var p = 1.0
            var n = num
            for (c in dist) {
                if (c == 0) continue
                p *= ncr(n, c)
                n -= c
            }
            p /= M.toDouble().pow(num.toDouble())
            DistWithProbability(dist, p)
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

    private fun diceDistIter(minFace: Int, dist: IntArray, numDices: Int): List<IntArray> {
        if (numDices == 0) {
            return listOf(dist)
        }
        val list = mutableListOf<IntArray>()
        for (i in minFace..<M) {
            // Suppose one die is rolled and its face number turns out to be i
            val u = dist.copyOf(dist.size)
            u[i]++
            // To avoid duplicates, we make sure that the frequencies of the face smaller than i
            // is not incremented in the following recursions
            list.addAll(diceDistIter(i, u, numDices - 1))
        }
        return list
    }
}

/** Distribution of dice faces and its probability */
data class DistWithProbability(val dist: IntArray, val probability: Double) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DistWithProbability) return false

        if (!dist.contentEquals(other.dist)) return false
        if (probability != other.probability) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dist.contentHashCode()
        result = 31 * result + probability.hashCode()
        return result
    }
}
