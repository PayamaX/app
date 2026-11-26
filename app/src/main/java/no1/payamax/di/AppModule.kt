package no1.payamax.di

import android.content.Context
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import androidx.sqlite.db.SupportSQLiteDatabase
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.db.PayamaxDatabase
import no1.payamax.db.RankedOriginEntity
import no1.payamax.modules.rankedorigin.RankedOriginContract
import no1.payamax.modules.rankedorigin.RankedOriginService
import no1.payamax.modules.report.PayamakReportContract
import no1.payamax.modules.report.PayamakReportService
import no1.payamax.services.*
import no1.payamax.utils.UserPreferences
import no1.payamax.vm.ThreadConversationViewModel
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
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS PayamakReportEntity(
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            payamak_id INTEGER NOT NULL DEFAULT 0,
                            origin TEXT NOT NULL,
                            body TEXT,
                            sha512 TEXT,
                            moment TEXT NOT NULL,
                            detected_diagnostics TEXT NOT NULL,
                            expected_diagnostics TEXT NOT NULL,
                            reporter_comment TEXT,
                            calced_rank REAL NOT NULL,
                            processors_report TEXT NOT NULL,
                            created_at INTEGER NOT NULL,
                            synced INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent()
                    )
                    val columns = db.query("PRAGMA table_info(PayamakReportEntity)")
                    val hasPayamakId = try {
                        generateSequence { if (columns.moveToNext()) columns else null }
                            .any { columns.getString(1) == "payamak_id" }
                    } finally {
                        columns.close()
                    }
                    if (!hasPayamakId) {
                        db.execSQL(
                            "ALTER TABLE PayamakReportEntity ADD COLUMN payamak_id INTEGER NOT NULL DEFAULT 0"
                        )
                    }
                }
            }
        )
    }
    single { PayamaxDatabase(get(), RankedOriginEntity.Adapter(EnumColumnAdapter())) }
    single { get<PayamaxDatabase>().rankedOriginQueries }
    single { get<PayamaxDatabase>().payamakReportQueries }
    single<RankedOriginContract> {
        RankedOriginService(
            get(),
            get<Context>().getSharedPreferences("general", Context.MODE_PRIVATE),
        )
    }
    single<PayamakReportContract> {
        PayamakReportService(
            get(),
            get(),
            get()
        )
    }
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
    factoryOf(::ThreadConversationViewModel)
    single { UsabilityProcessorEngine(getAll()) }
}
