package com.example.laba5

import android.os.Bundle
import android.widget.Toast
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.laba5.data.User
import com.example.laba5.data.UserRepository
import com.example.laba5.screens.*
import com.example.laba5.examples.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val preferences = getSharedPreferences("lab5_settings", MODE_PRIVATE)
        setContent {
            var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", false)) }
            MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
                SideEffect {
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                        AppNavigation(darkTheme, { enabled ->
                            darkTheme = enabled
                            preferences.edit().putBoolean("dark_theme", enabled).apply()
                        }, { finish() })
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(darkTheme: Boolean, onThemeChange: (Boolean) -> Unit, onExit: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserRepository(context.applicationContext) }
    LaunchedEffect(repository) { repository.importLegacyAuthLog(context.applicationContext) }
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
                    if (destination in listOf("users", "auth_log") && !canAccessAdminScreens(activeUser)) currentScreen = "menu" else currentScreen = destination
                }, {
                    user = null; currentScreen = "login"
                })
            }
        }
        "users" -> {
            if (!canAccessAdminScreens(user)) currentScreen = "menu" else UsersScreen(repository, { chosen -> selectedUser = chosen; currentScreen = "profile" }, { currentScreen = "menu" })
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
        "auth_log" -> if (canAccessAdminScreens(user)) AuthLogScreen(repository, onBack = { currentScreen = "menu" }) else currentScreen = "menu"
        "examples" -> ExamplesMenu({ currentScreen = it }, { currentScreen = "menu" })
        "text_example" -> TextExample { currentScreen = "examples" }
        "layout_example" -> LayoutExample { currentScreen = "examples" }
        "button_example" -> ButtonExample { currentScreen = "examples" }
        "list_example" -> ListExample { currentScreen = "examples" }
    }
}

fun canAccessAdminScreens(user: User?): Boolean = user?.isAdmin == true
