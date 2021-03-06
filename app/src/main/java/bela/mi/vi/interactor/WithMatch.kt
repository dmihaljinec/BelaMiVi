package bela.mi.vi.interactor

import bela.mi.vi.data.BelaRepository
import bela.mi.vi.data.BelaRepository.OperationFailed
import bela.mi.vi.data.Game
import bela.mi.vi.data.NewMatch
import bela.mi.vi.data.nowDate
import bela.mi.vi.data.Match
import bela.mi.vi.data.Set
import bela.mi.vi.data.Settings.Companion.QUICK_MATCH_VALID_ALWAYS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class WithMatch @Inject constructor(private val belaRepository: BelaRepository) {

    @Throws(IllegalArgumentException::class)
    suspend fun new(
        teamOnePlayerOneId: Long,
        teamOnePlayerTwoId: Long,
        teamTwoPlayerOneId: Long,
        teamTwoPlayerTwoId: Long,
        setLimit: Int
    ): Long {
        require(
            teamOnePlayerOneId != teamOnePlayerTwoId &&
                    teamOnePlayerOneId != teamTwoPlayerOneId &&
                    teamOnePlayerOneId != teamTwoPlayerTwoId &&
                    teamOnePlayerTwoId != teamTwoPlayerOneId &&
                    teamOnePlayerTwoId != teamTwoPlayerTwoId &&
                    teamTwoPlayerOneId != teamTwoPlayerTwoId
        ) { "Match players must be unique $teamOnePlayerOneId, $teamOnePlayerTwoId, $teamTwoPlayerOneId, $teamTwoPlayerTwoId" }
        require(setLimit > 0) { "Set limit must be greater then zero: $setLimit" }
        return belaRepository.add(
            NewMatch(
                teamOnePlayerOneId = teamOnePlayerOneId,
                teamOnePlayerTwoId = teamOnePlayerTwoId,
                teamTwoPlayerOneId = teamTwoPlayerOneId,
                teamTwoPlayerTwoId = teamTwoPlayerTwoId,
                setLimit = setLimit
            )
        )
    }

    suspend fun quick(): Long {
        val quickMatchPlayers = belaRepository.getQuickMatchPlayers().first()
        if (quickMatchPlayers.size < 4) return -1L
        return belaRepository.add(
            NewMatch(
                teamOnePlayerOneId = quickMatchPlayers[0].id,
                teamOnePlayerTwoId = quickMatchPlayers[1].id,
                teamTwoPlayerOneId = quickMatchPlayers[2].id,
                teamTwoPlayerTwoId = quickMatchPlayers[3].id,
                setLimit = belaRepository.settings.getSetLimit()
            )
        )
    }

    @Throws(OperationFailed::class)
    suspend fun get(id: Long): Flow<Match> = flow {
        belaRepository.getMatch(id).collect {
            emit(it)
            belaRepository.getAllSets(id).collect {
                emit(belaRepository.getMatch(id).first())
            }
        }
    }

    @Throws(OperationFailed::class)
    suspend fun getAll(): Flow<List<Match>> = belaRepository.getAllMatches()

    suspend fun remove(id: Long) {
        belaRepository.removeMatch(id)
    }

    suspend fun removeAll() = belaRepository.removeAllMatches()

    suspend fun removeQuickMatches() {
        val validityPeriod = belaRepository.settings.getQuickMatchValidityPeriod()
        if (validityPeriod == QUICK_MATCH_VALID_ALWAYS) return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val validUntil = getValidUntil(sdf, validityPeriod)
        getAll().first()
                .filter { match -> match.isQuickMatch } // filter list of all matches to only quick matches
                .filter { match -> sdf.parse(match.date)?.before(validUntil) ?: false } // filter list of quick matches to only those older then validUntil date
                .forEach { match -> remove(match.id) }
    }

    suspend fun getAllGamesFromLastSet(matchId: Long): Flow<List<Game>> = belaRepository.getAllGamesFromLastSet(matchId)

    suspend fun getAllGamesInSet(setId: Long): Flow<List<Game>> = belaRepository.getAllGamesInSet(setId)

    suspend fun getAllSets(matchId: Long): Flow<List<Set>> = belaRepository.getAllSets(matchId)

    suspend fun getMatchStatistics(id: Long) = belaRepository.getMatchStatistics(id)

    internal fun getValidUntil(simpleDateFormat: SimpleDateFormat, validityPeriod: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = simpleDateFormat.parse(nowDate()) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, -validityPeriod)
        return calendar.time
    }
}
