package com.largy.jass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largy.jass.GameViewModel
import com.largy.jass.R
import com.largy.jass.game.GameEvent
import com.largy.jass.game.Rules
import com.largy.jass.game.Team
import com.largy.jass.ui.theme.Slate

@Composable
fun RenameDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_title), fontSize = 18.sp) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= GameViewModel.MAX_NAME) text = it },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { DismissButton(onDismiss) },
    )
}

@Composable
fun ResetDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reset_title), fontSize = 18.sp) },
        text = { Text(stringResource(R.string.reset_text), color = Slate.ChalkMuted, fontSize = 14.sp) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.reset_confirm)) } },
        dismissButton = { DismissButton(onDismiss) },
    )
}

@Composable
fun GoalDialog(current: Int?, onDismiss: () -> Unit, onConfirm: (Int?) -> Unit) {
    var custom by rememberSaveable { mutableStateOf(current?.takeIf { it !in Rules.GOALS }?.toString() ?: "") }
    val customValue = custom.toIntOrNull()?.takeIf { it in 1..Rules.MAX_GOAL }
    val presets: List<Int?> = listOf(null) + Rules.GOALS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goal_title), fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { g ->
                            ToggleChip(
                                g?.toString() ?: stringResource(R.string.goal_none),
                                selected = g == current,
                                modifier = Modifier.weight(1f),
                            ) { onConfirm(g) }
                        }
                        repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                    }
                }
                OutlinedTextField(
                    value = custom,
                    onValueChange = { v -> if (v.length <= 4 && v.all(Char::isDigit)) custom = v },
                    label = { Text(stringResource(R.string.goal_custom)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(customValue) }, enabled = customValue != null) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = { DismissButton(onDismiss) },
    )
}

@Composable
fun VictoryDialog(
    winnerName: String,
    name1: String, total1: Int,
    name2: String, total2: Int,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text(stringResource(R.string.victory_title, winnerName), color = Slate.Accent, fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.victory_text, name1, total1, name2, total2), fontSize = 16.sp) },
        confirmButton = { TextButton(onClick = onNewGame) { Text(stringResource(R.string.new_game)) } },
        dismissButton = { DismissButton(onContinue, stringResource(R.string.keep_playing)) },
    )
}

@Composable
fun HistoryDialog(
    events: List<GameEvent>,
    nameOf: (Team) -> String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_title), color = Slate.Accent, fontWeight = FontWeight.Bold) },
        text = {
            if (events.isEmpty()) {
                Text(stringResource(R.string.history_empty), color = Slate.ChalkDim, fontSize = 13.sp)
            } else {
                val newestFirst = events.asReversed()
                LazyColumn(
                    Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    itemsIndexed(newestFirst) { _, e -> HistoryRow(e, nameOf) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        dismissButton = {
            if (events.isNotEmpty()) DismissButton(onUndo, stringResource(R.string.history_undo))
        },
    )
}

@Composable
private fun HistoryRow(e: GameEvent, nameOf: (Team) -> String) {
    val x2 = stringResource(R.string.h_doubled_suffix)
    val highlight = e is GameEvent.Round || e is GameEvent.Announce
    val text = when (e) {
        is GameEvent.Mark -> stringResource(R.string.h_mark, nameOf(e.team), e.zone.value)
        is GameEvent.Unmark -> stringResource(R.string.h_unmark, nameOf(e.team), e.zone.value)
        is GameEvent.Round -> {
            val suffix = if (e.multiplier == 2) x2 else ""
            val m = e.match
            if (m != null) stringResource(R.string.h_match, suffix, nameOf(m), e.points(m))
            else stringResource(
                R.string.h_round, suffix,
                nameOf(Team.ONE), e.points(Team.ONE), nameOf(Team.TWO), e.points(Team.TWO),
            )
        }
        is GameEvent.Announce ->
            stringResource(if (e.stoeck) R.string.h_stoeck else R.string.h_announce, nameOf(e.team), e.points)
    }
    if (highlight) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Slate.Accent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(text, color = Slate.Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    } else {
        Text(text, color = Slate.Chalk.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
    }
}
