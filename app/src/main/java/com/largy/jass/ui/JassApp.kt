package com.largy.jass.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.largy.jass.GameViewModel
import com.largy.jass.TeamScore

private val BG      = Color(0xFF1A1A14)
private val SURFACE = Color(0xFF222218)
private val ORANGE  = Color(0xFFCC4400)
private val CHALK   = Color(0xFFF0EDD5)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JassApp(vm: GameViewModel = viewModel()) {
    var showRound   by remember { mutableStateOf(false) }
    var editingTeam by remember { mutableStateOf(0) }

    Box(Modifier.fillMaxSize().background(BG)) {
        Column(Modifier.fillMaxSize()) {

            // ── App bar ───────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(SURFACE)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    "JASS",
                    color      = ORANGE,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    modifier   = Modifier.align(Alignment.Center)
                )
                TextButton(
                    onClick  = { vm.reset() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("✕", color = CHALK.copy(alpha = 0.25f), fontSize = 16.sp)
                }
            }

            // ── Score header (tap nom pour renommer) ──────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamHeader(vm.team1Name, vm.team1.total, onTap = { editingTeam = 1 })
                TeamHeader(vm.team2Name, vm.team2.total, onTap = { editingTeam = 2 }, rightAlign = true)
            }

            HorizontalDivider(color = ORANGE.copy(alpha = 0.5f), thickness = 1.dp)

            // ── Boards côte à côte ────────────────────────────────────
            Row(Modifier.weight(1f).fillMaxWidth()) {
                ZBoard(
                    score    = vm.team1,
                    onMark   = { vm.mark(1, it)   },
                    onUnmark = { vm.unmark(1, it) },
                    onWys    = { vm.setWys(1, it) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Box(Modifier.width(1.5.dp).fillMaxHeight().background(ORANGE.copy(alpha = 0.5f)))
                ZBoard(
                    score    = vm.team2,
                    onMark   = { vm.mark(2, it)   },
                    onUnmark = { vm.unmark(2, it) },
                    onWys    = { vm.setWys(2, it) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // ── Bottom bar ────────────────────────────────────────────
            HorizontalDivider(color = ORANGE.copy(alpha = 0.5f), thickness = 1.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(SURFACE)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick  = { showRound = true },
                    colors   = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = CHALK),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("+ Manche", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showRound) {
        RoundDialog(
            name1     = vm.team1Name,
            name2     = vm.team2Name,
            onDismiss = { showRound = false },
            onConfirm = { s1, s2, mult -> vm.addRound(s1, s2, mult); showRound = false }
        )
    }

    if (editingTeam > 0) {
        RenameDialog(
            current   = if (editingTeam == 1) vm.team1Name else vm.team2Name,
            onDismiss = { editingTeam = 0 },
            onConfirm = { name ->
                if (editingTeam == 1) vm.setNames(name, vm.team2Name)
                else                  vm.setNames(vm.team1Name, name)
                editingTeam = 0
            }
        )
    }
}

// ── En-tête équipe (cliquable pour renommer) ─────────────────────────────────

@Composable
private fun TeamHeader(
    name:       String,
    total:      Int,
    onTap:      () -> Unit,
    rightAlign: Boolean = false
) {
    Column(
        horizontalAlignment = if (rightAlign) Alignment.End else Alignment.Start,
        modifier = Modifier.clickable(onClick = onTap)
    ) {
        Text(name,   color = CHALK.copy(alpha = 0.45f), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Text("$total", color = CHALK, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 44.sp)
    }
}

// ── Tableau Z pour une équipe ────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZBoard(
    score:    TeamScore,
    onMark:   (zone: Int) -> Unit,
    onUnmark: (zone: Int) -> Unit,
    onWys:    (Int) -> Unit,
    modifier: Modifier
) {
    Column(modifier) {
        MarkZone(score.m100, "100", { onMark(1) }, { onUnmark(1) }, Modifier.weight(1f).fillMaxWidth())
        ZLine(rightward = true)
        MarkZone(score.m50,  "50",  { onMark(2) }, { onUnmark(2) }, Modifier.weight(1f).fillMaxWidth())
        ZLine(rightward = false)
        MarkZone(score.m20,  "20",  { onMark(3) }, { onUnmark(3) }, Modifier.weight(1f).fillMaxWidth())
        HorizontalDivider(color = ORANGE.copy(alpha = 0.2f), thickness = 0.5.dp)
        WysRow(score.wys, onWys)
    }
}

// ── Ligne diagonale Z ────────────────────────────────────────────────────────

@Composable
private fun ZLine(rightward: Boolean) {
    Canvas(Modifier.fillMaxWidth().height(24.dp)) {
        val sw = 1.5.dp.toPx()
        val (a, b) = if (rightward)
            Offset(0f, 0f) to Offset(size.width, size.height)
        else
            Offset(size.width, 0f) to Offset(0f, size.height)
        drawLine(ORANGE, a, b, sw, cap = StrokeCap.Round)
    }
}

// ── Zone cliquable avec barres de tally ──────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarkZone(
    count:     Int,
    label:     String,
    onTap:     () -> Unit,
    onLongTap: () -> Unit,
    modifier:  Modifier
) {
    Box(modifier.combinedClickable(onClick = onTap, onLongClick = onLongTap).padding(horizontal = 4.dp)) {
        Text(label, color = CHALK.copy(alpha = 0.06f), fontFamily = FontFamily.Monospace, fontSize = 38.sp, modifier = Modifier.align(Alignment.Center))
        Canvas(Modifier.fillMaxSize()) { drawTally(count, CHALK) }
    }
}

// ── Dessin des barres de comptage ────────────────────────────────────────────

private fun DrawScope.drawTally(count: Int, color: Color) {
    if (count == 0) return

    val mh = minOf(size.height * 0.52f, 28.dp.toPx())
    val sw = 2.5f.dp.toPx()
    val sp = 8.dp.toPx()
    val gw = 4 * sp
    val gg = 14.dp.toPx()
    val cy = size.height / 2f
    val ox = 6.dp.toPx()

    for (i in 0 until count) {
        val g  = i / 5
        val p  = i % 5
        val gx = ox + g * (gw + gg)
        if (gx > size.width - 4.dp.toPx()) break

        if (p < 4) {
            val x = gx + p * sp
            drawLine(color, Offset(x, cy - mh / 2), Offset(x, cy + mh / 2), sw, cap = StrokeCap.Round)
        } else {
            drawLine(color, Offset(gx - sp * 0.4f, cy + mh / 2), Offset(gx + gw + sp * 0.4f, cy - mh / 2), sw, cap = StrokeCap.Round)
        }
    }
}

// ── Champ Wys ────────────────────────────────────────────────────────────────

@Composable
private fun WysRow(value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(if (value == 0) "" else "$value") }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Wys",  color = CHALK.copy(alpha = 0.35f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text(" : ",  color = ORANGE.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        BasicTextField(
            value         = text,
            onValueChange = { s -> if (s.length <= 4) { text = s; onChange(s.toIntOrNull() ?: 0) } },
            textStyle       = TextStyle(color = CHALK, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush     = SolidColor(ORANGE),
            modifier        = Modifier.weight(1f)
        )
    }
}

// ── Dialogue renommer équipe ──────────────────────────────────────────────────

@Composable
private fun RenameDialog(
    current:   String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = Color(0xFF252515),
        titleContentColor = CHALK,
        textContentColor  = CHALK,
        title = { Text("Équipe", fontFamily = FontFamily.Monospace, fontSize = 16.sp) },
        text  = {
            OutlinedTextField(
                value         = text,
                onValueChange = { if (it.length <= 12) text = it },
                singleLine    = true,
                textStyle     = TextStyle(fontFamily = FontFamily.Monospace, color = CHALK),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = ORANGE,
                    unfocusedBorderColor = CHALK.copy(alpha = 0.3f),
                    cursorColor          = ORANGE,
                    focusedTextColor     = CHALK,
                    unfocusedTextColor   = CHALK
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("OK", color = ORANGE, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CHALK.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace)
            }
        }
    )
}

// ── Dialogue saisie de manche ─────────────────────────────────────────────────

@Composable
private fun RoundDialog(
    name1:     String,
    name2:     String,
    onDismiss: () -> Unit,
    onConfirm: (score1: Int, score2: Int, multiplier: Int) -> Unit
) {
    var input        by remember { mutableStateOf("") }
    var doubled      by remember { mutableStateOf(false) }
    var countingTeam by remember { mutableStateOf(1) }
    val mult         = if (doubled) 2 else 1
    val rawInput     = input.toIntOrNull()?.takeIf { it >= 0 }

    // L'équipe qui compte entre son propre score ; l'autre reçoit maxOf(0, 157 − ce score)
    val s1raw = rawInput?.let { if (countingTeam == 1) it                else maxOf(0, 157 - it) }
    val s2raw = rawInput?.let { if (countingTeam == 1) maxOf(0, 157 - it) else it                }
    val s1eff = s1raw?.let { it * mult }
    val s2eff = s2raw?.let { it * mult }

    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = Color(0xFF252515),
        titleContentColor = CHALK,
        textContentColor  = CHALK,
        title = { Text("Manche", fontFamily = FontFamily.Monospace) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ── Qui compte ? ──────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Qui compte ?", color = CHALK.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TeamChip(name1, selected = countingTeam == 1, onClick = { countingTeam = 1; input = "" }, modifier = Modifier.weight(1f))
                        TeamChip(name2, selected = countingTeam == 2, onClick = { countingTeam = 2; input = "" }, modifier = Modifier.weight(1f))
                    }
                }

                // ── Score de l'équipe qui compte ──────────────────────
                OutlinedTextField(
                    value         = input,
                    onValueChange = { if (it.length <= 3) input = it },
                    label         = { Text(if (countingTeam == 1) name1 else name2, fontFamily = FontFamily.Monospace) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle     = TextStyle(fontFamily = FontFamily.Monospace, color = CHALK),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = ORANGE,
                        unfocusedBorderColor = CHALK.copy(alpha = 0.3f),
                        focusedLabelColor    = ORANGE,
                        unfocusedLabelColor  = CHALK.copy(alpha = 0.45f),
                        focusedTextColor     = CHALK,
                        unfocusedTextColor   = CHALK,
                        cursorColor          = ORANGE
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Pique double ×2 ───────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pique double", color = CHALK.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { doubled = !doubled },
                        border  = BorderStroke(1.5.dp, ORANGE),
                        colors  = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (doubled) ORANGE else Color.Transparent,
                            contentColor   = if (doubled) CHALK  else ORANGE
                        )
                    ) {
                        Text("×2", fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                    }
                }

                // ── Aperçu scores ─────────────────────────────────────
                if (s1eff != null && s2eff != null) {
                    val previewColor = if (doubled) ORANGE else CHALK.copy(alpha = 0.7f)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("$name1 : $s1eff", color = previewColor, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                        Text("$name2 : $s2eff", color = previewColor, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (s1raw != null && s2raw != null) onConfirm(s1raw, s2raw, mult) },
                enabled  = s1raw != null && s2raw != null
            ) {
                Text("OK", color = ORANGE, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CHALK.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace)
            }
        }
    )
}

// ── Chip sélection équipe ─────────────────────────────────────────────────────

@Composable
private fun TeamChip(
    name:     String,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        border  = BorderStroke(1.5.dp, if (selected) ORANGE else CHALK.copy(alpha = 0.2f)),
        colors  = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) ORANGE.copy(alpha = 0.15f) else Color.Transparent,
            contentColor   = if (selected) ORANGE else CHALK.copy(alpha = 0.45f)
        ),
        shape    = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(name, fontFamily = FontFamily.Monospace, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
    }
}
