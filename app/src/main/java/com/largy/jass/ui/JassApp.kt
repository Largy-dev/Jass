package com.largy.jass.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.largy.jass.GameViewModel
import com.largy.jass.TeamScore

private val BG     = Color(0xFF1A1A14)
private val ORANGE = Color(0xFFCC4400)
private val CHALK  = Color(0xFFF0EDD5)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JassApp(vm: GameViewModel = viewModel()) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(BG)
    ) {
        Column(Modifier.fillMaxSize()) {

            // ── Header : noms + scores ────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreDisplay(vm.team1Name, vm.team1.total)
                TextButton(onClick = { vm.reset() }) {
                    Text("✕", color = CHALK.copy(alpha = 0.25f), fontSize = 18.sp)
                }
                ScoreDisplay(vm.team2Name, vm.team2.total, rightAlign = true)
            }

            HorizontalDivider(color = ORANGE.copy(alpha = 0.55f), thickness = 1.dp)

            // ── Deux tableaux côte à côte ─────────────────────────────────
            Row(Modifier.weight(1f).fillMaxWidth()) {

                ZBoard(
                    score = vm.team1,
                    onMark   = { vm.mark(1, it)   },
                    onUnmark = { vm.unmark(1, it) },
                    onWys    = { vm.setWys(1, it) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                Box(Modifier.width(1.5.dp).fillMaxHeight().background(ORANGE.copy(alpha = 0.55f)))

                ZBoard(
                    score = vm.team2,
                    onMark   = { vm.mark(2, it)   },
                    onUnmark = { vm.unmark(2, it) },
                    onWys    = { vm.setWys(2, it) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // ── Bouton manche ─────────────────────────────────────────────
            HorizontalDivider(color = ORANGE.copy(alpha = 0.55f), thickness = 1.dp)
            Box(Modifier.fillMaxWidth().padding(10.dp), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ORANGE, contentColor = CHALK)
                ) {
                    Text("+ Manche", fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                }
            }
        }
    }

    if (showDialog) {
        RoundDialog(
            name1 = vm.team1Name,
            name2 = vm.team2Name,
            onDismiss = { showDialog = false },
            onConfirm = { s, mult -> vm.addRound(s, mult); showDialog = false }
        )
    }
}

// ── Score dans le header ─────────────────────────────────────────────────────

@Composable
private fun ScoreDisplay(name: String, total: Int, rightAlign: Boolean = false) {
    Column(horizontalAlignment = if (rightAlign) Alignment.End else Alignment.Start) {
        Text(name,   color = CHALK.copy(alpha = 0.55f), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        Text("$total", color = CHALK,                     fontFamily = FontFamily.Monospace, fontSize = 34.sp)
    }
}

// ── Tableau Z pour une équipe ────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZBoard(
    score: TeamScore,
    onMark:   (zone: Int) -> Unit,
    onUnmark: (zone: Int) -> Unit,
    onWys:    (Int) -> Unit,
    modifier: Modifier
) {
    Column(modifier) {

        // Zone 100
        MarkZone(
            count    = score.m100,
            label    = "100",
            onTap    = { onMark(1)   },
            onLongTap= { onUnmark(1) },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        ZLine(rightward = true)

        // Zone 50
        MarkZone(
            count    = score.m50,
            label    = "50",
            onTap    = { onMark(2)   },
            onLongTap= { onUnmark(2) },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        ZLine(rightward = false)

        // Zone 20
        MarkZone(
            count    = score.m20,
            label    = "20",
            onTap    = { onMark(3)   },
            onLongTap= { onUnmark(3) },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        // Ligne Wys
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
    Box(
        modifier
            .combinedClickable(onClick = onTap, onLongClick = onLongTap)
            .padding(horizontal = 4.dp)
    ) {
        // Étiquette de valeur fantôme
        Text(
            label,
            color    = CHALK.copy(alpha = 0.06f),
            fontFamily = FontFamily.Monospace,
            fontSize = 38.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        // Barres dessinées sur Canvas
        Canvas(Modifier.fillMaxSize()) { drawTally(count, CHALK) }
    }
}

// ── Dessin des barres de comptage ────────────────────────────────────────────

private fun DrawScope.drawTally(count: Int, color: Color) {
    if (count == 0) return

    val mh  = minOf(size.height * 0.52f, 28.dp.toPx())  // hauteur d'une barre
    val sw  = 2.5f.dp.toPx()                              // épaisseur trait
    val sp  = 8.dp.toPx()                                 // écart entre barres du groupe
    val gw  = 4 * sp                                       // largeur d'un groupe de 4
    val gg  = 14.dp.toPx()                                // écart entre groupes
    val cy  = size.height / 2f
    val ox  = 6.dp.toPx()                                 // offset gauche

    for (i in 0 until count) {
        val g = i / 5
        val p = i % 5
        val gx = ox + g * (gw + gg)
        if (gx > size.width - 4.dp.toPx()) break          // hors limites

        if (p < 4) {
            // Barre verticale
            val x = gx + p * sp
            drawLine(color, Offset(x, cy - mh / 2), Offset(x, cy + mh / 2), sw, cap = StrokeCap.Round)
        } else {
            // Barre diagonale qui barre les 4 précédentes
            drawLine(
                color,
                Offset(gx - sp * 0.4f,      cy + mh / 2),
                Offset(gx + gw + sp * 0.4f, cy - mh / 2),
                sw, cap = StrokeCap.Round
            )
        }
    }
}

// ── Champ Wys ────────────────────────────────────────────────────────────────

@Composable
private fun WysRow(value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(if (value == 0) "" else "$value") }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Wys:", color = CHALK.copy(alpha = 0.35f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = text,
            onValueChange = { s ->
                if (s.length <= 4) {
                    text = s
                    onChange(s.toIntOrNull() ?: 0)
                }
            },
            textStyle  = TextStyle(color = CHALK, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(ORANGE),
            modifier   = Modifier.weight(1f)
        )
    }
}

// ── Dialogue saisie de manche ─────────────────────────────────────────────────

@Composable
private fun RoundDialog(
    name1: String,
    name2: String,
    onDismiss: () -> Unit,
    onConfirm: (score1: Int, multiplier: Int) -> Unit
) {
    var input   by remember { mutableStateOf("") }
    var doubled by remember { mutableStateOf(false) }
    val mult   = if (doubled) 2 else 1
    val s1raw  = input.toIntOrNull()?.takeIf { it >= 0 }
    val s2raw  = s1raw?.let { maxOf(0, 157 - it) }
    val s1eff  = s1raw?.let { it * mult }
    val s2eff  = s2raw?.let { it * mult }

    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = Color(0xFF252515),
        titleContentColor = CHALK,
        textContentColor  = CHALK,
        title = { Text("Manche", fontFamily = FontFamily.Monospace) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = input,
                    onValueChange = { if (it.length <= 3) input = it },
                    label    = { Text(name1, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, color = CHALK),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = ORANGE,
                        unfocusedBorderColor = CHALK.copy(alpha = 0.3f),
                        focusedLabelColor    = ORANGE,
                        unfocusedLabelColor  = CHALK.copy(alpha = 0.45f),
                        focusedTextColor     = CHALK,
                        unfocusedTextColor   = CHALK,
                        cursorColor          = ORANGE
                    )
                )

                // ── Bouton Pique double ×2 ────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Pique double",
                        color = CHALK.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { doubled = !doubled },
                        border = BorderStroke(1.5.dp, ORANGE),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (doubled) ORANGE else Color.Transparent,
                            contentColor   = if (doubled) CHALK  else ORANGE
                        )
                    ) {
                        Text("×2", fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                    }
                }

                // ── Scores effectifs ──────────────────────────────────────
                if (s1eff != null && s2eff != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "$name1 : $s1eff",
                            color = if (doubled) ORANGE else CHALK.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp
                        )
                        Text(
                            "$name2 : $s2eff",
                            color = CHALK.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { s1raw?.let { onConfirm(it, mult) } }, enabled = s1raw != null) {
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
