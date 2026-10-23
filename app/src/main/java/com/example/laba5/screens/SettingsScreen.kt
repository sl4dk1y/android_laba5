package com.example.laba5.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        // Заголовок экрана
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Настройка темы
        Text(
            text = "Тема приложения"
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row {

            // Светлая тема
            Button(
                onClick = {
                    onThemeChange(false)
                }
            ) {
                Text("День")
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            // Тёмная тема
            Button(
                onClick = {
                    onThemeChange(true)
                }
            ) {
                Text("Ночь")
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Отображение текущей темы
        Text(
            text = if (darkMode) {
                "Выбрана ночная тема"
            } else {
                "Выбрана дневная тема"
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Возврат в меню
        OutlinedButton(
            onClick = onBack
        ) {
            Text("Назад")
        }
    }
}