package com.largy.jass.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.largy.jass.GameState
import com.largy.jass.GameViewModel
import com.largy.jass.Round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JassApp(viewModel: GameViewModel = viewModel()) {
    val state = viewModel.state
    var showAddRound by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showNewGameConfirm by rememberSaveable { mutableStateOf(false) }
    var winnerToShow by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(state.winner) {
        if (state.winner != null && winnerToShow == null) {
            winnerToShow = state.winner
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Jass", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                    IconButton(onClick = { showNewGameConfirm = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Nouvelle partie")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.winner == null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddRound = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Manche") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScoreHeader(state = state)

            if (state.rounds.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        RoundListHeader(
                            team1Name = state.team1Name,
                            team2Name = state.team2Name
                        )
                    }
                    itemsIndexed(
                        items = state.rounds.reversed(),
                        key = { _, round -> round.id }
                    ) { index, round ->
                        RoundItem(
                            round = round,
                            roundNumber = state.rounds.size - index,
                            team1Name = state.team1Name,
                            team2Name = state.team2Name,
                            onDelete = { viewModel.deleteRound(round.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }

    if (winnerToShow != null) {
        WinnerDialog(
            winner = winnerToShow!!,
            team1Name = state.team1Name,
            team2Name = state.team2Name,
            team1Score = state.team1Score,
            team2Score = state.team2Score,
            onDismiss = { winnerToShow = null },
            onNewGame = { viewModel.newGame(); winnerToShow = null }
        )
    }

    if (showAddRound) {
        AddRoundDialog(
            teamName1 = state.team1Name,
            teamName2 = state.team2Name,
            onAdd = { round ->
                viewModel.addRound(round)
                showAddRound = false
            },
            onDismiss = { showAddRound = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentTeam1 = state.team1Name,
            currentTeam2 = state.team2Name,
            currentTarget = state.targetScore,
            onSave = { name1, name2, target ->
                viewModel.updateTeamNames(name1, name2)
                viewModel.updateTargetScore(target)
                showSettings = false
            },
            onDismiss = { showSettings = false }
        )
    }

    if (showNewGameConfirm) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirm = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            title = { Text("Nouvelle partie ?") },
            text = { Text("Toutes les manches seront effacées. Le score repart à zéro.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.newGame()
                    winnerToShow = null
                    showNewGameConfirm = false
                }) {
                    Text("Recommencer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun ScoreHeader(state: GameState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamScorePanel(
                name = state.team1Name,
                score = state.team1Score,
                progress = state.progress1,
                isLeading = state.team1Score >= state.team2Score && state.team1Score > 0,
                modifier = Modifier.weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    "VS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "/${state.targetScore}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }

            TeamScorePanel(
                name = state.team2Name,
                score = state.team2Score,
                progress = state.progress2,
                isLeading = state.team2Score > state.team1Score,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TeamScorePanel(
    name: String,
    score: Int,
    progress: Float,
    isLeading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isLeading) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (isLeading)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isLeading)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                "Prêt à jouer !",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                "Appuyez sur « Manche » pour commencer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun RoundListHeader(team1Name: String, team2Name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.List,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "#",
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            team1Name,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            team2Name,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(40.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoundItem(
    round: Round,
    roundNumber: Int,
    team1Name: String,
    team2Name: String,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showDeleteConfirm = true }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = roundNumber.toString(),
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = round.team1Total.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (round.team1Total >= round.team2Total)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            RoundBadges(
                tricks = round.team1Tricks,
                wys = round.team1Wys,
                stock = round.team1Stock,
                allTricks = round.allTricks == 1
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = round.team2Total.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (round.team2Total > round.team1Total)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            RoundBadges(
                tricks = round.team2Tricks,
                wys = round.team2Wys,
                stock = round.team2Stock,
                allTricks = round.allTricks == 2
            )
        }

        IconButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Supprimer",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la manche $roundNumber ?") },
            text = { Text("Cette action ne peut pas être annulée.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun RoundBadges(tricks: Int, wys: Int, stock: Boolean, allTricks: Boolean) {
    val parts = buildList {
        if (tricks > 0) add("${tricks}L")
        if (wys > 0) add("${wys}A")
        if (stock) add("Stöck")
        if (allTricks) add("Match+100")
    }
    if (parts.isNotEmpty()) {
        Text(
            text = parts.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 10.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRoundDialog(
    teamName1: String,
    teamName2: String,
    onAdd: (Round) -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var tricks1Text by remember { mutableStateOf("") }
    val tricks1 = tricks1Text.toIntOrNull()?.coerceIn(0, 157) ?: 0
    val tricks2 = (157 - tricks1).coerceAtLeast(0)

    var wys1Text by remember { mutableStateOf("0") }
    var wys2Text by remember { mutableStateOf("0") }
    val wys1 = wys1Text.toIntOrNull() ?: 0
    val wys2 = wys2Text.toIntOrNull() ?: 0

    // 0=personne, 1=équipe1, 2=équipe2
    var stockTeam by remember { mutableStateOf(0) }
    var allTricksTeam by remember { mutableStateOf(0) }

    val totalTricks = tricks1 + tricks2
    val tricksValid = totalTricks == 157

    val preview1 = tricks1 + wys1 + (if (stockTeam == 1) 20 else 0) + (if (allTricksTeam == 1) 100 else 0)
    val preview2 = tricks2 + wys2 + (if (stockTeam == 2) 20 else 0) + (if (allTricksTeam == 2) 100 else 0)

    val wysPresets = listOf(
        "Séq. 3" to 20,
        "Séq. 4" to 50,
        "Séq. 5" to 100,
        "Carré" to 100,
        "4 Nells" to 150,
        "4 Buur" to 200
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Nouvelle manche",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // ── LEVÉES ──────────────────────────────────────────────
                SectionLabel("Levées (total = 157)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tricks1Text,
                        onValueChange = { v ->
                            if (v.isEmpty() || (v.all { it.isDigit() } && v.length <= 3)) {
                                tricks1Text = v
                            }
                        },
                        label = { Text(teamName1, maxLines = 1) },
                        placeholder = { Text("0–157") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = tricks1Text.isNotEmpty() && !tricksValid,
                        supportingText = if (tricks1Text.isNotEmpty() && !tricksValid) {
                            { Text("Total: $totalTricks ≠ 157") }
                        } else null
                    )
                    OutlinedTextField(
                        value = if (tricks1Text.isEmpty()) "" else tricks2.toString(),
                        onValueChange = {},
                        label = { Text(teamName2, maxLines = 1) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = false
                    )
                }

                // Match bonus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Match +100 :",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = allTricksTeam == 1,
                        onClick = { allTricksTeam = if (allTricksTeam == 1) 0 else 1 },
                        label = { Text(teamName1, maxLines = 1) }
                    )
                    FilterChip(
                        selected = allTricksTeam == 2,
                        onClick = { allTricksTeam = if (allTricksTeam == 2) 0 else 2 },
                        label = { Text(teamName2, maxLines = 1) }
                    )
                }

                HorizontalDivider()

                // ── ANNONCES (WYS) ──────────────────────────────────────
                SectionLabel("Annonces (Wys)")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = wys1Text,
                        onValueChange = { v ->
                            if (v.isEmpty() || (v.all { it.isDigit() } && v.length <= 4)) {
                                wys1Text = v.ifEmpty { "0" }
                            }
                        },
                        label = { Text(teamName1, maxLines = 1) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = wys2Text,
                        onValueChange = { v ->
                            if (v.isEmpty() || (v.all { it.isDigit() } && v.length <= 4)) {
                                wys2Text = v.ifEmpty { "0" }
                            }
                        },
                        label = { Text(teamName2, maxLines = 1) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Quick-add chips for Wys
                Text(
                    "Ajout rapide :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    wysPresets.forEach { (label, value) ->
                        AssistChip(
                            onClick = {
                                wys1Text = ((wys1Text.toIntOrNull() ?: 0) + value).toString()
                            },
                            label = { Text("$teamName1 +$value ($label)", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    wysPresets.forEach { (label, value) ->
                        AssistChip(
                            onClick = {
                                wys2Text = ((wys2Text.toIntOrNull() ?: 0) + value).toString()
                            },
                            label = { Text("$teamName2 +$value ($label)", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                HorizontalDivider()

                // ── STÖCK ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Stöck +20 :",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = stockTeam == 1,
                        onClick = { stockTeam = if (stockTeam == 1) 0 else 1 },
                        label = { Text(teamName1, maxLines = 1) }
                    )
                    FilterChip(
                        selected = stockTeam == 2,
                        onClick = { stockTeam = if (stockTeam == 2) 0 else 2 },
                        label = { Text(teamName2, maxLines = 1) }
                    )
                }

                HorizontalDivider()

                // ── APERÇU ──────────────────────────────────────────────
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                teamName1,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                preview1.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "pts cette manche",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                teamName2,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                preview2.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // ── BOUTONS ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAdd(
                                Round(
                                    team1Tricks = tricks1,
                                    team2Tricks = tricks2,
                                    team1Wys = wys1,
                                    team2Wys = wys2,
                                    team1Stock = stockTeam == 1,
                                    team2Stock = stockTeam == 2,
                                    allTricks = allTricksTeam
                                )
                            )
                        },
                        enabled = tricks1Text.isNotEmpty()
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun SettingsDialog(
    currentTeam1: String,
    currentTeam2: String,
    currentTarget: Int,
    onSave: (String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var team1 by rememberSaveable { mutableStateOf(currentTeam1) }
    var team2 by rememberSaveable { mutableStateOf(currentTeam2) }
    var targetText by rememberSaveable { mutableStateOf(currentTarget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
        title = { Text("Paramètres") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = team1,
                    onValueChange = { team1 = it },
                    label = { Text("Nom équipe 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = team2,
                    onValueChange = { team2 = it },
                    label = { Text("Nom équipe 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Score objectif :", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1000, 2500, 3000).forEach { preset ->
                        FilterChip(
                            selected = targetText == preset.toString(),
                            onClick = { targetText = preset.toString() },
                            label = { Text(preset.toString()) }
                        )
                    }
                }
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { v ->
                        if (v.all { it.isDigit() } && v.length <= 5) targetText = v
                    },
                    label = { Text("Objectif personnalisé") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val target = targetText.toIntOrNull() ?: currentTarget
                onSave(team1, team2, target)
            }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun WinnerDialog(
    winner: String,
    team1Name: String,
    team2Name: String,
    team1Score: Int,
    team2Score: Int,
    onDismiss: () -> Unit,
    onNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        title = {
            Text(
                "$winner gagne !",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Score final",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(team1Name, style = MaterialTheme.typography.labelLarge)
                        Text(
                            team1Score.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (team1Score > team2Score) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(team2Name, style = MaterialTheme.typography.labelLarge)
                        Text(
                            team2Score.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (team2Score > team1Score) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onNewGame) {
                Text("Nouvelle partie")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Continuer") }
        }
    )
}
