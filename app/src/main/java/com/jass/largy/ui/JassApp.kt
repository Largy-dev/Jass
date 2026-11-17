package com.jass.largy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jass.largy.GameViewModel
import com.jass.largy.R
import com.jass.largy.game.Team
import com.jass.largy.ui.theme.Slate

private enum class DialogKind { NONE, ROUND, ANNOUNCE, HISTORY, RESET, GOAL, RENAME_1, RENAME_2 }

@Composable
fun JassApp(vm: GameViewModel = viewModel()) {
    var dialog by rememberSaveable { mutableStateOf(DialogKind.NONE) }
    val close = { dialog = DialogKind.NONE }
    val winner = vm.winner

    Column(Modifier.fillMaxSize().background(Slate.Background)) {
        TopBar(
            goal = vm.goal,
            canUndo = vm.events.isNotEmpty(),
            onReset = { dialog = DialogKind.RESET },
            onUndo = vm::undo,
            onGoal = { dialog = DialogKind.GOAL },
            onHistory = { dialog = DialogKind.HISTORY },
        )

        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val landscape = maxWidth > maxHeight
            val slate = @Composable { team: Team, mod: Modifier ->
                TeamSlate(
                    number = team.number,
                    name = vm.name(team),
                    score = vm.scores[team],
                    goal = vm.goal,
                    isWinner = winner == team,
                    onRename = { dialog = if (team == Team.ONE) DialogKind.RENAME_1 else DialogKind.RENAME_2 },
                    onMark = { vm.mark(team, it) },
                    onUnmark = { vm.unmark(team, it) },
                    modifier = mod,
                )
            }
            if (landscape) {
                Row(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
                    slate(Team.ONE, Modifier.weight(1f).fillMaxHeight())
                    Box(Modifier.width(1.5.dp).fillMaxHeight().background(Slate.Line))
                    slate(Team.TWO, Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    slate(Team.ONE, Modifier.weight(1f).fillMaxWidth())
                    HorizontalDivider(color = Slate.Line, thickness = 1.5.dp)
                    slate(Team.TWO, Modifier.weight(1f).fillMaxWidth())
                }
            }
        }

        BottomBar(
            onAnnounce = { dialog = DialogKind.ANNOUNCE },
            onRound = { dialog = DialogKind.ROUND },
        )
    }

    when (dialog) {
        DialogKind.NONE -> Unit
        DialogKind.ROUND -> RoundDialog(vm.team1Name, vm.team2Name, close) { vm.addRound(it); close() }
        DialogKind.ANNOUNCE -> AnnounceDialog(vm.team1Name, vm.team2Name, close) { t, pts, stoeck ->
            vm.addAnnounce(t, pts, stoeck); close()
        }
        DialogKind.HISTORY -> HistoryDialog(vm.events, vm::name, onUndo = vm::undo, onDismiss = close)
        DialogKind.RESET -> ResetDialog(close) { vm.reset(); close() }
        DialogKind.GOAL -> GoalDialog(vm.goal, close) { vm.changeGoal(it); close() }
        DialogKind.RENAME_1, DialogKind.RENAME_2 -> {
            val team = if (dialog == DialogKind.RENAME_1) Team.ONE else Team.TWO
            RenameDialog(vm.name(team), close) { vm.rename(team, it); close() }
        }
    }

    if (winner != null && !vm.victoryDismissed && dialog == DialogKind.NONE) {
        VictoryDialog(
            winnerName = vm.name(winner),
            name1 = vm.team1Name, total1 = vm.scores.team1.total,
            name2 = vm.team2Name, total2 = vm.scores.team2.total,
            onNewGame = vm::reset,
            onContinue = vm::dismissVictory,
        )
    }
}

@Composable
private fun TopBar(
    goal: Int?,
    canUndo: Boolean,
    onReset: () -> Unit,
    onUndo: () -> Unit,
    onGoal: () -> Unit,
    onHistory: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Slate.Surface)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(Modifier.align(Alignment.CenterStart)) {
            IconText("✕", stringResource(R.string.cd_reset), onReset)
            IconText("↶", stringResource(R.string.cd_undo), onUndo, enabled = canUndo)
        }
        Text(
            stringResource(R.string.title),
            color = Slate.Accent,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Center),
        )
        Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
            val goalDescription = stringResource(R.string.cd_goal) + " : " +
                (goal?.toString() ?: stringResource(R.string.goal_none))
            TextButton(
                onClick = onGoal,
                modifier = Modifier.semantics { contentDescription = goalDescription },
            ) {
                Text(
                    if (goal != null) stringResource(R.string.goal_chip, goal) else stringResource(R.string.goal_chip_none),
                    color = Slate.ChalkMuted,
                    fontSize = 13.sp,
                )
            }
            IconText("≡", stringResource(R.string.cd_history), onHistory, fontSize = 22)
        }
    }
}

@Composable
private fun IconText(glyph: String, description: String, onClick: () -> Unit, enabled: Boolean = true, fontSize: Int = 18) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Text(glyph, color = if (enabled) Slate.ChalkMuted else Slate.ChalkFaint, fontSize = fontSize.sp)
    }
}

@Composable
private fun BottomBar(onAnnounce: () -> Unit, onRound: () -> Unit) {
    Column {
        HorizontalDivider(color = Slate.Line, thickness = 1.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .background(Slate.Surface)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onAnnounce,
                border = BorderStroke(1.5.dp, Slate.Accent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(46.dp),
            ) {
                Text(stringResource(R.string.add_announce), fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }
            Button(
                onClick = onRound,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.4f).height(46.dp),
            ) {
                Text(stringResource(R.string.add_round), fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
            }
        }
    }
}
