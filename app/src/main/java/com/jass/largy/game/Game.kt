package com.jass.largy.game

/** Équipe. [number] est le numéro affiché sur l'ardoise. */
enum class Team(val number: Int) {
    ONE(1), TWO(2);

    val other: Team get() = if (this == ONE) TWO else ONE
}

/** Trait du Z sur lequel on trace une barre. */
enum class Zone(val value: Int) { HUNDRED(100), FIFTY(50), TWENTY(20) }

/** Barres tracées sur le Z d'une équipe + retenue (0–19) pas encore représentable. */
data class TeamScore(
    val m100: Int = 0,
    val m50: Int = 0,
    val m20: Int = 0,
    val retenue: Int = 0,
) {
    val total: Int get() = m100 * 100 + m50 * 50 + m20 * 20 + retenue

    fun count(zone: Zone): Int = when (zone) {
        Zone.HUNDRED -> m100
        Zone.FIFTY -> m50
        Zone.TWENTY -> m20
    }

    fun withCount(zone: Zone, n: Int): TeamScore {
        val v = maxOf(0, n)
        return when (zone) {
            Zone.HUNDRED -> copy(m100 = v)
            Zone.FIFTY -> copy(m50 = v)
            Zone.TWENTY -> copy(m20 = v)
        }
    }

    /** Ajoute des points en barres 100/50/20 ; le reste (< 20) devient la nouvelle retenue. */
    fun addPoints(points: Int): TeamScore {
        var rem = maxOf(0, points) + retenue
        val n100 = rem / 100; rem %= 100
        val n50 = rem / 50; rem %= 50
        val n20 = rem / 20; rem %= 20
        return TeamScore(m100 + n100, m50 + n50, m20 + n20, rem)
    }
}

/** Une action de jeu. L'état est toujours recalculé à partir de la liste d'événements (undo gratuit). */
sealed interface GameEvent {
    data class Mark(val team: Team, val zone: Zone) : GameEvent
    data class Unmark(val team: Team, val zone: Zone) : GameEvent

    /** [score1]/[score2] : points bruts avant multiplicateur. [match] : équipe ayant fait toutes les levées. */
    data class Round(
        val score1: Int,
        val score2: Int,
        val multiplier: Int = 1,
        val match: Team? = null,
    ) : GameEvent {
        fun raw(team: Team): Int = if (team == Team.ONE) score1 else score2
        fun points(team: Team): Int = raw(team) * multiplier
    }

    /** Annonce (Wys) ou Stöck. */
    data class Announce(val team: Team, val points: Int, val stoeck: Boolean = false) : GameEvent
}

data class Scores(
    val team1: TeamScore = TeamScore(),
    val team2: TeamScore = TeamScore(),
) {
    operator fun get(team: Team): TeamScore = if (team == Team.ONE) team1 else team2

    private fun update(team: Team, f: (TeamScore) -> TeamScore): Scores =
        if (team == Team.ONE) copy(team1 = f(team1)) else copy(team2 = f(team2))

    fun apply(event: GameEvent): Scores = when (event) {
        is GameEvent.Mark -> update(event.team) { it.withCount(event.zone, it.count(event.zone) + 1) }
        is GameEvent.Unmark -> update(event.team) { it.withCount(event.zone, it.count(event.zone) - 1) }
        is GameEvent.Round -> Team.entries.fold(this) { s, t -> s.update(t) { it.addPoints(event.points(t)) } }
        is GameEvent.Announce -> update(event.team) { it.addPoints(event.points) }
    }

    companion object {
        fun replay(events: List<GameEvent>): Scores = events.fold(Scores()) { s, e -> s.apply(e) }
    }
}

object Rules {
    const val ROUND_TOTAL = 157
    const val MATCH_TOTAL = 257
    const val STOECK = 20
    const val DEFAULT_GOAL = 1500
    const val MAX_GOAL = 9999
    val ANNOUNCES = listOf(20, 50, 100, 150, 200)
    val GOALS = listOf(1000, 1500, 2500, 3000)

    /**
     * Construit une manche à partir des points de l'équipe qui compte.
     * Retourne null si les points sont hors de 0..157 (sauf match).
     */
    fun round(counting: Team, points: Int, match: Boolean, doubled: Boolean): GameEvent.Round? {
        val mult = if (doubled) 2 else 1
        val (mine, theirs) = when {
            match -> MATCH_TOTAL to 0
            points in 0..ROUND_TOTAL -> points to ROUND_TOTAL - points
            else -> return null
        }
        return if (counting == Team.ONE) {
            GameEvent.Round(mine, theirs, mult, if (match) Team.ONE else null)
        } else {
            GameEvent.Round(theirs, mine, mult, if (match) Team.TWO else null)
        }
    }

    /** Équipe gagnante si au moins une a atteint l'objectif ; null si pas d'objectif ou égalité. */
    fun winner(scores: Scores, goal: Int?): Team? {
        if (goal == null) return null
        val t1 = scores.team1.total
        val t2 = scores.team2.total
        return when {
            maxOf(t1, t2) < goal || t1 == t2 -> null
            t1 > t2 -> Team.ONE
            else -> Team.TWO
        }
    }
}
