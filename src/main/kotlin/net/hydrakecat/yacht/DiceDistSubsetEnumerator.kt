package net.hydrakecat.yacht

/** Returns all the subsets of the given distribution of dice faces.
 *
 * For example, if the distribution `[3, 0, 0, 0, 0, 0]` is given
 * (this means all the dice show the face 1), the method is expected to return
 * `[[0, 0, 0, 0, 0, 0], [1, 0, 0, 0, 0, 0], [2, 0, 0, 0, 0, 0], [3, 0, 0, 0, 0, 0]]`.
 **/
object DiceDistSubsetEnumerator {
    fun listSubsets(dist: IntArray): List<IntArray> {
        val list: MutableList<IntArray> = ArrayList()
        for (i in 0..dist.sum()) {
            list.addAll(listIter(dist, 0, IntArray(M), i))
        }
        return list
    }

    private fun listIter(
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
            list.addAll(listIter(remaining, idx + 1, candidate, numToChoose - i))
            candidate[idx] -= i
            remaining[idx] += i
        }
        return list
    }
}