package com.example.laba5.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegistrationScreen(onRegister: (String, String, String, String, String, String?) -> Unit, onBack: () -> Unit, error: String?) {
    val context = LocalContext.current
    var login by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }; var birthDate by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }; var avatarUri by rememberSaveable { mutableStateOf<String?>(null) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            avatarUri = uri.toString()
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("Регистрация пользователя", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(login, { login = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(password, { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        OutlinedTextField(fullName, { fullName = it }, label = { Text("ФИО") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(birthDate, { birthDate = it }, label = { Text("Дата рождения (ДД.ММ.ГГГГ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Пол")
        Row { listOf("Мужской", "Женский").forEach { value -> Row { RadioButton(gender == value, { gender = value }); Text(value, modifier = Modifier.padding(top = 12.dp, end = 8.dp)) } } }
        OutlinedButton(onClick = { avatarPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (avatarUri == null) "Выбрать аватар" else "Аватар выбран")
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onRegister(login, password, fullName, birthDate, gender, avatarUri) }, modifier = Modifier.fillMaxWidth()) { Text("Зарегистрироваться") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}
