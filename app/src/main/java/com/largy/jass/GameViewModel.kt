package com.largy.jass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Round(
    val id: Long = System.currentTimeMillis(),
    val team1Tricks: Int,
    val team2Tricks: Int,
    val team1Wys: Int,
    val team2Wys: Int,
    val team1Stock: Boolean,
    val team2Stock: Boolean,
    val allTricks: Int = 0
) {
    // allTricks: 0=personne, 1=équipe1, 2=équipe2 remporte toutes les levées (+100)
    val team1Total: Int
        get() = team1Tricks + team1Wys +
                (if (team1Stock) 20 else 0) +
                (if (allTricks == 1) 100 else 0)

    val team2Total: Int
        get() = team2Tricks + team2Wys +
                (if (team2Stock) 20 else 0) +
                (if (allTricks == 2) 100 else 0)
}

data class GameState(
    val team1Name: String = "Nous",
    val team2Name: String = "Eux",
    val rounds: List<Round> = emptyList(),
    val targetScore: Int = 1000
) {
    val team1Score: Int get() = rounds.sumOf { it.team1Total }
    val team2Score: Int get() = rounds.sumOf { it.team2Total }

    val winner: String?
        get() = when {
            team1Score >= targetScore -> team1Name
            team2Score >= targetScore -> team2Name
            else -> null
        }

    val progress1: Float
        get() = (team1Score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)

    val progress2: Float
        get() = (team2Score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)
}

class GameViewModel : ViewModel() {
    var state by mutableStateOf(GameState())
        private set

    fun addRound(round: Round) {
        state = state.copy(rounds = state.rounds + round)
    }

    fun deleteRound(roundId: Long) {
        state = state.copy(rounds = state.rounds.filter { it.id != roundId })
    }

    fun newGame() {
        state = state.copy(rounds = emptyList())
    }

    fun updateTeamNames(team1: String, team2: String) {
        state = state.copy(
            team1Name = team1.ifBlank { "Nous" },
            team2Name = team2.ifBlank { "Eux" }
        )
    }

    fun updateTargetScore(target: Int) {
        state = state.copy(targetScore = target.coerceAtLeast(100))
    }
}
