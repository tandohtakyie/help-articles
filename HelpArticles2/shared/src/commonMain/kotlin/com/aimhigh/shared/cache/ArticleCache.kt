package com.aimhigh.shared.cache

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.aimhigh.helparticles.shared.cache.HelpArticlesDatabase
import com.aimhigh.shared.domain.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.aimhigh.helparticles.shared.cache.ArticleCache as ArticleCacheEntity

class ArticleCache(database: HelpArticlesDatabase) {

    companion object {
        const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val ARTICLES_LIST_KEY = "articles_list"
    }

    val queries = database.helpArticlesQueries

    suspend fun cacheArticles(articles: List<Article>) = withContext(Dispatchers.Default) {
        queries.transaction {
            val currentTime = System.currentTimeMillis()

            // Clear old cache
            queries.deleteAllArticles()

            // Insert new articles
            articles.forEach { article ->
                queries.insertOrReplaceArticle(
                    id = article.id,
                    title = article.title,
                    summary = article.summary,
                    content = article.content,
                    lastUpdatedTimestamp = article.lastUpdatedTimestamp,
                    cachedAtTimestamp = currentTime
                )
            }

            queries.insertOrReplaceMetadata(
                key = ARTICLES_LIST_KEY,
                lastFetchTimestamp = currentTime,
                isStale = false
            )
        }
    }

    suspend fun cacheArticle(article: Article) = withContext(Dispatchers.Default) {
        val currentTime = System.currentTimeMillis()
        queries.insertOrReplaceArticle(
            id = article.id,
            title = article.title,
            summary = article.summary,
            content = article.content,
            lastUpdatedTimestamp = article.lastUpdatedTimestamp,
            cachedAtTimestamp = currentTime
        )
    }

    fun observeAllArticles(): Flow<List<Article>> {
        return queries
            .getAllArticles()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { cached ->
                cached.map { it.toArticle() }
            }
    }

    suspend fun getAllArticles(): List<Article> = withContext(Dispatchers.Default) {
        queries
            .getAllArticles()
            .executeAsList()
            .map { it.toArticle() }
    }

    suspend fun getArticleById(id: String): Article? = withContext(Dispatchers.Default) {
        queries
            .getArticleById(id)
            .executeAsOneOrNull()
            ?.toArticle()
    }

    fun observeArticleById(id: String): Flow<Article?> {
        return queries
            .getArticleById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toArticle() }
    }

    suspend fun isCacheStale(): Boolean = withContext(Dispatchers.Default) {
        val metadata = queries
            .getMetadata(ARTICLES_LIST_KEY)
            .executeAsOneOrNull()
            ?: return@withContext true

        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - metadata.lastFetchTimestamp

        cacheAge > CACHE_TTL_MS
    }

    suspend fun hasCachedArticles(): Boolean = withContext(Dispatchers.Default) {
        queries
            .getAllArticles()
            .executeAsList()
            .isNotEmpty()
    }

    suspend fun clearCache() = withContext(Dispatchers.Default) {
        queries.transaction {
            queries.deleteAllArticles()
            queries.deleteMetadata(ARTICLES_LIST_KEY)
        }
    }

    private fun ArticleCacheEntity.toArticle() = Article(
        id = id,
        title = title,
        summary = summary,
        content = content,
        lastUpdatedTimestamp = lastUpdatedTimestamp
    )
}
