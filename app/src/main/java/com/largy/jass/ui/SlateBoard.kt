package com.largy.jass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.largy.jass.R
import com.largy.jass.game.TeamScore
import com.largy.jass.game.Zone
import com.largy.jass.ui.theme.Slate
import kotlin.math.floor
import kotlin.math.hypot

/** Une moitié d'ardoise : numéro + nom, score, grand Z avec barres, retenue et progression. */
@Composable
fun TeamSlate(
    number: Int,
    name: String,
    score: TeamScore,
    goal: Int?,
    isWinner: Boolean,
    onRename: () -> Unit,
    onMark: (Zone) -> Unit,
    onUnmark: (Zone) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onRename).padding(vertical = 4.dp),
            ) {
                Text("$number", color = Slate.Accent, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(name, color = Slate.ChalkMuted, fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${score.total}",
                color = if (isWinner) Slate.Accent else Slate.Chalk,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 38.sp,
            )
        }

        ZSlate(score, name, onMark, onUnmark, Modifier.weight(1f).fillMaxWidth())

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                if (goal != null) GoalProgress(score.total.toFloat() / goal)
            }
            Text(
                if (score.retenue > 0) stringResource(R.string.retenue, score.retenue) else stringResource(R.string.retenue_none),
                color = if (score.retenue > 0) Slate.Accent else Slate.ChalkDim,
                fontWeight = if (score.retenue > 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun GoalProgress(fraction: Float) {
    Box(Modifier.fillMaxWidth().height(3.dp).background(Slate.ChalkFaint)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(Slate.Line))
    }
}

// ── Le Z ─────────────────────────────────────────────────────────────────────

/** Tap sur un trait = +1 barre, appui long = −1 barre. Le trait visé est le plus proche du doigt. */
@Composable
private fun ZSlate(
    score: TeamScore,
    name: String,
    onMark: (Zone) -> Unit,
    onUnmark: (Zone) -> Unit,
    modifier: Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val measurer = rememberTextMeasurer()
    val mark by rememberUpdatedState(onMark)
    val unmark by rememberUpdatedState(onUnmark)

    val description = stringResource(
        R.string.slate_description, name, score.total, score.m100, score.m50, score.m20, score.retenue,
    )
    val actions = Zone.entries.flatMap { z ->
        listOf(
            CustomAccessibilityAction(stringResource(R.string.action_add, z.value)) { onMark(z); true },
            CustomAccessibilityAction(stringResource(R.string.action_remove, z.value)) { onUnmark(z); true },
        )
    }

    Canvas(
        modifier
            .semantics {
                contentDescription = description
                customActions = actions
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { p ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        mark(zGeometry(size.width.toFloat(), size.height.toFloat(), density).nearest(p))
                    },
                    onLongPress = { p ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        unmark(zGeometry(size.width.toFloat(), size.height.toFloat(), density).nearest(p))
                    },
                )
            }
    ) {
        val z = zGeometry(size.width, size.height, density)
        val lineWidth = 2.dp.toPx()
        Zone.entries.forEach { zone ->
            val (a, b) = z.segment(zone)
            drawLine(Slate.Chalk.copy(alpha = 0.85f), a, b, lineWidth, StrokeCap.Round)
        }
        drawZoneLabels(z, measurer)
        Zone.entries.forEach { zone ->
            val (a, b) = z.segment(zone)
            drawTally(a, b, score.count(zone), measurer)
        }
    }
}

/** Géométrie du Z dans une zone de dessin : trait du haut (100), diagonale (50), trait du bas (20). */
private class ZGeometry(val left: Float, val right: Float, val top: Float, val bottom: Float) {
    fun segment(zone: Zone): Pair<Offset, Offset> = when (zone) {
        Zone.HUNDRED -> Offset(left, top) to Offset(right, top)
        Zone.FIFTY -> Offset(right, top) to Offset(left, bottom)
        Zone.TWENTY -> Offset(left, bottom) to Offset(right, bottom)
    }

    fun nearest(p: Offset): Zone = Zone.entries.minBy { z ->
        val (a, b) = segment(z)
        distanceToSegment(p, a, b)
    }
}

private fun zGeometry(w: Float, h: Float, density: Float): ZGeometry {
    val padX = 12f * density
    val padY = minOf(22f * density, h * 0.18f)
    return ZGeometry(padX, w - padX, padY, h - padY)
}

private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
    val ab = b - a
    val len2 = ab.x * ab.x + ab.y * ab.y
    if (len2 == 0f) return (p - a).getDistance()
    val t = (((p.x - a.x) * ab.x + (p.y - a.y) * ab.y) / len2).coerceIn(0f, 1f)
    return (p - (a + ab * t)).getDistance()
}

// ── Dessin ───────────────────────────────────────────────────────────────────

private val labelStyle = TextStyle(color = Slate.Chalk.copy(alpha = 0.18f), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
private val badgeStyle = TextStyle(color = Slate.Accent, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold)

private fun DrawScope.drawZoneLabels(z: ZGeometry, measurer: TextMeasurer) {
    val gap = 6.dp.toPx()
    val l100 = measurer.measure("100", labelStyle)
    drawText(l100, topLeft = Offset(z.right - l100.size.width, z.top + gap))
    val l50 = measurer.measure("50", labelStyle)
    drawText(l50, topLeft = Offset(z.left, (z.top + z.bottom) / 2f - l50.size.height / 2f))
    val l20 = measurer.measure("20", labelStyle)
    drawText(l20, topLeft = Offset(z.left, z.bottom - gap - l20.size.height))
}

/**
 * Barres de comptage perpendiculaires au trait [a]→[b], par groupes de 5 (4 barres + 1 qui barre).
 * L'espacement se resserre pour tenir ; au-delà, les barres restantes s'affichent en « +N ».
 */
private fun DrawScope.drawTally(a: Offset, b: Offset, count: Int, measurer: TextMeasurer) {
    if (count <= 0) return
    val len = hypot(b.x - a.x, b.y - a.y)
    val dir = (b - a) / len
    val normal = Offset(-dir.y, dir.x)
    val margin = 22.dp.toPx()
    val usable = len - 2 * margin
    val half = 13.dp.toPx()
    val stroke = 2.5.dp.toPx()
    val groupUnits = 3f + GROUP_GAP          // largeur d'un groupe + espace, en unités d'espacement

    val groups = (count + 4) / 5
    var sp = minOf(9.dp.toPx(), usable / (groups * groupUnits - GROUP_GAP))
    var shown = count
    if (sp < 4.dp.toPx()) {
        sp = 4.dp.toPx()
        val fit = maxOf(1, floor((usable + GROUP_GAP * sp) / (groupUnits * sp)).toInt() - 1) // 1 place pour le badge
        shown = fit * 5
    }

    fun at(d: Float) = a + dir * (margin + d)
    for (i in 0 until shown) {
        val start = (i / 5) * groupUnits * sp
        val p = i % 5
        if (p < 4) {
            val c = at(start + p * sp)
            drawLine(Slate.Chalk, c - normal * half, c + normal * half, stroke, StrokeCap.Round)
        } else {
            drawLine(Slate.Chalk, at(start - sp * 0.5f) + normal * half, at(start + sp * 3.5f) - normal * half, stroke, StrokeCap.Round)
        }
    }

    if (shown < count) {
        val text = measurer.measure("+${count - shown}", badgeStyle)
        val c = at((shown / 5) * groupUnits * sp + sp)
        val pad = 3.dp.toPx()
        val tl = Offset(c.x - text.size.width / 2f, c.y - text.size.height / 2f)
        drawRoundRect(
            Slate.Background,
            topLeft = tl - Offset(pad, pad),
            size = Size(text.size.width + 2 * pad, text.size.height + 2 * pad),
            cornerRadius = CornerRadius(pad * 2),
        )
        drawText(text, topLeft = tl)
    }
}

private const val GROUP_GAP = 2f
