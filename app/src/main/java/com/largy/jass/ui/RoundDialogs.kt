package com.largy.jass.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largy.jass.R
import com.largy.jass.game.GameEvent
import com.largy.jass.game.Rules
import com.largy.jass.game.Team
import com.largy.jass.ui.theme.Slate

// ── Manche ───────────────────────────────────────────────────────────────────

@Composable
fun RoundDialog(
    name1: String,
    name2: String,
    onDismiss: () -> Unit,
    onConfirm: (GameEvent.Round) -> Unit,
) {
    var counting by rememberSaveable { mutableStateOf(Team.ONE) }
    var digits by rememberSaveable { mutableStateOf("") }
    var match by rememberSaveable { mutableStateOf(false) }
    var doubled by rememberSaveable { mutableStateOf(false) }

    val round = when {
        match -> Rules.round(counting, 0, match = true, doubled = doubled)
        digits.isNotEmpty() -> Rules.round(counting, digits.toInt(), match = false, doubled = doubled)
        else -> null
    }

    fun press(d: Char) {
        val next = (digits + d).trimStart('0').ifEmpty { "0" }
        if (next.toInt() <= Rules.ROUND_TOTAL) {
            digits = next
            match = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.round_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Caption(stringResource(R.string.who_counts))
                TeamPicker(name1, name2, counting) { counting = it }

                // Affichage de la saisie
                Box(
                    Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Slate.Line, RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        when {
                            match -> stringResource(R.string.match).uppercase()
                            digits.isEmpty() -> stringResource(R.string.points_hint)
                            else -> digits
                        },
                        color = if (digits.isEmpty() && !match) Slate.ChalkDim else Slate.Chalk,
                        fontSize = if (digits.isEmpty() && !match) 14.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Keypad(onDigit = { press(it) }, onBackspace = { digits = digits.dropLast(1) })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToggleChip(stringResource(R.string.match), match, Modifier.weight(1f)) {
                        match = !match
                        if (match) digits = ""
                    }
                    ToggleChip(stringResource(R.string.doubled), doubled, Modifier.weight(1f)) { doubled = !doubled }
                }

                if (round != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val col = if (doubled) Slate.Accent else Slate.Chalk.copy(alpha = 0.75f)
                        Text(stringResource(R.string.preview_line, name1, round.points(Team.ONE)), color = col, fontSize = 15.sp)
                        Text(stringResource(R.string.preview_line, name2, round.points(Team.TWO)), color = col, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { round?.let(onConfirm) }, enabled = round != null) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = { DismissButton(onDismiss) },
    )
}

@Composable
private fun Keypad(onDigit: (Char) -> Unit, onBackspace: () -> Unit) {
    val backspace = stringResource(R.string.key_backspace)
    val rows = listOf("123", "456", "789", " 0⌫")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    if (key == ' ') {
                        Box(Modifier.weight(1f))
                    } else {
                        OutlinedButton(
                            onClick = { if (key == '⌫') onBackspace() else onDigit(key) },
                            border = BorderStroke(1.dp, Slate.ChalkFaint),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate.Chalk),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .then(if (key == '⌫') Modifier.semantics { contentDescription = backspace } else Modifier),
                        ) {
                            Text("$key", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Annonce (Wys / Stöck) ────────────────────────────────────────────────────

@Composable
fun AnnounceDialog(
    name1: String,
    name2: String,
    onDismiss: () -> Unit,
    onConfirm: (team: Team, points: Int, stoeck: Boolean) -> Unit,
) {
    var team by rememberSaveable { mutableStateOf(Team.ONE) }
    val values = Rules.ANNOUNCES.map { it to false } + (Rules.STOECK to true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.announce_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Caption(stringResource(R.string.for_team))
                TeamPicker(name1, name2, team) { team = it }
                values.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (pts, stoeck) ->
                            OutlinedButton(
                                onClick = { onConfirm(team, pts, stoeck) },
                                border = BorderStroke(1.5.dp, Slate.Line),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = CompactPadding,
                                modifier = Modifier.weight(1f).height(52.dp),
                            ) {
                                Text(
                                    if (stoeck) stringResource(R.string.stoeck) else "+$pts",
                                    fontSize = if (stoeck) 14.sp else 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { DismissButton(onDismiss) },
    )
}

// ── Composants partagés ──────────────────────────────────────────────────────

@Composable
fun Caption(text: String) {
    Text(text, color = Slate.ChalkMuted, fontSize = 12.sp)
}

@Composable
fun DismissButton(onClick: () -> Unit, label: String = stringResource(R.string.cancel)) {
    TextButton(onClick = onClick) { Text(label, color = Slate.ChalkMuted) }
}

@Composable
fun TeamPicker(name1: String, name2: String, selected: Team, onSelect: (Team) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleChip(name1, selected == Team.ONE, Modifier.weight(1f)) { onSelect(Team.ONE) }
        ToggleChip(name2, selected == Team.TWO, Modifier.weight(1f)) { onSelect(Team.TWO) }
    }
}

@Composable
fun ToggleChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.5.dp, if (selected) Slate.Accent else Slate.ChalkFaint),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Slate.Accent.copy(alpha = 0.18f) else Color.Transparent,
            contentColor = if (selected) Slate.Accent else Slate.ChalkMuted,
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = CompactPadding,
        modifier = modifier,
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp, maxLines = 1)
    }
}

/** Padding réduit : les libellés (« +100 », « Stöck », « Aucun ») tiennent sur un tiers de dialogue. */
private val CompactPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
