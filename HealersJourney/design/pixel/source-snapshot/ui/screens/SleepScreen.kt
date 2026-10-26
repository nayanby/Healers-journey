package com.healersjourney.app.ui.screens

import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.healersjourney.app.ui.components.AppButton as Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.domain.SleepStats
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.Art
import com.healersjourney.app.ui.components.MonthCalendar
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine
import com.healersjourney.app.ui.components.WeekGrid

/** Red under 5 h, yellow 5–6.9 h, green from 7 h — the web app's sleep quality bands. */
fun sleepColor(hours: Double): Color = when {
    hours <= 0 -> Color(0xFF6B7280)
    hours < 5 -> Color(0xFFD73027)
    hours < 7 -> Color(0xFFFEE08B)
    else -> Color(0xFF1A9850)
}

@Composable
fun SleepScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    val stats = SleepStats.of(state)
    val previousNight = GameEngine.previousNightHours(state)
    var hours by remember { mutableFloatStateOf(if (previousNight > 0) previousNight.toFloat() else 6f) }
    val selected = hours.toDouble()
    val xp = GameEngine.sleepXpForHours(selected)

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = "Sleep tracker") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Art.quickCare),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${GameEngine.numText(selected)} hours",
                        style = MaterialTheme.typography.titleMedium,
                        color = sleepColor(selected)
                    )
                    Text(
                        "Logged against ${Dates.prettyCareDate(Dates.previousNightKeyOfNow())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Slider(
                value = hours,
                onValueChange = { hours = it },
                valueRange = 1f..12f,
                steps = 21,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth()) {
                Text("1 h", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("12 h", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.runQuiet { GameEngine.completeSleepQuest(it, selected) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Log previous night • +$xp XP") }
            Spacer(Modifier.height(8.dp))
            Text(
                "Sleep XP: ≤2 h = 5, 3 h = 10, 4 h = 20, 5 h = 30, 6 h = 40, 7 h = 55, 8 h+ = 70. Sleep does not decay other attributes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(title = "This week") {
            val series = SleepStats.weeklySeries(state)
            WeekGrid(
                keys = series.map { it.first },
                values = series.map { it.second },
                color = sleepColor(stats.averageSleep),
                blocks = 8,
                caption = { if (it > 0) "${GameEngine.numText(it)}h" else "--" }
            )
            Spacer(Modifier.height(12.dp))
            StatLine("Week total", Rules.formatMetricValue(stats.weeklyHours, " h"))
            StatLine("Week XP", "${stats.weeklyXp}")
        }

        SectionCard(title = "This month", collapsible = true, initiallyExpanded = false) {
            Text(
                "${Dates.monthName(Dates.monthOfNowZeroBased() + 1)} ${Dates.yearOfNow()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(AppLayout.contentGap))
            val (cells, offset) = SleepStats.monthlySeries(state)
            MonthCalendar(cells = cells, offset = offset, color = sleepColor(stats.averageSleep))
            Spacer(Modifier.height(12.dp))
            StatLine("Month hours", Rules.formatMetricValue(stats.monthlyHours, " h"))
            StatLine("Month entries", "${stats.monthlyEntries}")
            StatLine("Month XP", "${stats.monthlyXp}")
        }

        SectionCard(title = "Lifetime", collapsible = true, initiallyExpanded = false) {
            StatLine("Last sleep", Rules.formatMetricValue(state.lastSleepHours, " h"))
            StatLine("Previous night logged", Rules.formatMetricValue(previousNight, " h"))
            StatLine("Lifetime sleep", Rules.formatMetricValue(stats.lifetimeHours, " h"))
            StatLine("Sleep entries", "${stats.entries}")
            StatLine("Average logged sleep", Rules.formatMetricValue(stats.averageSleep, " h"))
            StatLine("Best single night", Rules.formatMetricValue(stats.bestSleep, " h"))
            StatLine("Lifetime sleep XP", "${stats.lifetimeXp}")
        }
    }
}
