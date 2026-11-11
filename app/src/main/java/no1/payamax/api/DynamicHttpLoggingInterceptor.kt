package no1.payamax.api

import android.util.Log
import no1.payamax.utils.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class DynamicHttpLoggingInterceptor(
    private val userPreferences: UserPreferences,
    private val tag: String = "HttpLogging"
) : Interceptor {

    private val delegate = HttpLoggingInterceptor { message ->
        Log.d(tag, message)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        delegate.level = if (userPreferences.httpLoggingEnabled.value) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return delegate.intercept(chain)
    }
}
