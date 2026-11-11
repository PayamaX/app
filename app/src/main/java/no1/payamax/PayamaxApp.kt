package no1.payamax

import android.app.Application
import no1.payamax.utils.DynamicHttpLoggingInterceptor
import no1.payamax.api.infrastructure.ApiClient
import no1.payamax.di.appModule
import no1.payamax.modules.rankedorigin.SyncWorker
import no1.payamax.utils.UserPreferences
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PayamaxApp : Application() {
    private val userPreferences: UserPreferences by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PayamaxApp)
            modules(appModule)
        }

        ApiClient.builder.addInterceptor(DynamicHttpLoggingInterceptor(userPreferences))

        SyncWorker.schedule(this)
    }
}