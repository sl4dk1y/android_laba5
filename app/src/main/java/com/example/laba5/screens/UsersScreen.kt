package com.example.laba5.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.laba5.data.User
import com.example.laba5.data.UserRepository

@Composable
fun UsersScreen(repository: UserRepository, onSelect: (User) -> Unit, onBack: () -> Unit) {
    val users by repository.users.collectAsState(initial = emptyList())
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Зарегистрированные пользователи", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(users, key = { it.id }) { user ->
                Column(Modifier.fillMaxWidth().clickable { onSelect(user) }.padding(12.dp)) {
                    Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                    Text("${user.login} · ${if (user.isAdmin) "Администратор" else "Пользователь"}")
                }
                HorizontalDivider()
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}
