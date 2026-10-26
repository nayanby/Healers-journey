package com.healersjourney.app.ui.screens

import com.healersjourney.app.ui.components.PixelImage as Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import com.healersjourney.app.ui.components.PixelShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import com.healersjourney.app.ui.components.AppIcon as Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.healersjourney.app.ui.components.AppTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.healersjourney.app.ui.components.pixelPainterResource as painterResource
import com.healersjourney.app.ui.components.AppLayout
import androidx.compose.ui.unit.dp
import com.healersjourney.app.domain.Rules
import com.healersjourney.app.ui.components.Art

/** Shield shelf opened from the drawer badge: every title, its unlock level and its artwork. */
@Composable
fun TitleShelfDialog(level: Int, onDismiss: () -> Unit) {
    val currentTitle = Rules.title(level)
    AlertDialog(shape = com.healersjourney.app.ui.components.PixelEdgeShape,
        onDismissRequest = onDismiss,
        title = { Text("Levels & titles") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Every ${Rules.XP_PER_LEVEL} XP is a level. Each rank adds a visible upgrade: stronger materials, sun rays, laurels, wings, then a crown and celestial armor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Rules.TITLE_LEVELS.forEachIndexed { tier, (minLevel, title) ->
                    val active = title == currentTitle
                    val reached = level >= minLevel
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(PixelShape(12.dp))
                            .background(
                                if (active) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Art.badgeFor(title)),
                            contentDescription = "$title shield",
                            modifier = Modifier.size(64.dp),
                            alpha = if (reached) 1f else 0.78f
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (active) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (reached) "Tier ${tier + 1} · Level $minLevel+"
                                else "Tier ${tier + 1} · Level $minLevel+ • ${(minLevel - level) * Rules.XP_PER_LEVEL} XP to go",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        when {
                            active -> Text(
                                "Current",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            reached -> Icon(
                                Icons.Filled.Check,
                                contentDescription = "Unlocked",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(AppLayout.icon)
                            )
                            else -> Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(AppLayout.icon)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
