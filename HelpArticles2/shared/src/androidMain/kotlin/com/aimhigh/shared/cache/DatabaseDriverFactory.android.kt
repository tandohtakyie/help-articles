package com.aimhigh.shared.cache

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.aimhigh.helparticles.shared.cache.HelpArticlesDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = HelpArticlesDatabase.Schema as SqlSchema<QueryResult.Value<Unit>>,
            context = context,
            name = "helparticles.db",
            callback = object :
                AndroidSqliteDriver.Callback(HelpArticlesDatabase.Schema as SqlSchema<QueryResult.Value<Unit>>) {
                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Force recreate tables if they don't exist
                    db.execSQL("CREATE TABLE IF NOT EXISTS ArticleCache (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, summary TEXT NOT NULL, content TEXT NOT NULL, lastUpdatedTimestamp INTEGER NOT NULL, cachedAtTimestamp INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS CacheMetadata (key TEXT NOT NULL PRIMARY KEY, lastFetchTimestamp INTEGER NOT NULL, isStale INTEGER NOT NULL DEFAULT 0)")
                }
            }
        )
    }
}
