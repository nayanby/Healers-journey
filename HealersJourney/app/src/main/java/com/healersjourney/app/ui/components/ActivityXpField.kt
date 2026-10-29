package com.healersjourney.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ActivityXpField(value: String, onChange: (String) -> Unit) {
    Column {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0, 10, 25, 50).forEach { amount -> FilterChip(value.toIntOrNull() == amount,
                { onChange(amount.toString()) }, label = { Text("$amount XP") }) }
        }
        OutlinedTextField(value, { onChange(it.filter(Char::isDigit).take(5)) }, label = { Text("Base XP") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Text("0 XP keeps the completion, coins and goal progress.", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
