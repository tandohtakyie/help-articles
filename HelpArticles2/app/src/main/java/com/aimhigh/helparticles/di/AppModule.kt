package com.aimhigh.helparticles.di

import android.content.Context
import com.aimhigh.helparticles.shared.cache.HelpArticlesDatabase
import com.aimhigh.shared.cache.ArticleCache
import com.aimhigh.shared.cache.DatabaseDriverFactory
import com.aimhigh.shared.data.ArticleApiClient
import com.aimhigh.shared.data.ArticleApiClientImpl
import com.aimhigh.shared.data.ArticleRepository
import com.aimhigh.shared.data.ArticleRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseDriverFactory(
        @ApplicationContext context: Context
    ): DatabaseDriverFactory {
        return DatabaseDriverFactory(context)
    }

    @Provides
    @Singleton
    fun provideHelpArticlesDatabase(
        driverFactory: DatabaseDriverFactory
    ): HelpArticlesDatabase {
        return HelpArticlesDatabase(driverFactory.createDriver())
    }

    @Provides
    @Singleton
    fun provideArticleCache(
        database: HelpArticlesDatabase
    ): ArticleCache {
        return ArticleCache(database)
    }

    @Provides
    @Singleton
    fun provideArticleApiClient(): ArticleApiClient {
        return ArticleApiClientImpl()
    }

    @Provides
    @Singleton
    fun provideArticleRepository(
        apiClient: ArticleApiClient,
        cache: ArticleCache
    ): ArticleRepository {
        return ArticleRepositoryImpl(apiClient, cache)
    }
}
