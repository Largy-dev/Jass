package com.largy.jass

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class HistoryEntry(val text: String, val isRound: Boolean = false)

data class TeamScore(
    val m100:    Int = 0,
    val m50:     Int = 0,
    val m20:     Int = 0,
    val retenue: Int = 0
) {
    val total get() = m100 * 100 + m50 * 50 + m20 * 20 + retenue
}

class GameViewModel : ViewModel() {
    var team1     by mutableStateOf(TeamScore());             private set
    var team2     by mutableStateOf(TeamScore());             private set
    var team1Name by mutableStateOf("Nous");                  private set
    var team2Name by mutableStateOf("Eux");                   private set
    var history   by mutableStateOf(listOf<HistoryEntry>());  private set

    // ── Barres manuelles (tapé sur le tableau) ────────────────────────
    fun mark(team: Int, zone: Int) {
        log(HistoryEntry("${teamName(team)} : +${zoneVal(zone)}"))
        markSilent(team, zone)
    }

    fun unmark(team: Int, zone: Int) {
        val count = when (zone) { 1 -> score(team).m100; 2 -> score(team).m50; else -> score(team).m20 }
        if (count > 0) {
            log(HistoryEntry("${teamName(team)} : -${zoneVal(zone)}"))
            unmarkSilent(team, zone)
        }
    }

    // ── Manche (via dialogue) ─────────────────────────────────────────
    fun addRound(score1: Int, score2: Int = maxOf(0, 157 - score1), multiplier: Int = 1) {
        addAsMarks(1, score1.coerceIn(0, 600) * multiplier)
        addAsMarks(2, score2.coerceIn(0, 600) * multiplier)
        val mult = if (multiplier == 2) " ×2" else ""
        log(HistoryEntry("Manche$mult  $team1Name $score1 — $team2Name $score2", isRound = true))
    }

    // ── Noms ──────────────────────────────────────────────────────────
    fun setNames(n1: String, n2: String) {
        team1Name = n1.ifBlank { "Nous" }
        team2Name = n2.ifBlank { "Eux" }
    }

    // ── Reset ─────────────────────────────────────────────────────────
    fun reset() {
        team1   = TeamScore()
        team2   = TeamScore()
        history = listOf()
    }

    // ── Internes ──────────────────────────────────────────────────────

    private fun markSilent(team: Int, zone: Int) {
        if (team == 1) team1 = when (zone) {
            1    -> team1.copy(m100 = team1.m100 + 1)
            2    -> team1.copy(m50  = team1.m50  + 1)
            else -> team1.copy(m20  = team1.m20  + 1)
        }
        else team2 = when (zone) {
            1    -> team2.copy(m100 = team2.m100 + 1)
            2    -> team2.copy(m50  = team2.m50  + 1)
            else -> team2.copy(m20  = team2.m20  + 1)
        }
    }

    private fun unmarkSilent(team: Int, zone: Int) {
        if (team == 1) team1 = when (zone) {
            1    -> team1.copy(m100 = maxOf(0, team1.m100 - 1))
            2    -> team1.copy(m50  = maxOf(0, team1.m50  - 1))
            else -> team1.copy(m20  = maxOf(0, team1.m20  - 1))
        }
        else team2 = when (zone) {
            1    -> team2.copy(m100 = maxOf(0, team2.m100 - 1))
            2    -> team2.copy(m50  = maxOf(0, team2.m50  - 1))
            else -> team2.copy(m20  = maxOf(0, team2.m20  - 1))
        }
    }

    private fun addAsMarks(team: Int, score: Int) {
        var rem  = score + score(team).retenue  // carry-over cumulatif
        val n100 = rem / 100; rem %= 100
        val n50  = rem / 50;  rem %= 50
        val n20  = rem / 20;  rem %= 20
        repeat(n100) { markSilent(team, 1) }
        repeat(n50)  { markSilent(team, 2) }
        repeat(n20)  { markSilent(team, 3) }
        // rem = 0–19 : nouvelle retenue
        if (team == 1) team1 = team1.copy(retenue = rem)
        else           team2 = team2.copy(retenue = rem)
    }

    private fun log(entry: HistoryEntry) {
        history = (listOf(entry) + history).take(100)
    }

    private fun score(team: Int)    = if (team == 1) team1 else team2
    private fun teamName(team: Int) = if (team == 1) team1Name else team2Name
    private fun zoneVal(zone: Int)  = when (zone) { 1 -> 100; 2 -> 50; else -> 20 }
}
