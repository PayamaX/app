package no1.payamax.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class CalendarType {
    PERSIAN, GREGORIAN
}

enum class AppLanguage(val code: String) {
    PERSIAN("fa"),
    ENGLISH("en")
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("payamax_user_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _calendarType = MutableStateFlow(getSavedCalendarType())
    val calendarType: StateFlow<CalendarType> = _calendarType.asStateFlow()

    private val _appLanguage = MutableStateFlow(getSavedAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private fun getSavedThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun getSavedCalendarType(): CalendarType {
        val name = prefs.getString(KEY_CALENDAR_TYPE, CalendarType.PERSIAN.name) ?: CalendarType.PERSIAN.name
        return try {
            CalendarType.valueOf(name)
        } catch (e: Exception) {
            CalendarType.PERSIAN
        }
    }

    fun setCalendarType(type: CalendarType) {
        prefs.edit().putString(KEY_CALENDAR_TYPE, type.name).apply()
        _calendarType.value = type
    }

    private fun getSavedAppLanguage(): AppLanguage {
        val name = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.PERSIAN.name) ?: AppLanguage.PERSIAN.name
        return try {
            AppLanguage.valueOf(name)
        } catch (e: Exception) {
            AppLanguage.PERSIAN
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language.name).apply()
        _appLanguage.value = language
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_CALENDAR_TYPE = "key_calendar_type"
        private const val KEY_APP_LANGUAGE = "key_app_language"
    }
}
