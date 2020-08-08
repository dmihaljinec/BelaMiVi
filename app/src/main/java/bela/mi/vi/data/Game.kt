package bela.mi.vi.data

data class Game(
    val id: Long,
    val setId: Long,
    val allTricks: Boolean = false,
    val teamOneDeclarations: Int = 0,
    val teamTwoDeclarations: Int = 0,
    val teamOnePoints: Int = 0,
    val teamTwoPoints: Int = 0
) {
    constructor(
        setId: Long,
        allTricks: Boolean = false,
        teamOneDeclarations: Int = 0,
        teamTwoDeclarations: Int = 0,
        teamOnePoints: Int = 0,
        teamTwoPoints: Int = 0
    ) : this(
        0,
        setId,
        allTricks,
        teamOneDeclarations,
        teamTwoDeclarations,
        teamOnePoints,
        teamTwoPoints
    )
}