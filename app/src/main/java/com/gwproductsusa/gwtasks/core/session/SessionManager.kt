package com.gwproductsusa.gwtasks.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gwproductsusa.gwtasks.core.util.OdooConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gw_tasks_session"
)

data class SessionData(
    val userId: Int,
    val email: String,
    val databaseName: String,
    val isLoggedIn: Boolean
)

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.sessionDataStore

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] == true && (prefs[KEY_USER_ID] ?: 0) > 0
    }

    val sessionData: Flow<SessionData?> = dataStore.data.map { prefs ->
        val userId = prefs[KEY_USER_ID] ?: return@map null
        val email = prefs[KEY_EMAIL] ?: return@map null
        val database = prefs[KEY_DATABASE] ?: OdooConstants.DATABASE_NAME
        val loggedIn = prefs[KEY_IS_LOGGED_IN] == true
        if (!loggedIn || userId <= 0) return@map null
        SessionData(
            userId = userId,
            email = email,
            databaseName = database,
            isLoggedIn = true
        )
    }

    suspend fun saveSession(
        userId: Int,
        email: String,
        password: String,
        databaseName: String = OdooConstants.DATABASE_NAME
    ) {
        // commit() ensures password is persisted before navigation triggers dashboard API calls
        encryptedPrefs.edit().putString(KEY_PASSWORD, password).commit()
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_EMAIL] = email
            prefs[KEY_DATABASE] = databaseName
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun getPassword(): String? = encryptedPrefs.getString(KEY_PASSWORD, null)

    suspend fun getUserId(): Int = dataStore.data.first()[KEY_USER_ID] ?: 0

    suspend fun getEmail(): String = dataStore.data.first()[KEY_EMAIL].orEmpty()

    suspend fun getDatabaseName(): String =
        dataStore.data.first()[KEY_DATABASE] ?: OdooConstants.DATABASE_NAME

    suspend fun isLoggedInSync(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[KEY_IS_LOGGED_IN] == true && (prefs[KEY_USER_ID] ?: 0) > 0
    }

    suspend fun clearSession() {
        encryptedPrefs.edit().remove(KEY_PASSWORD).apply()
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private const val PREFS_FILE = "gw_tasks_secure_prefs"
        private const val KEY_PASSWORD = "password"

        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_DATABASE = stringPreferencesKey("database_name")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }
}
