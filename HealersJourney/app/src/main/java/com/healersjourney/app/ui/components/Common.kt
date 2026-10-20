package com.healersjourney.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.healersjourney.app.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun skillColor(skill: String): Color = when (skill) {
    "knowledgeXp" -> Color(0xFF4F6DF5)
    "medicineXp" -> Color(0xFFE05656)
    "strengthXp" -> Color(0xFF2FA36B)
    "socialXp" -> Color(0xFFE08B3A)
    "hobbiesXp" -> Color(0xFF8B5CF6)
    else -> Color(0xFF8FB8DE)
}

/** A plain progress track: an empty value stays visibly empty. */
@Composable
fun SimpleProgress(progress: () -> Float, modifier: Modifier = Modifier,
                   color: Color = MaterialTheme.colorScheme.primary,
                   trackColor: Color = MaterialTheme.colorScheme.outlineVariant) {
    val value = progress().coerceIn(0f, 1f)
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(trackColor)
        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) }) {
        if (value > 0f) Box(Modifier.fillMaxWidth(value).height(8.dp).background(color))
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    leadingIcon: Int? = null,
    collapsible: Boolean = false,
    initiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(AppLayout.cardRadius),

        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = if (collapsible) 8.dp else 20.dp)) {
            Row(Modifier.fillMaxWidth().then(if (collapsible) Modifier.heightIn(min = 48.dp).semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }.clickable(role = Role.Button) { expanded = !expanded } else Modifier), verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.let { resource ->
                    androidx.compose.material3.Icon(painterResource(resource), contentDescription = null,
                        modifier = Modifier.size(AppLayout.icon), tint = Color.Unspecified)
                    Spacer(Modifier.width(AppLayout.smallGap))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                trailing?.invoke()
                if (collapsible) {
                    Text(if (expanded) "Less" else "Open", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Image(painterResource(if (expanded) R.drawable.color_up else R.drawable.color_down), null, Modifier.size(16.dp))
                }
            }
            if (!collapsible || expanded) {
                Spacer(Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
fun StatLine(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(AppLayout.contentGap), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
fun XpProgress(progress: Float, label: String, color: Color = MaterialTheme.colorScheme.primary) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(4.dp))
        SimpleProgress(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.Transparent,

        )
    }
}

/** Freshness ramp shared by attribute and care rings: 4 = logged recently, 0 = neglected. */
fun freshnessColor(level: Int): Color = when (level) {
    4 -> Color(0xFF1A9850)
    3 -> Color(0xFF91CF60)
    2 -> Color(0xFFFEE08B)
    1 -> Color(0xFFFC8D59)
    else -> Color(0xFFD73027)
}

/**
 * Ring that empties clockwise as an item goes stale. At level 0 the track takes the red tint so a
 * fully neglected item reads as critical instead of looking like missing artwork.
 */
@Composable
fun FreshnessRing(level: Int, modifier: Modifier = Modifier, strokeWidth: Dp = 3.5.dp) {
    val color = freshnessColor(level)
    val track = if (level <= 0) color.copy(alpha = 0.32f) else MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        val stroke = strokeWidth.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2f, stroke / 2f)
        drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
        drawArc(
            color, -90f, 360f * (level.coerceIn(0, 4) / 4f), false,
            topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun HourBlocks(filled: Int, total: Int, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        (0 until total).forEach { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(14.dp)
                    .background(
                        if (index < filled) color else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

/** Monday-first strip of 7 day columns, each an hour stack capped at [blocks] plus a caption. */
@Composable
fun WeekGrid(
    keys: List<String>,
    values: List<Double>,
    color: Color,
    blocks: Int,
    caption: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val today = com.healersjourney.app.domain.Dates.todayKey()
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        keys.forEachIndexed { index, key ->
            val value = values.getOrElse(index) { 0.0 }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DAY_LABELS.getOrElse(index) { "" }, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                HourBlocks(
                    filled = value.coerceIn(0.0, blocks.toDouble()).toInt(),
                    total = blocks,
                    color = color,
                    modifier = Modifier.height(14.dp).fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    caption(value),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (key == today) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Current month as a 7-column Monday-first grid; [offset] is the leading blank count. */
@Composable
fun MonthCalendar(
    cells: List<Pair<String, Double>>,
    offset: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val today = com.healersjourney.app.domain.Dates.todayKey()
    val padded = List(offset) { "" to 0.0 } + cells
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DAY_LABELS.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        padded.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { (key, value) ->
                    val hasData = value > 0
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 32.dp)
                            .background(
                                when {
                                    key.isEmpty() -> Color.Transparent
                                    hasData -> color.copy(alpha = (0.25 + (value / 8.0).coerceIn(0.0, 1.0) * 0.6).toFloat())
                                    key == today -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                },
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (key.isNotEmpty()) {
                            Text(
                                key.takeLast(2).trimStart('0').ifEmpty { "0" },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasData) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Box(Modifier.weight(1f).heightIn(min = 32.dp)) }
            }
        }
    }
}

@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() || (decimal && it == '.') }
            onValueChange(filtered)
        },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.heightIn(min = AppLayout.controlHeight),
        shape = RoundedCornerShape(AppLayout.controlRadius),
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number)
    )
}

@Composable
fun SpacerRow(height: Int = 12) {
    Spacer(Modifier.height(height.dp))
}

@Composable
fun SpacerColumn(width: Int = 8) {
    Spacer(Modifier.width(width.dp))
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
fun SizedSpacer(size: Int) {
    Box(Modifier.size(size.dp))
}
