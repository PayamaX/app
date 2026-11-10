package no1.payamax.di

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.db.PayamaxDatabase
import no1.payamax.db.RankedOriginEntity
import no1.payamax.modules.rankedorigin.RankedOriginContract
import no1.payamax.modules.rankedorigin.RankedOriginService
import no1.payamax.services.*
import no1.payamax.utils.UserPreferences
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { UserPreferences(get()) }
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = PayamaxDatabase.Schema.synchronous(),
            context = get(),
            name = "payamax.db",
            callback = object : AndroidSqliteDriver.Callback(PayamaxDatabase.Schema.synchronous()) {
            }
        )
    }
    single { PayamaxDatabase(get(), RankedOriginEntity.Adapter(EnumColumnAdapter())) }
    single { get<PayamaxDatabase>().rankedOriginQueries }
    single<RankedOriginContract> { RankedOriginService(get(), get()) }
    single { RankedOriginProcessor(get(), get()) } bind PayamakUsabilityRuleContract::class
    factoryOf(::ConversionalUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::CancelableUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::CodeUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::EmojiUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::LandlineOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::OperatorOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::PersonalOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::PhonebookOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::ThousandPrefixedOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::TitledOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factory { UsabilityProcessorEngine(getAll()) }
}
