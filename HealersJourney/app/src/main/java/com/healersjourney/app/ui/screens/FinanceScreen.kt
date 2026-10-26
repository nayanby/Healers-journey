package com.healersjourney.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import com.healersjourney.app.ui.components.AppButton as Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.FinanceInput
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.Dates
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.Reports
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.EmptyHint
import com.healersjourney.app.ui.components.NumberField
import com.healersjourney.app.ui.components.SectionCard
import com.healersjourney.app.ui.components.StatLine
import com.healersjourney.app.ui.components.XpProgress

/** Same green-to-red ramp the web app uses for goal progress. */
private fun financeColor(percent: Int): Color = when {
    percent >= 90 -> Color(0xFF1A9850)
    percent >= 75 -> Color(0xFF91CF60)
    percent >= 60 -> Color(0xFFD9EF8B)
    percent >= 40 -> Color(0xFFFEE08B)
    percent >= 20 -> Color(0xFFFC8D59)
    else -> Color(0xFFD73027)
}

@Composable
fun FinanceScreen(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    var bank by remember(state.bankBalance) { mutableStateOf(GameEngine.numText(state.bankBalance)) }
    var savings by remember(state.savings) { mutableStateOf(GameEngine.numText(state.savings)) }
    var emergency by remember(state.emergencyFund) { mutableStateOf(GameEngine.numText(state.emergencyFund)) }
    var investments by remember(state.investments) { mutableStateOf(GameEngine.numText(state.investments)) }
    var bankGoal by remember(state.bankGoal) { mutableStateOf(GameEngine.numText(state.bankGoal)) }
    var savingsGoal by remember(state.savingsGoal) { mutableStateOf(GameEngine.numText(state.savingsGoal)) }
    var emergencyGoal by remember(state.emergencyGoal) { mutableStateOf(GameEngine.numText(state.emergencyGoal)) }
    var investmentsGoal by remember(state.investmentsGoal) { mutableStateOf(GameEngine.numText(state.investmentsGoal)) }
    var monthly by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        SectionCard(title = "Goals") {
            GoalRow("Bank balance", state.bankBalance, state.bankGoal)
            GoalRow("Savings", state.savings, state.savingsGoal)
            GoalRow("Emergency fund", state.emergencyFund, state.emergencyGoal)
            GoalRow("Investments", state.investments, state.investmentsGoal)
        }

        SectionCard(title = "Update amounts", collapsible = true, initiallyExpanded = false) {
            AmountRow("Bank balance", bank, { bank = it }, "Bank goal", bankGoal, { bankGoal = it })
            AmountRow("Savings", savings, { savings = it }, "Savings goal", savingsGoal, { savingsGoal = it })
            AmountRow(
                "Emergency fund", emergency, { emergency = it },
                "Emergency goal", emergencyGoal, { emergencyGoal = it }
            )
            AmountRow(
                "Investments", investments, { investments = it },
                "Investments goal", investmentsGoal, { investmentsGoal = it }
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.runQuiet {
                        GameEngine.updateFinance(
                            it,
                            FinanceInput(
                                bankBalance = bank.toDoubleOrNull(),
                                savings = savings.toDoubleOrNull(),
                                emergencyFund = emergency.toDoubleOrNull(),
                                investments = investments.toDoubleOrNull(),
                                savingsGoal = savingsGoal.toDoubleOrNull(),
                                bankGoal = bankGoal.toDoubleOrNull(),
                                emergencyGoal = emergencyGoal.toDoubleOrNull(),
                                investmentsGoal = investmentsGoal.toDoubleOrNull()
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save amounts and goals") }
        }

        SectionCard(title = "Monthly investment") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumberField(
                    value = monthly,
                    onValueChange = { monthly = it },
                    label = "Amount invested",
                    decimal = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(AppLayout.contentGap))
                Button(onClick = {
                    val amount = monthly.toDoubleOrNull() ?: 0.0
                    viewModel.runQuiet {
                        GameEngine.updateFinance(it, FinanceInput(monthlyInvestment = amount))
                    }
                    monthly = ""
                }) { Text("Record") }
            }
            Spacer(Modifier.height(AppLayout.contentGap))
            StatLine(
                "Invested this month",
                Rules.money(GameEngine.monthlyInvestmentDone(state)),
                MaterialTheme.colorScheme.primary
            )
            Text(
                "Record contributions toward your monthly goal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(title = "Recent finance log", collapsible = true, initiallyExpanded = false) {
            val entries = state.financeLog.take(12)
            if (entries.isEmpty()) {
                EmptyHint("No finance updates recorded yet.")
            } else {
                entries.forEach { entry ->
                    Text(
                        com.healersjourney.app.domain.FriendlyLog.entry(entry, state),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                StatLine("Total finance entries", "${state.financeLog.size}")
                StatLine("Entries this month", "${Reports.financeUpdatesForMonthKey(state, Dates.monthKeyOfNow()).size}")
            }
        }
    }
}

@Composable
private fun GoalRow(label: String, value: Double, goal: Double) {
    val percent = GameEngine.financePercent(value, goal)
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                "${Rules.money(value)} / ${Rules.money(goal)} • $percent%",
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(Modifier.height(4.dp))
        XpProgress(progress = percent / 100f, label = "", color = financeColor(percent))
    }
}

@Composable
private fun AmountRow(
    valueLabel: String,
    value: String,
    onValue: (String) -> Unit,
    goalLabel: String,
    goal: String,
    onGoal: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        NumberField(value = value, onValueChange = onValue, label = valueLabel, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        NumberField(value = goal, onValueChange = onGoal, label = goalLabel, modifier = Modifier.weight(1f))
    }
}
