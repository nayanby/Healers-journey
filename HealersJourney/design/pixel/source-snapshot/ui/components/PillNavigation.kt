package com.healersjourney.app.ui.components

import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import com.healersjourney.app.ui.components.PixelShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.healersjourney.app.Destination
import com.healersjourney.app.R

@Composable
fun PillNavigation(selected: Destination, onSelect: (Destination) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(Triple(Destination.Dashboard, R.drawable.pixel_home, "Home"),
                Triple(Destination.Todo, R.drawable.pixel_todo, "Today"),
                Triple(Destination.More, R.drawable.pixel_menu, "More")).forEach { (destination, icon, label) ->
                val active = selected == destination
                Column(Modifier.weight(1f).clip(PixelShape(18.dp))
                    .selectable(active, role = Role.Tab, onClick = { onSelect(destination) })
                    .padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.width(60.dp).height(34.dp).clip(PixelShape(16.dp))
                        .background(if (active) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent),
                        contentAlignment = Alignment.Center) {
                        Image(painterResource(icon), null, Modifier.size(28.dp))
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
