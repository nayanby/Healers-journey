package com.healersjourney.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.components.*

/** A dated, interactive display of actual completions; nothing is earned by merely planning. */
@Composable
@NonSkippableComposable
fun DayShowcase(state: GameState, day: String, revision: Int) {
    val entries = remember(revision, day) { DailyShowcase.entries(state, day) }
    var page by rememberSaveable(day) { mutableIntStateOf(0) }
    val pages = maxOf(1, (entries.size + 7) / 8)
    val currentPage = page.coerceIn(0, pages - 1)
    val visible = entries.drop(currentPage * 8).take(8)
    var knownIds by remember(day) { mutableStateOf(entries.map { it.id }.toSet()) }
    LaunchedEffect(day, entries.map { it.id }) {
        val added = entries.lastOrNull { it.id !in knownIds }
        if(added != null) {
            page = entries.indexOfFirst { it.id == added.id } / 8
        }
        knownIds = entries.map { it.id }.toSet()
    }
    Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF233B4B)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Your daily badge shelf", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFE1A5))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Coins earned today", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6E2E8))
                Image(gameArtPainter(com.healersjourney.app.R.drawable.gameart_icon_coin), null, Modifier.size(22.dp))
                Text("${DailyCoinBonus.earnedToday(state, day)}", style = MaterialTheme.typography.titleSmall, color = Color(0xFFFFE1A5))
            }
            BoxWithConstraints(Modifier.fillMaxWidth(.82f).align(Alignment.CenterHorizontally).aspectRatio(4f/3f)) {
                val w = maxWidth
                val h = maxHeight
                Image(gameArtPainter(com.healersjourney.app.R.drawable.gameart_trophy_shelf), null,
                    Modifier.fillMaxSize(), contentScale=androidx.compose.ui.layout.ContentScale.FillBounds)
                visible.mapIndexed { index, action -> Triple(index / 4, index % 4, action) }.forEach { (row,col,action) ->
                    key(action.id) {
                        var appeared by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { appeared = true }
                        val scale by animateFloatAsState(if(appeared) 1f else .25f, label = "Badge arrival")
                        val itemW = w * .19f
                        val itemH = h * .25f
                        val x = w * (.21f + col*.195f) - itemW/2
                        val y = h * (if(row == 0) .15f else .53f)
                        Box(Modifier.offset(x=x,y=y).size(itemW,itemH)
                            .graphicsLayer { scaleX=scale;scaleY=scale;transformOrigin=androidx.compose.ui.graphics.TransformOrigin(.5f,0f) },
                            contentAlignment = Alignment.Center) {
                            Image(gameArtPainter(DailyShowcaseArt.badgeResource(action.skill), 128),
                                "${DailyShowcase.collection(action.skill)?.title} badge: ${action.originalName ?: action.name}",
                                Modifier.fillMaxSize())
                        }
                    }
                }
                if(entries.isEmpty()) Text("A place for today's badges", Modifier.align(Alignment.TopCenter).padding(top=h*.29f), color=Color(0xFFE3CBA9), style=MaterialTheme.typography.labelSmall)
            }
            if(pages > 1) Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
                AppTextButton(enabled=currentPage>0,onClick={page=currentPage-1}) { Text("Previous",color=Color.White) }
                Text("Display ${currentPage+1} / $pages",color=Color.White,style=MaterialTheme.typography.labelSmall)
                AppTextButton(enabled=currentPage<pages-1,onClick={page=currentPage+1}) { Text("Next",color=Color.White) }
            }
            DailyCoinBonus.goals.forEach { goal ->
                val done = DailyCoinBonus.claimKey(day, goal) in state.coinRewardDays
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (done) "✓" else "${DailyCoinBonus.progress(state, day, goal)}/${goal.categoriesRequired}",
                        color = if (done) Color(0xFF9EDAB5) else Color(0xFFD6E2E8),
                        style = MaterialTheme.typography.labelSmall)
                    Text(goal.label, Modifier.weight(1f), color = Color(0xFFD6E2E8),
                        style = MaterialTheme.typography.labelSmall,
                        textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None)
                    Image(gameArtPainter(com.healersjourney.app.R.drawable.gameart_icon_coin), null, Modifier.size(18.dp))
                    Text("+${goal.coins}", color = Color(0xFFFFE1A5), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
