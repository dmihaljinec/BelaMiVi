package bela.mi.vi.interactor

import bela.mi.vi.data.*
import bela.mi.vi.data.Set
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow


@ExperimentalCoroutinesApi
class WithMatch(private val belaRepository: BelaRepository) {

    suspend fun new(teamOnePlayerOneId: Long,
                    teamOnePlayerTwoId: Long,
                    teamTwoPlayerOneId: Long,
                    teamTwoPlayerTwoId: Long,
                    setLimit: Int): Long {
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

    suspend fun get(id: Long): Flow<Match> = flow {
        belaRepository.getMatch(id).collect {
            emit(it)
            belaRepository.getAllSets(id).collect {
                emit(belaRepository.getMatch(id).first())
            }
        }
    }

    suspend fun getAll(): Flow<List<Match>> = belaRepository.getAllMatches()

    suspend fun remove(id: Long) {
        belaRepository.removeMatch(id)
    }

    suspend fun removeAll() = belaRepository.removeAllMatches()

    suspend fun getAllGamesFromLastSet(matchId: Long): Flow<List<Game>> = belaRepository.getAllGamesFromLastSet(matchId)

    suspend fun getAllGamesInSet(setId: Long): Flow<List<Game>> = belaRepository.getAllGamesInSet(setId)

    suspend fun getAllSets(matchId: Long): Flow<List<Set>> = belaRepository.getAllSets(matchId)

    suspend fun getMatchStatistics(id: Long) = belaRepository.getMatchStatistics(id)
}