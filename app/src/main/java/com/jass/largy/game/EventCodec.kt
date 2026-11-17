package com.jass.largy.game

/**
 * Sérialisation texte des événements (une ligne par événement), pour la sauvegarde locale.
 * Les lignes invalides sont ignorées au décodage.
 */
object EventCodec {

    fun encode(events: List<GameEvent>): String = events.joinToString("\n") { encode(it) }

    fun decode(text: String?): List<GameEvent> =
        text.orEmpty().lineSequence().mapNotNull { decodeLine(it.trim()) }.toList()

    private fun encode(e: GameEvent): String = when (e) {
        is GameEvent.Mark -> "M,${e.team.number},${e.zone.value}"
        is GameEvent.Unmark -> "U,${e.team.number},${e.zone.value}"
        is GameEvent.Round -> "R,${e.score1},${e.score2},${e.multiplier},${e.match?.number ?: 0}"
        is GameEvent.Announce -> "A,${e.team.number},${e.points},${if (e.stoeck) 1 else 0}"
    }

    private fun decodeLine(line: String): GameEvent? {
        val p = line.split(',')
        return when (p.firstOrNull()) {
            "M" -> if (p.size == 3) markOf(p) { t, z -> GameEvent.Mark(t, z) } else null
            "U" -> if (p.size == 3) markOf(p) { t, z -> GameEvent.Unmark(t, z) } else null
            "R" -> if (p.size == 5) round(p) else null
            "A" -> if (p.size == 4) announce(p) else null
            else -> null
        }
    }

    private fun markOf(p: List<String>, f: (Team, Zone) -> GameEvent): GameEvent? {
        val team = team(p[1]) ?: return null
        val zone = Zone.entries.firstOrNull { it.value == p[2].toIntOrNull() } ?: return null
        return f(team, zone)
    }

    private fun round(p: List<String>): GameEvent? {
        val s1 = p[1].toIntOrNull()?.takeIf { it in 0..Rules.MATCH_TOTAL } ?: return null
        val s2 = p[2].toIntOrNull()?.takeIf { it in 0..Rules.MATCH_TOTAL } ?: return null
        val mult = p[3].toIntOrNull()?.takeIf { it in 1..2 } ?: return null
        val match = if (p[4] == "0") null else team(p[4]) ?: return null
        return GameEvent.Round(s1, s2, mult, match)
    }

    private fun announce(p: List<String>): GameEvent? {
        val team = team(p[1]) ?: return null
        val pts = p[2].toIntOrNull()?.takeIf { it in 1..1000 } ?: return null
        return GameEvent.Announce(team, pts, p[3] == "1")
    }

    private fun team(s: String): Team? = Team.entries.firstOrNull { it.number == s.toIntOrNull() }
}
