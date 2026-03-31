package com.matrix.multigpt.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AutoReplyDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val WHATSAPP_ENABLED = booleanPreferencesKey("auto_reply_whatsapp")
        val TELEGRAM_ENABLED = booleanPreferencesKey("auto_reply_telegram")
        val INSTAGRAM_ENABLED = booleanPreferencesKey("auto_reply_instagram")
        val REPLY_DELAY_SECONDS = intPreferencesKey("auto_reply_delay")
        val AUTO_REPLY_MASTER = booleanPreferencesKey("auto_reply_master")
    }

    val whatsappEnabled: Flow<Boolean> = dataStore.data.map { it[WHATSAPP_ENABLED] ?: false }
    val telegramEnabled: Flow<Boolean> = dataStore.data.map { it[TELEGRAM_ENABLED] ?: false }
    val instagramEnabled: Flow<Boolean> = dataStore.data.map { it[INSTAGRAM_ENABLED] ?: false }
    val replyDelaySeconds: Flow<Int> = dataStore.data.map { it[REPLY_DELAY_SECONDS] ?: 3 }
    val masterEnabled: Flow<Boolean> = dataStore.data.map { it[AUTO_REPLY_MASTER] ?: false }

    suspend fun setWhatsapp(enabled: Boolean) {
        dataStore.edit { it[WHATSAPP_ENABLED] = enabled }
    }

    suspend fun setTelegram(enabled: Boolean) {
        dataStore.edit { it[TELEGRAM_ENABLED] = enabled }
    }

    suspend fun setInstagram(enabled: Boolean) {
        dataStore.edit { it[INSTAGRAM_ENABLED] = enabled }
    }

    suspend fun setReplyDelay(seconds: Int) {
        dataStore.edit { it[REPLY_DELAY_SECONDS] = seconds }
    }

    suspend fun setMasterEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTO_REPLY_MASTER] = enabled }
    }
}
