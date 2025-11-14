package com.aimhigh.shared.data


import com.aimhigh.shared.cache.ArticleCache
import com.aimhigh.shared.domain.Article
import com.aimhigh.shared.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository implementing offline-first pattern:
 * 1. Check cache first
 * 2. If cache is stale or missing, fetch from network
 * 3. Update cache with fresh data
 * 4. Return cached data if network fails
 */
interface ArticleRepository {
    suspend fun getArticles(forceRefresh: Boolean = false): Result<List<Article>>
    suspend fun getArticleDetail(id: String, forceRefresh: Boolean = false): Result<Article>
    fun observeArticles(): Flow<List<Article>>
    fun observeArticleDetail(id: String): Flow<Article?>
    suspend fun refreshArticles(): Result<List<Article>>
}

class ArticleRepositoryImpl(
    private val apiClient: ArticleApiClient,
    private val cache: ArticleCache
) : ArticleRepository {

    override suspend fun getArticles(forceRefresh: Boolean): Result<List<Article>> {
        val isCacheStale = cache.isCacheStale()
        val hasCachedArticles = cache.hasCachedArticles()

        // If cache is fresh and we're not forcing refresh, return cached data
        if (!forceRefresh && hasCachedArticles && !isCacheStale) {
            val cachedArticles = cache.getAllArticles()
            return Result.Success(cachedArticles)
        }

        // Try to fetch from network
        return when (val networkResult = apiClient.getArticles()) {
            is Result.Success -> {
                // Cache the fresh data
                cache.cacheArticles(networkResult.data)
                Result.Success(networkResult.data)
            }

            is Result.Error -> {
                // If network fails but we have cache, return cached data
                if (hasCachedArticles) {
                    val cachedArticles = cache.getAllArticles()
                    Result.Success(cachedArticles)
                } else {
                    // No cache available, return the error
                    networkResult
                }
            }
        }
    }

    override suspend fun getArticleDetail(id: String, forceRefresh: Boolean): Result<Article> {
        // Check cache first
        val cachedArticle = cache.getArticleById(id)

        if (!forceRefresh && cachedArticle != null) {
            return Result.Success(cachedArticle)
        }

        // Fetch from network
        return when (val networkResult = apiClient.getArticleDetail(id)) {
            is Result.Success -> {
                // Cache the fresh data
                cache.cacheArticle(networkResult.data)
                Result.Success(networkResult.data)
            }

            is Result.Error -> {
                // If network fails but we have cache, return cached data
                if (cachedArticle != null) {
                    Result.Success(cachedArticle)
                } else {
                    networkResult
                }
            }
        }
    }

    override fun observeArticles(): Flow<List<Article>> {
        return cache.observeAllArticles()
    }

    override fun observeArticleDetail(id: String): Flow<Article?> {
        return cache.observeArticleById(id)
    }

    override suspend fun refreshArticles(): Result<List<Article>> {
        return getArticles(forceRefresh = true)
    }
}
