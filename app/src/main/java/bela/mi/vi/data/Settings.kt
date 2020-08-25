package bela.mi.vi.data

interface Settings {
    fun getGamePoints() = DEFAULT_GAME_POINTS
    fun getAllTricks() = DEFAULT_ALL_TRICKS
    fun getBelaDeclaration() = DEFAULT_BELA_DECLARATION
    fun getSetLimit() = DEFAULT_SET_LIMIT
    fun getQuickMatchValidityPeriod() = DEFAULT_QUICK_MATCH_VALIDITY_PERIOD

    companion object {
        const val QUICK_MATCH_VALID_2_DAYS = 2
        const val QUICK_MATCH_VALID_7_DAYS = 7
        const val QUICK_MATCH_VALID_30_DAYS = 30
        const val QUICK_MATCH_VALID_ALWAYS = -1

        const val DEFAULT_GAME_POINTS = 162
        const val DEFAULT_ALL_TRICKS = 90
        const val DEFAULT_BELA_DECLARATION = 20
        const val DEFAULT_SET_LIMIT = 1001
        const val DEFAULT_QUICK_MATCH_VALIDITY_PERIOD = QUICK_MATCH_VALID_7_DAYS
    }
}