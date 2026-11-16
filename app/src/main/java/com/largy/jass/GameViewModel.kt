package com.largy.jass

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.largy.jass.game.EventCodec
import com.largy.jass.game.GameEvent
import com.largy.jass.game.Rules
import com.largy.jass.game.Scores
import com.largy.jass.game.Team
import com.largy.jass.game.Zone

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("jass_game", Context.MODE_PRIVATE)

    var events by mutableStateOf(EventCodec.decode(prefs.getString(KEY_EVENTS, null))); private set
    var scores by mutableStateOf(Scores.replay(events)); private set
    var team1Name by mutableStateOf(prefs.getString(KEY_NAME1, null) ?: DEFAULT_NAME1); private set
    var team2Name by mutableStateOf(prefs.getString(KEY_NAME2, null) ?: DEFAULT_NAME2); private set
    var goal by mutableStateOf(prefs.getInt(KEY_GOAL, Rules.DEFAULT_GOAL).takeIf { it > 0 }); private set
    var victoryDismissed by mutableStateOf(prefs.getBoolean(KEY_VICTORY_DISMISSED, false)); private set

    val winner: Team? get() = Rules.winner(scores, goal)

    fun name(team: Team): String = if (team == Team.ONE) team1Name else team2Name

    // ── Actions ───────────────────────────────────────────────────────

    fun mark(team: Team, zone: Zone) = push(GameEvent.Mark(team, zone))

    fun unmark(team: Team, zone: Zone) {
        if (scores[team].count(zone) > 0) push(GameEvent.Unmark(team, zone))
    }

    fun addRound(round: GameEvent.Round) = push(round)

    fun addAnnounce(team: Team, points: Int, stoeck: Boolean) =
        push(GameEvent.Announce(team, points, stoeck))

    fun undo() {
        if (events.isNotEmpty()) setEvents(events.dropLast(1), Scores.replay(events.dropLast(1)))
    }

    /** Efface scores et historique ; conserve noms et objectif. */
    fun reset() = setEvents(emptyList(), Scores())

    fun rename(team: Team, name: String) {
        val clean = name.trim().take(MAX_NAME)
        if (team == Team.ONE) team1Name = clean.ifBlank { DEFAULT_NAME1 }
        else team2Name = clean.ifBlank { DEFAULT_NAME2 }
        save()
    }

    fun changeGoal(newGoal: Int?) {
        goal = newGoal?.takeIf { it in 1..Rules.MAX_GOAL }
        syncVictory()
        save()
    }

    fun dismissVictory() {
        victoryDismissed = true
        save()
    }

    // ── Internes ──────────────────────────────────────────────────────

    private fun push(event: GameEvent) = setEvents(events + event, scores.apply(event))

    private fun setEvents(list: List<GameEvent>, newScores: Scores) {
        events = list
        scores = newScores
        syncVictory()
        save()
    }

    /** Le dialogue de victoire réapparaît si plus personne n'a gagné (undo, reset, objectif relevé). */
    private fun syncVictory() {
        if (winner == null) victoryDismissed = false
    }

    private fun save() {
        prefs.edit()
            .putString(KEY_EVENTS, EventCodec.encode(events))
            .putString(KEY_NAME1, team1Name)
            .putString(KEY_NAME2, team2Name)
            .putInt(KEY_GOAL, goal ?: 0)
            .putBoolean(KEY_VICTORY_DISMISSED, victoryDismissed)
            .apply()
    }

    companion object {
        const val MAX_NAME = 12
        private const val DEFAULT_NAME1 = "Nous"
        private const val DEFAULT_NAME2 = "Eux"
        private const val KEY_EVENTS = "events"
        private const val KEY_NAME1 = "name1"
        private const val KEY_NAME2 = "name2"
        private const val KEY_GOAL = "goal"
        private const val KEY_VICTORY_DISMISSED = "victory_dismissed"
    }
}
