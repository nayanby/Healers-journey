package com.healersjourney.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.healersjourney.app.ui.components.AppButton as Button
import com.healersjourney.app.ui.components.AppTonalButton as FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.Reports
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.Art
import com.healersjourney.app.ui.components.HourBlocks
import com.healersjourney.app.ui.components.MonthCalendar
import com.healersjourney.app.ui.components.NumberField
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine
import com.healersjourney.app.ui.components.WeekGrid
import com.healersjourney.app.ui.components.skillColor
import kotlinx.coroutines.delay

@Composable
fun StudyScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    val summary = Reports.studySummary(state)
    val color = skillColor("knowledgeXp")

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        StudyEntryCard(snapshot, viewModel)
        SkillActivityCard(snapshot, viewModel, "knowledgeXp")

        SectionCard(title = "This week") {
            val series = Reports.weeklyStudySeries(state)
            WeekGrid(
                keys = series.map { it.first },
                values = series.map { it.second },
                color = color,
                blocks = 8,
                caption = { "${GameEngine.numText(it)}h" }
            )
            Spacer(Modifier.height(12.dp))
            StatLine("Week total", "${GameEngine.numText(summary.weeklyHours)} / 56 h")
            StatLine("Study streak", "${GameEngine.studyStreak(state)} days", MaterialTheme.colorScheme.primary)
            StatLine("Study XP this week", "${summary.weeklyXp} / ${Rules.WEEKLY_TARGETS["knowledgeXp"]}")
        }

        SectionCard(title = "This month", collapsible = true, initiallyExpanded = false) {
            Text(
                "${Dates.monthName(Dates.monthOfNowZeroBased() + 1)} ${Dates.yearOfNow()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(AppLayout.contentGap))
            val (cells, offset) = Reports.monthlyStudyCalendar(state)
            MonthCalendar(cells = cells, offset = offset, color = color)
            Spacer(Modifier.height(12.dp))
            StatLine("Month hours", "${GameEngine.numText(summary.monthlyHours)} h")
            StatLine("Month Study XP", "${summary.monthlyXp}")
        }

        SectionCard(title = "Lifetime", collapsible = true, initiallyExpanded = false) {
            StatLine("Total study hours", "${GameEngine.numText(summary.lifetimeHours)} h")
            StatLine("Study days", "${summary.entries}")
            StatLine("Best single day", "${GameEngine.numText(summary.bestStudy)} h")
            StatLine("Average per day", "${GameEngine.numText(summary.averageStudy)} h")
            StatLine("Lifetime Study XP", "${summary.lifetimeXp}")

        }


    }
}

@Composable
private fun StudyEntryCard(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    val todayHours = GameEngine.studyHoursToday(state)
    val remaining = (GameEngine.STUDY_CYCLE_CAP - todayHours).coerceAtLeast(0.0)
    val color = skillColor("knowledgeXp")
    var hours by remember { mutableStateOf("1") }
    var millisLeft by remember { mutableStateOf(Dates.millisUntilFiveAm()) }

    LaunchedEffect(Unit) {
        while (true) {
            millisLeft = Dates.millisUntilFiveAm()
            delay(60_000L)
        }
    }

    SectionCard(title = "Study today") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Art.streakStudy),
                contentDescription = null,
                modifier = Modifier.size(AppLayout.illustration)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${GameEngine.numText(todayHours)} / ${GameEngine.STUDY_CYCLE_CAP} h today",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${GameEngine.numText(remaining)} h left to log • cycle resets in ${Dates.countdownText(millisLeft)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(AppLayout.contentGap))
        HourBlocks(
            filled = todayHours.toInt(),
            total = 8,
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberField(
                value = hours,
                onValueChange = { hours = it },
                label = "Hours",
                decimal = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(AppLayout.contentGap))
            Button(
                onClick = { viewModel.run { GameEngine.addStudySession(it, hours.toDoubleOrNull() ?: 0.0) } },
                enabled = remaining > 0
            ) { Text("Log study") }
        }

    }
}
