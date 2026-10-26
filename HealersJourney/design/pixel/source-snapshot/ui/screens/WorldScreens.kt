package com.healersjourney.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import com.healersjourney.app.ui.components.PixelEdgeShape
import com.healersjourney.app.ui.components.PixelShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.healersjourney.app.Destination
import com.healersjourney.app.R
import com.healersjourney.app.domain.*
import com.healersjourney.app.ui.*
import com.healersjourney.app.ui.components.*
import kotlin.math.sin

private val locations = listOf(
    Triple("knowledgeXp", "Library", Destination.LearningProgress),
    Triple("medicineXp", "Workshop", Destination.WorkProgress),
    Triple("strengthXp", "Training grounds", Destination.FitnessProgress),
    Triple("socialXp", "Gathering place", Destination.SocialProgress),
    Triple("hobbiesXp", "Creative studio", Destination.CreativityProgress)
)

@Composable
fun WorldHome(snapshot: GameSnapshot, viewModel: GameViewModel, onOpen: (Destination) -> Unit) {
    val state = snapshot.state
    var wardrobe by rememberSaveable { mutableStateOf(false) }
    var titles by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf(false) }
    val level = Rules.level(state.totalXp)
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(Art.badgeFor(Rules.title(level))), "View levels and titles",
                Modifier.size(56.dp).clickable { titles = true })
            Column(Modifier.weight(1f)) {
                Text(state.playerName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.clickable { rename = true })
                Text("Level $level · ${Rules.title(level)}", style = MaterialTheme.typography.bodySmall)
                SimpleProgress({ (state.totalXp % Rules.XP_PER_LEVEL).toFloat() / Rules.XP_PER_LEVEL },
                    Modifier.fillMaxWidth().padding(top = 8.dp).height(5.dp), color = MaterialTheme.colorScheme.primary)
                Text("${Rules.XP_PER_LEVEL - state.totalXp % Rules.XP_PER_LEVEL} XP to level ${level + 1}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Your little world", style = MaterialTheme.typography.titleLarge)
                Text("Real-life effort brings it to life.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AppTextButton(onClick = { wardrobe = true }) { Text("Customize") }
        }
        WorldStage(state, onOpen)
        AppButton(onClick = { onOpen(Destination.Todo) }, modifier = Modifier.fillMaxWidth()) { Text("Visit the quest board") }
        AdventureCard(snapshot, viewModel)
        val next = WorldRules.items.filter { it.id !in state.worldUnlocks && it.adventures == 0 }.minByOrNull { it.xp }
        if (next != null) Surface(shape = PixelShape(22.dp), color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.pixel_lock), null, Modifier.size(36.dp))
                Column(Modifier.weight(1f)) {
                    Text("Next discovery: ${next.name}", style = MaterialTheme.typography.titleSmall)
                    Text("${(next.xp - state.totalXp).coerceAtLeast(0)} more XP · ${next.slot}", style = MaterialTheme.typography.bodySmall)
                }
                AppTextButton(onClick = { wardrobe = true }) { Text("View") }
            }
        }
    }
    if (wardrobe) WorldWardrobe(snapshot, viewModel) { wardrobe = false }
    if (titles) TitleShelfDialog(level) { titles = false }
    if (rename) RenameDialog(state.playerName, { rename = false }) { viewModel.run { current -> GameEngine.renamePlayer(current, it) }; rename = false }
}

@Composable
@NonSkippableComposable
private fun WorldStage(state: GameState, onOpen: (Destination) -> Unit) {
    val night = state.worldEquipment["scenery"] in listOf("moonlight", "aurora")
    val scene = state.worldEquipment["scenery"]
    val sky = MaterialTheme.colorScheme.surface

    val transition = rememberInfiniteTransition(label = "World life")
    val float by transition.animateFloat(0f, 5f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "Character bob")
    BoxWithConstraints(Modifier.fillMaxWidth().height(480.dp).clip(PixelShape(32.dp))
        .background(sky)) {
        Image(painterResource(PixelArt.scenery(scene)), null, Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds)
        if (state.worldEquipment["decor"] != "plain") Image(painterResource(PixelArt.decor(state.worldEquipment["decor"])),
            null, Modifier.size(72.dp).align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 12.dp))
        val coordinates = listOf(.22f to .2f, .78f to .2f, .17f to .59f, .83f to .59f, .5f to .87f)
        locations.forEachIndexed { index, (skill, name, destination) ->
            val (x, y) = coordinates[index]
            WorldPlace(skill, name, WorldRules.placeStage(state.skillXp(skill)),
                Modifier.offset(x = maxWidth * x - 48.dp, y = 480.dp * y - 46.dp).width(96.dp)) { onOpen(destination) }
        }
        Column(Modifier.offset(x = maxWidth / 2 - 85.dp, y = 480.dp * .3f + float.dp).width(170.dp)
            .clickable(role = Role.Button, onClickLabel = "Open personal care") { onOpen(Destination.Care) },
            horizontalAlignment = Alignment.CenterHorizontally) {
            WorldCharacter(state, Modifier.size(170.dp)) { onOpen(Destination.Care) }
            Surface(shape = PixelEdgeShape, color = Color.White.copy(alpha = .9f)) {
                Text(if (state.actionHistory.firstOrNull()?.type == "sleep") "Resting · zzz" else GameEngine.personalCareStats(state).rank,
                    Modifier.padding(horizontal = 12.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF273447))
            }
        }
        Text("Tap a place to explore · Tap your character for care", Modifier.align(Alignment.BottomCenter).padding(12.dp),
            style = MaterialTheme.typography.labelSmall, color = if (night) Color.White else Color(0xFF364A42), textAlign = TextAlign.Center)
    }
}

@Composable
private fun WorldPlace(skill: String, name: String, stage: Int, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clickable(role = Role.Button, onClickLabel = "Explore ${Rules.SKILL_LABELS[skill]}", onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
            Image(painterResource(PixelArt.place(skill, stage)), null, Modifier.fillMaxSize())
            Image(painterResource(Art.skillIcon(skill)), null, Modifier.size(22.dp).padding(top = 4.dp), alpha = 1f)
        }
        Surface(shape = PixelShape(10.dp), color = Color.White.copy(alpha = .92f)) {
            Column(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(name, style = MaterialTheme.typography.labelSmall, color = Color(0xFF283646), textAlign = TextAlign.Center)
                Text(listOf("Camp", "Growing", "Thriving", "Radiant")[stage], style = MaterialTheme.typography.labelSmall, color = Color(0xFF65756B))
            }
        }
    }
}

@Composable
@NonSkippableComposable
fun WorldCharacter(state: GameState, modifier: Modifier = Modifier, previewScore: Int? = null, onTap: (() -> Unit)? = null) {
    var clock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(60_000); clock = System.currentTimeMillis() } }
    val score = previewScore ?: clock.let { GameEngine.personalCareStats(state).score }
    val key = Rules.careAvatarKey(score)
    val outfit = state.worldEquipment["outfit"]
    val cape = when (outfit) {
        "forest_cloak", "ranger" -> Color(0xFF4B8D68)
        "sun_cape", "golden_knight" -> Color(0xFFE6A53F)
        "ocean_explorer", "winter_coat" -> Color(0xFF76ABCA)
        "cozy_knit" -> Color(0xFFD58E7E)
        else -> Color(0xFF8366BC)
    }
    Box(if (onTap != null) modifier.clickable(role = Role.Button, onClick = onTap) else modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = .15f), Offset(size.width * .28f, size.height * .9f), Size(size.width * .44f, size.height * .04f))
            if (outfit != null && outfit != "traveler") {
                val unit = size.width / 32f
                for (row in 14..28) {
                    val half = 4 + (row - 14) / 3
                    drawRect(cape, Offset((16-half)*unit,row*unit), Size(half*2*unit,unit))
                    drawRect(Color.Black.copy(alpha=.15f), Offset((16+half-1)*unit,row*unit), Size(unit,unit))
                }
            }
        }
        Image(painterResource(Art.careAvatar(key)), "Your character: $key", Modifier.fillMaxSize())
        state.worldEquipment["companion"]?.takeIf { it != "none" }?.let { CompanionArt(it, Modifier.size(32.dp).align(Alignment.BottomEnd)) }
        if (state.actionHistory.firstOrNull()?.type == "sleep") Text("z Z z", Modifier.align(Alignment.TopEnd),
            color = Color(0xFF495E91), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun CompanionArt(id: String, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "Companion")
    val bob by transition.animateFloat(0f, 4f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "Companion float")
    Image(painterResource(PixelArt.companion(id)), null, modifier.offset(y = bob.dp))
}

@Composable
fun AdventureCard(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val state = snapshot.state
    val progress = WorldRules.adventure(state)
    val claimed = Dates.weekKeyOfNow() in state.adventureClaims
    val reward = when (state.adventureClaims.size + if (claimed) 0 else 1) { 1 -> "Adventure lanterns"; 2 -> "Moonlit realm"; 3 -> "Star companion"; else -> "Adventure seal" }
    SectionCard(title = "Weekly adventure · Restore the starlight", leadingIcon = R.drawable.pixel_milestone) {
        Text("Bring light back to your world by showing up across different parts of life.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        SimpleProgress({ progress.fraction }, Modifier.fillMaxWidth().height(8.dp), color = MaterialTheme.colorScheme.tertiary)
        Spacer(Modifier.height(12.dp))
        Text("${progress.actions.coerceAtMost(12)}/12 activities · ${progress.days.coerceAtMost(3)}/3 days · ${progress.categories.coerceAtMost(3)}/3 places",
            style = MaterialTheme.typography.labelMedium)
        Text("Reward: $reward + a permanent adventure seal", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        AppTonalButton(enabled = progress.complete && !claimed, onClick = { viewModel.run { WorldRules.claimAdventure(it) } }) {
            Text(if (claimed) "Adventure reward collected" else if (progress.complete) "Collect adventure reward" else "Adventure in progress")
        }
        if (state.adventureClaims.isNotEmpty()) Text("${state.adventureClaims.size} adventure seals collected", style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyQuestBoard(snapshot: GameSnapshot, viewModel: GameViewModel) {
    val quests = WorldRules.quests(snapshot.state)
    var selected by remember { mutableStateOf<DailyQuest?>(null) }
    selected?.let { quest -> PresetEditorDialog(quest.skill, snapshot, viewModel, initialActivity = quest.activity) { selected = null } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.pixel_today), null, Modifier.size(38.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Today's quest board", style = MaterialTheme.typography.titleLarge)
                Text("${quests.count { it.completed }} / ${quests.size} quests complete · A fresh board at 5 AM", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (quests.isEmpty()) EmptyHint("Add presets below to start your daily adventure.")
        quests.forEach { quest ->
            val xp = GameEngine.activityXpPreview(snapshot.state, quest.skill, quest.activity.xp).first
            Surface(shape = PixelShape(22.dp), color = if (quest.completed) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClickLabel = "Edit or delete preset",
                    onLongClick = { selected = quest }).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(painterResource(Art.skillIcon(quest.skill)), null, Modifier.size(42.dp))
                        Column(Modifier.weight(1f)) {
                            Text(Rules.activityLabel(quest.skill, quest.activity.name), style = MaterialTheme.typography.titleMedium)
                            Text("${Rules.SKILL_LABELS[quest.skill]} · ${if (quest.completed) "Reward earned" else "+$xp XP"}", style = MaterialTheme.typography.labelMedium)
                        }
                        if (quest.completed) Image(painterResource(R.drawable.pixel_check), "Quest complete", Modifier.size(30.dp))
                    }
                    if (!quest.completed) AppTonalButton(onClick = { viewModel.run { WorldRules.finishQuest(it, quest.skill, quest.activity.id) } }) { Text("Complete quest · +$xp XP") }
                }
            }
        }
    }
}

@Composable
@NonSkippableComposable
private fun WorldRoom(state: GameState) {
    val scene = state.worldEquipment["scenery"]
    Box(Modifier.fillMaxWidth().height(220.dp).clip(PixelShape(20.dp)).background(Color(0xFFE7DAC7))) {
        Image(painterResource(PixelArt.room(scene)), null, Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds)
        if (state.worldEquipment["decor"] != "plain") Image(painterResource(PixelArt.decor(state.worldEquipment["decor"])),
            null, Modifier.size(88.dp).align(Alignment.BottomEnd))
        WorldCharacter(state, Modifier.size(190.dp).align(Alignment.BottomCenter))
    }
}

@Composable
private fun WorldWardrobe(snapshot: GameSnapshot, viewModel: GameViewModel, onDismiss: () -> Unit) {
    var slot by remember { mutableStateOf("outfit") }
    val state = snapshot.state
    AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape, onDismissRequest = onDismiss, title = { Text("Your room & wardrobe") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WorldRoom(state)
                Text("Your avatar reflects your care routines", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("outfit" to "Wear", "decor" to "Decor", "companion" to "Friends", "scenery" to "Scene").forEach { (key, label) ->
                        FilterChip(selected = slot == key, onClick = { slot = key }, label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                WorldRules.items.filter { it.slot == slot }.forEach { item ->
                    val unlocked = item.id in state.worldUnlocks
                    val equipped = state.worldEquipment[slot] == item.id
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (slot == "companion" && item.id != "none") CompanionArt(item.id, Modifier.size(30.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleSmall)
                            Text(if (unlocked) "Yours to keep" else if (item.adventures > 0) "${item.adventures} weekly adventures" else "${item.xp} lifetime XP",
                                style = MaterialTheme.typography.labelSmall)
                        }
                        AppTextButton(enabled = unlocked && !equipped, onClick = { viewModel.run { WorldRules.equip(it, item.id) } }) {
                            Text(if (equipped) "Equipped" else if (unlocked) "Equip" else "Locked")
                        }
                    }
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reward sounds", Modifier.weight(1f)); PixelToggle(state.soundEnabled, { value -> viewModel.runQuiet { it.soundEnabled = value } })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Haptic feedback", Modifier.weight(1f)); PixelToggle(state.hapticsEnabled, { value -> viewModel.runQuiet { it.hapticsEnabled = value } })
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Back to my world") } })
}
