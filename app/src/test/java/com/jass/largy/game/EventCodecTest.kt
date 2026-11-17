package com.jass.largy.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventCodecTest {

    @Test
    fun `aller-retour de tous les types d'evenements`() {
        val events = listOf(
            GameEvent.Mark(Team.ONE, Zone.HUNDRED),
            GameEvent.Unmark(Team.TWO, Zone.TWENTY),
            GameEvent.Round(57, 100, 2),
            GameEvent.Round(257, 0, 1, Team.ONE),
            GameEvent.Announce(Team.TWO, 150),
            GameEvent.Announce(Team.ONE, 20, stoeck = true),
        )
        assertEquals(events, EventCodec.decode(EventCodec.encode(events)))
    }

    @Test
    fun `les lignes invalides sont ignorees`() {
        val text = "M,1,100\nX,1\nM,3,100\nM,1,30\nR,1,2\nR,10,20,5,0\nA,1,abc,0\n\nU,2,50"
        assertEquals(
            listOf(GameEvent.Mark(Team.ONE, Zone.HUNDRED), GameEvent.Unmark(Team.TWO, Zone.FIFTY)),
            EventCodec.decode(text),
        )
    }

    @Test
    fun `texte vide ou null donne une liste vide`() {
        assertTrue(EventCodec.decode(null).isEmpty())
        assertTrue(EventCodec.decode("").isEmpty())
    }
}
