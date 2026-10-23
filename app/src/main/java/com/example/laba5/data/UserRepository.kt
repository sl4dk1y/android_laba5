package com.example.laba5.data

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

class UserRepository constructor(
    private val dao: UserDao,
    private val authLogDao: AuthLogDao
) {
    constructor(context: Context) : this(AppDatabase.get(context).userDao(), AppDatabase.get(context).authLogDao())

    val users: Flow<List<User>> = dao.observeAll()
    val authLogs: Flow<List<AuthLog>> = authLogDao.observeAllNewestFirst()

    suspend fun register(login: String, password: String, fullName: String, birthDate: String,
                        gender: String, avatarUri: String?): Result<User> = withContext(Dispatchers.IO) {
        val normalized = login.trim().lowercase()
        if (listOf(normalized, password, fullName.trim(), birthDate.trim(), gender).any { it.isBlank() }) {
            return@withContext Result.failure(IllegalArgumentException("Заполните все обязательные поля"))
        }
        if (normalized.length < 3) return@withContext Result.failure(IllegalArgumentException("Логин должен содержать не менее 3 символов"))
        if (password.length < 4) return@withContext Result.failure(IllegalArgumentException("Пароль должен содержать не менее 4 символов"))
        if (dao.findByLogin(normalized) != null) return@withContext Result.failure(IllegalArgumentException("Этот логин уже зарегистрирован"))
        val admin = dao.count() == 0
        val newUser = User(login = normalized, passwordHash = hash(password), fullName = fullName.trim(),
            birthDate = birthDate.trim(), gender = gender, avatarUri = avatarUri, isAdmin = admin)
        try {
            val saved = newUser.copy(id = dao.insert(newUser))
            Result.success(saved)
        } catch (_: Exception) {
            Result.failure(IllegalArgumentException("Не удалось создать пользователя с этим логином"))
        }
    }

    suspend fun authenticate(login: String, password: String): User? = withContext(Dispatchers.IO) {
        val user = dao.findByLogin(login.trim().lowercase()) ?: return@withContext null
        if (!verify(password, user.passwordHash)) return@withContext null
        authLogDao.insert(AuthLog(userId = user.id, login = user.login, timestamp = System.currentTimeMillis()))
        user
    }

    /** Imports only lines matching the exact legacy `dd.MM.yyyy HH:mm:ss — login` format. */
    suspend fun importLegacyAuthLog(context: Context): Int = withContext(Dispatchers.IO) {
        val lines = try {
            context.openFileInput("auth.log").bufferedReader().useLines { it.toList() }
        } catch (_: java.io.FileNotFoundException) {
            return@withContext 0
        } catch (_: Exception) {
            return@withContext 0
        }
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ROOT).apply { isLenient = false }
        var imported = 0
        lines.forEach { line ->
            val delimiter = " — "
            val split = line.indexOf(delimiter)
            if (split <= 0 || split + delimiter.length >= line.length) return@forEach
            val position = ParsePosition(0)
            val date = formatter.parse(line.substring(0, split), position)
            val login = line.substring(split + delimiter.length).trim()
            if (date == null || position.index != split || login.isBlank() || login.any { it.isWhitespace() }) return@forEach
            val userId = dao.findByLogin(login)?.id
            val result = authLogDao.insert(AuthLog(userId = userId, login = login, timestamp = date.time))
            if (result != -1L) imported++
        }
        imported
    }

    suspend fun promote(actorId: Long, targetId: Long): Boolean = withContext(Dispatchers.IO) {
        val actor = dao.findById(actorId) ?: return@withContext false
        val target = dao.findById(targetId) ?: return@withContext false
        if (!actor.isAdmin || actor.id == target.id) return@withContext false
        dao.update(target.copy(isAdmin = true)); true
    }

    suspend fun getUser(id: Long): User? = withContext(Dispatchers.IO) { dao.findById(id) }

    private fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        val derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" + Base64.encodeToString(derived, Base64.NO_WRAP)
    }

    private fun verify(password: String, stored: String): Boolean = try {
        val parts = stored.split(":")
        if (parts.size != 2) false else {
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expected = Base64.decode(parts[1], Base64.NO_WRAP)
            val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, expected.size * 8)
            MessageDigest.isEqual(expected, SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded)
        }
    } catch (_: Exception) { false }
}
