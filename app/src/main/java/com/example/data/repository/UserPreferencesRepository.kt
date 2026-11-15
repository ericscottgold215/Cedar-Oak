package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BeverageFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.cedarDataStore: DataStore<Preferences> by preferencesDataStore(name = "cedar_oak_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val HAS_SEEDED_REVIEWS = booleanPreferencesKey("has_seeded_reviews")
        val PREFERRED_BEVERAGE_FILTER = stringPreferencesKey("preferred_beverage_filter")
        val LAST_REVIEWER_NAME = stringPreferencesKey("last_reviewer_name")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val preferredBeverageFilter: Flow<BeverageFilter> = context.cedarDataStore.data.map { prefs ->
        val raw = prefs[Keys.PREFERRED_BEVERAGE_FILTER]
        BeverageFilter.entries.find { it.name == raw } ?: BeverageFilter.ALL
    }

    val lastReviewerName: Flow<String> = context.cedarDataStore.data.map { prefs ->
        prefs[Keys.LAST_REVIEWER_NAME] ?: "Aficionado"
    }

    val isDarkTheme: Flow<Boolean> = context.cedarDataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME] ?: true
    }

    suspend fun hasSeededReviews(): Boolean =
        context.cedarDataStore.data.first()[Keys.HAS_SEEDED_REVIEWS] ?: false

    suspend fun markReviewsSeeded() {
        context.cedarDataStore.edit { prefs ->
            prefs[Keys.HAS_SEEDED_REVIEWS] = true
        }
    }

    suspend fun setPreferredBeverageFilter(filter: BeverageFilter) {
        context.cedarDataStore.edit { prefs ->
            prefs[Keys.PREFERRED_BEVERAGE_FILTER] = filter.name
        }
    }

    suspend fun setLastReviewerName(name: String) {
        context.cedarDataStore.edit { prefs ->
            prefs[Keys.LAST_REVIEWER_NAME] = name
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.cedarDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
        }
    }
}
