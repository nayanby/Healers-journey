package com.healersjourney.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healersjourney.app.Destination
import com.healersjourney.app.R
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.DriveUiState
import com.healersjourney.app.data.games.PlayGamesUiState
import com.healersjourney.app.ui.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(snapshot: GameSnapshot, viewModel: GameViewModel, onOpen: (Destination) -> Unit) {
    WorldHome(snapshot, viewModel, onOpen)
}

@Composable
fun ExploreScreen(snapshot: GameSnapshot, viewModel: GameViewModel, onOpen: (Destination) -> Unit) {
    var section by rememberSaveable { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        TabRow(selectedTabIndex = section) {
            Tab(selected = section == 0, onClick = { section = 0 }, text = { Text("Your journey") })
            Tab(selected = section == 1, onClick = { section = 1 }, text = { Text("My thoughts") })
        }
        if (section == 1) { MyThoughtsScreen(snapshot, viewModel); return@Column }
        JourneyHeading("Your story so far", "Look back at what you've done and plan ahead.")
        ExploreGroup("Your journey", listOf(
            Triple(Destination.Logs, R.drawable.color_logs, "Activity history & insights"),
            Triple(Destination.Milestones, R.drawable.color_milestone, "Goals & milestones")
        ), onOpen)
        ExploreGroup("More tools", listOf(
            Triple(Destination.Finance, R.drawable.color_finance, "Money & goals")
        ), onOpen)
    }
}

@Composable
fun SettingsScreen(snapshot: GameSnapshot, viewModel: GameViewModel, drive: DriveUiState, onSignIn: () -> Unit,
                   gamesState: PlayGamesUiState, onGamesSignIn: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppLayout.sectionGap)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Your character", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("male" to "Male", "female" to "Female").forEach { (key, label) ->
                    val selected = (snapshot.state.worldEquipment["character"] ?: "male") == key
                    Surface(Modifier.weight(1f).clickable { viewModel.runQuiet { it.worldEquipment["character"] = key } },
                        shape = RoundedCornerShape(18.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Image(gameArtPainter(GameArt.avatar("traveler", key), 128), null, Modifier.size(52.dp))
                            Text(label, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            }
        }
        SectionCard(title = "Google Play Games") {
            Text(when {
                gamesState.busy -> "Connecting…"
                gamesState.signedIn -> gamesState.playerName?.let { "Connected as $it" } ?: "Connected"
                else -> "Connect your Play Games profile."
            }, style = MaterialTheme.typography.bodyMedium)
            if (!gamesState.signedIn) {
                Text("You can keep playing without signing in.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                gamesState.message?.let { Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
                AppButton(onClick = onGamesSignIn, enabled = !gamesState.busy) {
                    Text(if (gamesState.busy) "Connecting…" else "Sign in with Play Games")
                }
            }
        }
        DataScreen(snapshot, viewModel, drive, onSignIn)
    }
}

@Composable
private fun ExploreGroup(title: String, entries: List<Triple<Destination, Int, String>>, onOpen: (Destination) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column {
                entries.forEachIndexed { index, (destination, icon, label) ->
                    Row(Modifier.fillMaxWidth().clickable { onOpen(destination) }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Image(painterResource(icon), null, Modifier.size(36.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Image(painterResource(R.drawable.color_right), null, Modifier.size(16.dp))
                    }
                    if (index < entries.lastIndex) HorizontalDivider(Modifier.padding(start = 66.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GrowthTile(title: String, icon: Int, detail: String, accent: Color, progress: Float?, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icon), null, Modifier.size(46.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (progress != null) SimpleProgress(progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = accent, trackColor = accent.copy(alpha = 0.12f))
            else Spacer(Modifier.height(4.dp))

        }
    }
}

@Composable
private fun JourneyHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun JourneyAction(icon: Int, title: String, detail: String, accent: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                Image(painterResource(icon), null, Modifier.size(40.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Image(painterResource(R.drawable.color_right), null, Modifier.size(16.dp))
        }
    }
}
