package com.healersjourney.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.healersjourney.app.ui.GameViewModel
import com.healersjourney.app.ui.components.GameArt
import com.healersjourney.app.ui.components.gameArtPainter

@Composable
fun OnboardingScreen(viewModel: GameViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var character by rememberSaveable { mutableStateOf("male") }
    val validName = username.trim().isNotEmpty()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text("Welcome to Life RPG", style = MaterialTheme.typography.headlineMedium)
        Text("Every real-life activity helps your character grow.", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { if (it.length <= 24 && '\n' !in it) username = it },
            label = { Text("Your username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Choose your character", style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("male" to "Male", "female" to "Female").forEach { (key, label) ->
                val selected = character == key
                Surface(
                    modifier = Modifier.weight(1f).clickable { character = key },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(2.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Image(gameArtPainter(GameArt.avatar("traveler", key), 180), label,
                            Modifier.size(120.dp))
                        Text(label, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.runQuiet { state ->
                    state.playerName = username.trim()
                    state.worldEquipment["character"] = character
                    state.onboardingCompleted = true
                }
            },
            enabled = validName,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Start my journey") }
    }
}
