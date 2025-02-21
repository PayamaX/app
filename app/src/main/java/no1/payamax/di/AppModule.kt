package no1.payamax.di

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.db.PayamaxDatabase
import no1.payamax.db.PredefinedOriginEntity
import no1.payamax.repos.PredefinedOriginRepo
import no1.payamax.services.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    single<SqlDriver> { AndroidSqliteDriver(PayamaxDatabase.Schema, get(), "payamax.db") }
    single { PayamaxDatabase(get(), PredefinedOriginEntity.Adapter(EnumColumnAdapter())) }
    single { get<PayamaxDatabase>().predefinedOriginQueries }
    single { PredefinedOriginRepo(get()) }
    factoryOf(::Cancel11UsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::CodeUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::EmojiUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::LandlineOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::OperatorOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::PredefinedOriginProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::PersonalOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::PhonebookOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::ThousandPrefixedOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factoryOf(::TitledOriginUsabilityProcessor) { bind<PayamakUsabilityRuleContract>() }
    factory { UsabilityProcessorEngine(getAll()) }
}