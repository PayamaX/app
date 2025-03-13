package no1.payamax

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import no1.payamax.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

@HiltAndroidApp
class PayamaxApp  : Application() {

        companion object {

        @Volatile
        private lateinit var instance: PayamaxApp
        fun getInstance() = instance

    }
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PayamaxApp)
            modules(appModule)
        }
    }
}