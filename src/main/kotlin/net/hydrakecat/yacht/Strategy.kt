package net.hydrakecat.yacht

interface Strategy {
    suspend fun init()

    /**
     * Returns the best `n` choices for the given conditions.
     *
     * @param n the maximum number of choices to return
     * @param categories available categories to choose from
     * @param upperTotalScore the total score of the upper section in the scorecard, which is used to calculate the expected score
     * @param faces a list of dice faces, e.g., `[1, 3, 3, 5, 6]`
     **/
    fun chooseBest(
        n: Int,
        categories: Set<Category>,
        upperTotalScore: Int,
        numRemainRolls: Int,
        faces: IntArray,
    ): List<Choice>
}