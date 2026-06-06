package com.largy.jass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class TeamScore(
    val m100: Int = 0,
    val m50: Int = 0,
    val m20: Int = 0,
    val wys: Int = 0
) {
    val total get() = m100 * 100 + m50 * 50 + m20 * 20 + wys
}

class GameViewModel : ViewModel() {
    var team1 by mutableStateOf(TeamScore()); private set
    var team2 by mutableStateOf(TeamScore()); private set
    var team1Name by mutableStateOf("Nous"); private set
    var team2Name by mutableStateOf("Eux"); private set

    fun mark(team: Int, zone: Int) {
        if (team == 1) team1 = when (zone) {
            1 -> team1.copy(m100 = team1.m100 + 1)
            2 -> team1.copy(m50 = team1.m50 + 1)
            else -> team1.copy(m20 = team1.m20 + 1)
        }
        else team2 = when (zone) {
            1 -> team2.copy(m100 = team2.m100 + 1)
            2 -> team2.copy(m50 = team2.m50 + 1)
            else -> team2.copy(m20 = team2.m20 + 1)
        }
    }

    fun unmark(team: Int, zone: Int) {
        if (team == 1) team1 = when (zone) {
            1 -> team1.copy(m100 = maxOf(0, team1.m100 - 1))
            2 -> team1.copy(m50 = maxOf(0, team1.m50 - 1))
            else -> team1.copy(m20 = maxOf(0, team1.m20 - 1))
        }
        else team2 = when (zone) {
            1 -> team2.copy(m100 = maxOf(0, team2.m100 - 1))
            2 -> team2.copy(m50 = maxOf(0, team2.m50 - 1))
            else -> team2.copy(m20 = maxOf(0, team2.m20 - 1))
        }
    }

    fun addRound(score1: Int) {
        addAsMarks(1, score1.coerceIn(0, 300))
        addAsMarks(2, maxOf(0, 157 - score1))
    }

    private fun addAsMarks(team: Int, score: Int) {
        var rem = score
        val n100 = rem / 100; rem %= 100
        val n50 = rem / 50; rem %= 50
        val n20 = rem / 20; rem %= 20
        repeat(n100) { mark(team, 1) }
        repeat(n50) { mark(team, 2) }
        repeat(n20) { mark(team, 3) }
        if (rem > 0) setWys(team, (if (team == 1) team1.wys else team2.wys) + rem)
    }

    fun setWys(team: Int, v: Int) {
        if (team == 1) team1 = team1.copy(wys = v.coerceAtLeast(0))
        else team2 = team2.copy(wys = v.coerceAtLeast(0))
    }

    fun setNames(n1: String, n2: String) {
        team1Name = n1.ifBlank { "Nous" }
        team2Name = n2.ifBlank { "Eux" }
    }

    fun reset() {
        team1 = TeamScore()
        team2 = TeamScore()
    }
}
