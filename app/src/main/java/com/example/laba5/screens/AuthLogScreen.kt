package com.example.laba5.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AuthLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        entries = withContext(Dispatchers.IO) {
            runCatching { context.openFileInput("auth.log").bufferedReader().useLines { it.toList().reversed() } }.getOrDefault(emptyList())
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Журнал авторизаций", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (entries.isEmpty()) Text("Записей пока нет") else LazyColumn(Modifier.weight(1f)) { items(entries) { Text(it, Modifier.fillMaxWidth().padding(12.dp)); HorizontalDivider() } }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}
