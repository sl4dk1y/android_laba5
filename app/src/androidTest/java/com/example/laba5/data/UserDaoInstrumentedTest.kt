package com.example.laba5.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: UserDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.userDao()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun usersAndRolesAreStoredAndObserved() = runBlocking {
        val adminId = dao.insert(User(login = "admin", passwordHash = "hash", fullName = "Admin",
            birthDate = "01.01.2000", gender = "Мужской", avatarUri = null, isAdmin = true))
        val userId = dao.insert(User(login = "user", passwordHash = "hash", fullName = "User",
            birthDate = "02.02.2000", gender = "Женский", avatarUri = null, isAdmin = false))

        val admin = dao.findById(adminId)
        assertTrue(admin!!.isAdmin)
        dao.update(dao.findById(userId)!!.copy(isAdmin = true))
        assertTrue(dao.findById(userId)!!.isAdmin)
        assertEquals(2, dao.observeAll().first().size)
    }

    @Test
    fun duplicateLoginIsRejected() = runBlocking {
        val first = User(login = "same", passwordHash = "hash", fullName = "First",
            birthDate = "01.01.2000", gender = "Мужской", avatarUri = null, isAdmin = true)
        dao.insert(first)
        assertThrows(SQLiteConstraintException::class.java) { runBlocking { dao.insert(first.copy(id = 0)) } }
    }

    @Test
    fun themePreferenceCanBeReadAfterWriting() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "lab5_instrumented_test"
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().putBoolean("dark_theme", true).commit()
        assertTrue(context.getSharedPreferences(name, Context.MODE_PRIVATE).getBoolean("dark_theme", false))
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        assertFalse(context.getSharedPreferences(name, Context.MODE_PRIVATE).getBoolean("dark_theme", false))
    }
}
