package bela.mi.vi.data

interface Settings {
    fun getGamePoints() = DEFAULT_GAME_POINTS
    fun getAllTricks() = DEFAULT_ALL_TRICKS
    fun getBelaDeclaration() = DEFAULT_BELA_DECLARATION
    fun getSetLimit() = DEFAULT_SET_LIMIT

    companion object {
        const val DEFAULT_GAME_POINTS = 162
        const val DEFAULT_ALL_TRICKS = 90
        const val DEFAULT_BELA_DECLARATION = 20
        const val DEFAULT_SET_LIMIT = 1001
    }
}