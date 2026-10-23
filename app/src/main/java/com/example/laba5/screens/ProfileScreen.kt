package com.example.laba5.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.laba5.data.User

@Composable
fun ProfileScreen(user: User, adminCanPromote: Boolean, onPromote: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Профиль", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("ФИО: ${user.fullName}"); Text("Логин: ${user.login}"); Text("Дата рождения: ${user.birthDate}")
        Text("Пол: ${user.gender}"); Text("Роль: ${if (user.isAdmin) "Администратор" else "Пользователь"}")
        Spacer(Modifier.height(16.dp))
        if (adminCanPromote && !user.isAdmin) Button(onClick = onPromote) { Text("Сделать администратором") }
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}
