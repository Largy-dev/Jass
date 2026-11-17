package com.jass.largy.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameTest {

    @Test
    fun `addPoints decompose en 100-50-20 et garde la retenue`() {
        val s = TeamScore().addPoints(157)
        assertEquals(TeamScore(m100 = 1, m50 = 1, m20 = 0, retenue = 7), s)
        assertEquals(157, s.total)
    }

    @Test
    fun `la retenue est cumulée sur la manche suivante`() {
        val s = TeamScore().addPoints(157).addPoints(157) // 157 + 7 = 164 -> 100 + 50, reste 14
        assertEquals(TeamScore(m100 = 2, m50 = 2, m20 = 0, retenue = 14), s)
        assertEquals(314, s.total)
    }

    @Test
    fun `retenue + points peuvent former une barre de 20`() {
        val s = TeamScore(retenue = 14).addPoints(6)
        assertEquals(TeamScore(m20 = 1, retenue = 0), s)
    }

    @Test
    fun `unmark ne descend jamais sous zero`() {
        val s = Scores().apply(GameEvent.Unmark(Team.ONE, Zone.HUNDRED))
        assertEquals(0, s.team1.m100)
    }

    @Test
    fun `mark et unmark touchent la bonne zone et la bonne equipe`() {
        val s = Scores.replay(
            listOf(
                GameEvent.Mark(Team.TWO, Zone.FIFTY),
                GameEvent.Mark(Team.TWO, Zone.FIFTY),
                GameEvent.Unmark(Team.TWO, Zone.FIFTY),
                GameEvent.Mark(Team.ONE, Zone.TWENTY),
            )
        )
        assertEquals(TeamScore(m50 = 1), s.team2)
        assertEquals(TeamScore(m20 = 1), s.team1)
    }

    @Test
    fun `round complete le total a 157 pour l'autre equipe`() {
        val r = Rules.round(Team.TWO, 100, match = false, doubled = false)!!
        assertEquals(GameEvent.Round(score1 = 57, score2 = 100), r)
    }

    @Test
    fun `round double multiplie les points des deux equipes`() {
        val r = Rules.round(Team.ONE, 100, match = false, doubled = true)!!
        assertEquals(200, r.points(Team.ONE))
        assertEquals(114, r.points(Team.TWO))
        val s = Scores().apply(r)
        assertEquals(200, s.team1.total)
        assertEquals(114, s.team2.total)
    }

    @Test
    fun `match donne 257 a l'equipe qui compte et 0 a l'autre`() {
        val r = Rules.round(Team.TWO, 0, match = true, doubled = false)!!
        assertEquals(GameEvent.Round(0, 257, 1, Team.TWO), r)
        assertEquals(257, Scores().apply(r).team2.total)
    }

    @Test
    fun `round refuse les points hors limites`() {
        assertNull(Rules.round(Team.ONE, 158, match = false, doubled = false))
        assertNull(Rules.round(Team.ONE, -1, match = false, doubled = false))
    }

    @Test
    fun `annonce et stoeck s'ajoutent avec la retenue`() {
        val s = Scores.replay(
            listOf(
                Rules.round(Team.ONE, 157, match = false, doubled = false)!!, // ret 7
                GameEvent.Announce(Team.ONE, 50),
                GameEvent.Announce(Team.ONE, Rules.STOECK, stoeck = true),
            )
        )
        assertEquals(227, s.team1.total)
        assertEquals(7, s.team1.retenue)
    }

    @Test
    fun `annuler le dernier evenement revient a l'etat precedent`() {
        val events = listOf(
            Rules.round(Team.ONE, 80, match = false, doubled = false)!!,
            Rules.round(Team.TWO, 95, match = false, doubled = true)!!,
        )
        val before = Scores.replay(events.dropLast(1))
        assertEquals(Scores().apply(events[0]), before)
        assertEquals(Scores.replay(events), before.apply(events[1]))
    }

    @Test
    fun `winner exige l'objectif et une equipe devant`() {
        val s = Scores(TeamScore(m100 = 15), TeamScore(m100 = 9))
        assertEquals(Team.ONE, Rules.winner(s, 1500))
        assertNull(Rules.winner(s, 2500))
        assertNull(Rules.winner(s, null))
        assertNull(Rules.winner(Scores(TeamScore(m100 = 15), TeamScore(m100 = 15)), 1500))
    }
}
