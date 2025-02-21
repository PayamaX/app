package no1.payamax

import android.app.Application
import no1.payamax.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PayamaxApp  : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PayamaxApp)
            modules(appModule)
        }
    }
}