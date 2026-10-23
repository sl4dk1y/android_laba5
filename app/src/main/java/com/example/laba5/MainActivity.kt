package com.example.laba5

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.laba5.data.User
import com.example.laba5.data.UserRepository
import com.example.laba5.screens.*
import com.example.laba5.examples.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("lab5_settings", MODE_PRIVATE)
        setContent {
            var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", false)) }
            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(darkTheme, { enabled ->
                        darkTheme = enabled
                        preferences.edit().putBoolean("dark_theme", enabled).apply()
                    }, { finish() })
                }
            }
        }
    }
}

@Composable
fun AppNavigation(darkTheme: Boolean, onThemeChange: (Boolean) -> Unit, onExit: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf("login") }
    var user by remember { mutableStateOf<User?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    fun previousScreen(screen: String) = when (screen) {
        "registration" -> "login"
        "menu" -> "login"
        "users", "profile", "settings", "examples", "auth_log" -> "menu"
        "text_example", "layout_example", "button_example", "list_example" -> "examples"
        else -> "login"
    }
    BackHandler(enabled = currentScreen != "login") { currentScreen = previousScreen(currentScreen); error = null }

    when (currentScreen) {
        "login" -> LoginScreen({ login, password ->
            error = null
            scope.launch {
                val found = repository.authenticate(login, password)
                if (found == null) error = "Неверный логин или пароль" else {
                    user = found
                    withContext(Dispatchers.IO) {
                        context.openFileOutput("auth.log", android.content.Context.MODE_APPEND).bufferedWriter().use {
                            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
                            it.appendLine("$time — ${found.login}")
                        }
                    }
                    notice = "Вход выполнен"; currentScreen = "menu"
                }
            }
        }, { currentScreen = "registration" }, onExit, error)
        "registration" -> RegistrationScreen({ login, password, name, date, gender, avatarUri ->
            error = null
            scope.launch {
                val result = repository.register(login, password, name, date, gender, avatarUri)
                result.onSuccess {
                    notice = if (it.isAdmin) "Регистрация выполнена. Создан первый администратор" else "Регистрация выполнена"
                    currentScreen = "login"
                }.onFailure { error = it.message ?: "Ошибка регистрации" }
            }
        }, { currentScreen = "login" }, error)
        "menu" -> {
            val activeUser = user
            if (activeUser == null) currentScreen = "login" else {
                if (notice != null) { Toast.makeText(context, notice, Toast.LENGTH_SHORT).show(); notice = null }
                MenuScreen(activeUser, { destination ->
                    if (destination in listOf("users", "auth_log") && !activeUser.isAdmin) currentScreen = "menu" else currentScreen = destination
                }, {
                    user = null; currentScreen = "login"
                })
            }
        }
        "users" -> {
            if (user?.isAdmin != true) currentScreen = "menu" else UsersScreen(repository, { chosen -> selectedUser = chosen; currentScreen = "profile" }, { currentScreen = "menu" })
        }
        "profile" -> {
            val profile = if (selectedUser == null) user else selectedUser
            if (profile == null) currentScreen = "menu" else ProfileScreen(profile, user?.isAdmin == true, {
                val actorId = user?.id ?: return@ProfileScreen
                scope.launch {
                    if (repository.promote(actorId, profile.id)) {
                        selectedUser = repository.getUser(profile.id)
                        if (user?.id == profile.id) user = selectedUser
                        notice = "Права администратора назначены"
                    } else error = "Недостаточно прав для изменения роли"
                }
            }, { selectedUser = null; currentScreen = if (user?.isAdmin == true) "users" else "menu" })
        }
        "settings" -> SettingsScreen(darkTheme, onThemeChange, { currentScreen = "menu" })
        "auth_log" -> if (user?.isAdmin == true) AuthLogScreen(onBack = { currentScreen = "menu" }) else currentScreen = "menu"
        "examples" -> ExamplesMenu({ currentScreen = it }, { currentScreen = "menu" })
        "text_example" -> TextExample { currentScreen = "examples" }
        "layout_example" -> LayoutExample { currentScreen = "examples" }
        "button_example" -> ButtonExample { currentScreen = "examples" }
        "list_example" -> ListExample { currentScreen = "examples" }
    }
}
