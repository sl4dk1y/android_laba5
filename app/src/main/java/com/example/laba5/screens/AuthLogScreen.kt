package com.example.laba5.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.laba5.data.UserRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuthLogScreen(repository: UserRepository, onBack: () -> Unit) {
    val entries by repository.authLogs.collectAsState(initial = emptyList())
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Журнал авторизаций", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (entries.isEmpty()) Text("Записей пока нет") else LazyColumn(Modifier.weight(1f)) {
            items(entries, key = { it.id }) { entry ->
                Text("${formatter.format(Date(entry.timestamp))} — ${entry.login}", Modifier.fillMaxWidth().padding(12.dp))
                HorizontalDivider()
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}
