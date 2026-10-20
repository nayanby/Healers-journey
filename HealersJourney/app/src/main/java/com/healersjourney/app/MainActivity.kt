package com.healersjourney.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.key
import com.healersjourney.app.ui.screens.GrowthScreen
import com.healersjourney.app.ui.screens.ExploreScreen
import androidx.compose.ui.graphics.toArgb
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.GameEngine
import com.healersjourney.app.domain.GameState
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.GameSnapshot
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.PillNavigation
import com.healersjourney.app.ui.components.Art
import com.healersjourney.app.ui.screens.AchievementsScreen
import com.healersjourney.app.ui.screens.CareScreen
import com.healersjourney.app.ui.screens.DataScreen
import com.healersjourney.app.ui.screens.DashboardScreen
import com.healersjourney.app.ui.screens.RenameDialog
import com.healersjourney.app.ui.screens.DailyScreen
import com.healersjourney.app.ui.screens.FinanceScreen
import com.healersjourney.app.ui.screens.FitnessScreen
import com.healersjourney.app.ui.screens.HobbiesScreen
import com.healersjourney.app.ui.screens.LogsScreen
import com.healersjourney.app.ui.screens.MedicineScreen
import com.healersjourney.app.ui.screens.MilestonesScreen
import com.healersjourney.app.ui.screens.SleepScreen
import com.healersjourney.app.ui.screens.SocialScreen
import com.healersjourney.app.ui.screens.StudyScreen
import com.healersjourney.app.ui.screens.TitleShelfDialog
import com.healersjourney.app.ui.screens.TodoScreen
import com.healersjourney.app.ui.theme.HealersJourneyTheme
import kotlinx.coroutines.launch

enum class Destination(val label: String, val icon: ImageVector) {
    Dashboard("Home", Icons.Filled.Home),
    Growth("Growth", Icons.AutoMirrored.Filled.MenuBook),
    More("More", Icons.Filled.Menu),
    Daily("Quick activity", Icons.Filled.Bolt),
    Study("Study", Icons.AutoMirrored.Filled.MenuBook),
    Medicine("Medicine", Icons.Filled.MedicalServices),
    Fitness("Fitness", Icons.Filled.FitnessCenter),
    Social("Social", Icons.Filled.Groups),
    Sleep("Sleep", Icons.Filled.Bedtime),
    Hobbies("Hobbies", Icons.Filled.MusicNote),
    Finance("Finance", Icons.Filled.AccountBalanceWallet),
    Todo("To-do list", Icons.Filled.Checklist),
    Care("Personal care", Icons.Filled.Sanitizer),
    Milestones("Milestones", Icons.Filled.Flag),
    Achievements("Achievements", Icons.Filled.WorkspacePremium),
    Logs("Logs & history", Icons.AutoMirrored.Filled.ReceiptLong),
    Data("Data & sync", Icons.Filled.CloudSync)
}

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels {
        GameViewModel.Factory((application as HealersJourneyApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val signIn = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let(viewModel::handleDriveSignInResult)
        }
        setContent {
            HealersJourneyTheme {
                val colors = MaterialTheme.colorScheme
                val dark = androidx.compose.foundation.isSystemInDarkTheme()
                androidx.compose.runtime.SideEffect {
                    window.statusBarColor = colors.background.toArgb()
                    window.navigationBarColor = colors.surface.toArgb()
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                AppRoot(viewModel, onSignIn = { signIn.launch(viewModel.driveSignInIntent()) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: GameViewModel, onSignIn: () -> Unit) {
    val snapshot by viewModel.snapshot.collectAsState()
    val message by viewModel.message.collectAsState()
    val drive by viewModel.drive.collectAsState()
    val loaded by viewModel.loaded.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var destination by rememberSaveable { mutableStateOf(Destination.Dashboard) }
    var tab by rememberSaveable { mutableStateOf(Destination.Dashboard) }
    val primary = listOf(Destination.Dashboard, Destination.Todo, Destination.Growth, Destination.More)
    val open: (Destination) -> Unit = { next ->
        destination = next
        if (next in primary) tab = next
    }
    BackHandler(enabled = destination !in primary) { destination = tab }
    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it); viewModel.showMessage(null) }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { PillNavigation(tab) { open(it) } },
        topBar = {
            TopAppBar(
                title = { Text(when (destination) {
                    Destination.Dashboard -> "Life RPG"
                    Destination.Todo -> "Today"
                    Destination.More -> "Your world"
                    else -> destination.label
                }, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (destination !in primary) IconButton(onClick = { destination = tab }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (!loaded) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else key(destination) {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(bottom = 20.dp)) {
                    when (destination) {
                        Destination.Dashboard -> DashboardScreen(snapshot, viewModel, open)
                        Destination.Growth -> GrowthScreen(snapshot, open)
                        Destination.More -> ExploreScreen(open)
                        Destination.Daily -> DailyScreen(snapshot, viewModel)
                        Destination.Study -> StudyScreen(snapshot, viewModel)
                        Destination.Medicine -> MedicineScreen(snapshot, viewModel)
                        Destination.Fitness -> FitnessScreen(snapshot, viewModel)
                        Destination.Social -> SocialScreen(snapshot, viewModel)
                        Destination.Sleep -> SleepScreen(snapshot, viewModel)
                        Destination.Hobbies -> HobbiesScreen(snapshot, viewModel)
                        Destination.Finance -> FinanceScreen(snapshot, viewModel)
                        Destination.Todo -> TodoScreen(snapshot, viewModel)
                        Destination.Care -> CareScreen(snapshot, viewModel)
                        Destination.Milestones -> MilestonesScreen(snapshot, viewModel)
                        Destination.Achievements -> AchievementsScreen(snapshot, viewModel)
                        Destination.Logs -> LogsScreen(snapshot, viewModel)
                        Destination.Data -> DataScreen(viewModel, drive, onSignIn)
                    }
                }
            }
        }
    }
}