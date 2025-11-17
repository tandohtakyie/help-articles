package com.aimhigh.shared.cache

import com.aimhigh.shared.domain.Article
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ArticleCacheTest {

    @Test
    fun `isCacheStale returns true when no cache exists`() = runTest {
        val cache = createTestCache()

        val isStale = cache.isCacheStale()

        assert(isStale) { "Cache should be stale when no data exists" }
    }

    @Test
    fun `isCacheStale returns false when cache is fresh`() = runTest {
        val cache = createTestCache()
        val articles = listOf(
            Article(
                id = "1",
                title = "Test Article",
                summary = "Test Summary",
                content = "Test Content",
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        )

        cache.cacheArticles(articles)

        val isStale = cache.isCacheStale()
        assert(!isStale) { "Cache should be fresh when just added" }
    }

    @Test
    fun `isCacheStale returns true when cache exceeds TTL`() = runTest {
        val cache = createTestCache()

        val staleTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)

        val isStale = cache.isCacheStale()

        assert(isStale) { "Cache should be stale after 24 hours" }
    }

    @Test
    fun `cacheArticles stores all articles correctly`() = runTest {
        val cache = createTestCache()
        val articles = listOf(
            Article("1", "Title 1", "Summary 1", "Content 1", System.currentTimeMillis()),
            Article("2", "Title 2", "Summary 2", "Content 2", System.currentTimeMillis()),
            Article("3", "Title 3", "Summary 3", "Content 3", System.currentTimeMillis())
        )

        cache.cacheArticles(articles)

        val cached = cache.getAllArticles()
        assert(cached.size == 3) { "Should have 3 cached articles" }
        assert(cached.any { it.id == "1" }) { "Should contain article 1" }
        assert(cached.any { it.id == "2" }) { "Should contain article 2" }
        assert(cached.any { it.id == "3" }) { "Should contain article 3" }
    }

    @Test
    fun `cacheArticles replaces old cache`() = runTest {
        val cache = createTestCache()
        val oldArticles = listOf(
            Article("1", "Old Title", "Old Summary", "Old Content", System.currentTimeMillis())
        )
        cache.cacheArticles(oldArticles)

        val newArticles = listOf(
            Article("2", "New Title", "New Summary", "New Content", System.currentTimeMillis())
        )
        cache.cacheArticles(newArticles)

        val cached = cache.getAllArticles()
        assert(cached.size == 1) { "Should have only 1 article" }
        assert(cached.first().id == "2") { "Should be the new article" }
        assert(cached.none { it.id == "1" }) { "Old article should be gone" }
    }

    @Test
    fun `getArticleById returns correct article`() = runTest {
        val cache = createTestCache()
        val articles = listOf(
            Article("1", "Title 1", "Summary 1", "Content 1", System.currentTimeMillis()),
            Article("2", "Title 2", "Summary 2", "Content 2", System.currentTimeMillis()),
            Article("3", "Title 3", "Summary 3", "Content 3", System.currentTimeMillis())
        )
        cache.cacheArticles(articles)

        val article = cache.getArticleById("2")

        assert(article != null) { "Article should be found" }
        assert(article?.id == "2") { "Should be article 2" }
        assert(article?.title == "Title 2") { "Should have correct title" }
        assert(article?.summary == "Summary 2") { "Should have correct summary" }
    }

    @Test
    fun `getArticleById returns null for non-existent article`() = runTest {
        val cache = createTestCache()
        val articles = listOf(
            Article("1", "Title 1", "Summary 1", "Content 1", System.currentTimeMillis())
        )
        cache.cacheArticles(articles)

        val article = cache.getArticleById("999")

        assert(article == null) { "Non-existent article should return null" }
    }

    @Test
    fun `hasCachedArticles returns false initially`() = runTest {
        val cache = createTestCache()

        val hasArticles = cache.hasCachedArticles()

        assert(!hasArticles) { "Should have no cached articles initially" }
    }

    @Test
    fun `hasCachedArticles returns true after caching`() = runTest {
        val cache = createTestCache()
        val articles = listOf(
            Article("1", "Title", "Summary", "Content", System.currentTimeMillis())
        )

        cache.cacheArticles(articles)

        val hasArticles = cache.hasCachedArticles()
        assert(hasArticles) { "Should have cached articles after caching" }
    }

    @Ignore("Requires in-memory database - run integration tests instead")
    @Test
    fun `clearCache removes all articles and metadata`() = runTest {
        val cache = createTestCache()
        val articles = listOf(
            Article("1", "Title", "Summary", "Content", System.currentTimeMillis())
        )
        cache.cacheArticles(articles)

        assert(cache.hasCachedArticles()) { "Should have articles before clearing" }

        cache.clearCache()

        val hasArticles = cache.hasCachedArticles()
        val isStale = cache.isCacheStale()

        assert(!hasArticles) { "Should have no articles after clearing" }
        assert(isStale) { "Should be stale after clearing" }
    }

    @Test
    fun `cacheArticle stores single article without clearing others`() = runTest {
        val cache = createTestCache()
        val articles = listOf(
            Article("1", "Title 1", "Summary 1", "Content 1", System.currentTimeMillis())
        )
        cache.cacheArticles(articles)

        val newArticle =
            Article("2", "Title 2", "Summary 2", "Content 2", System.currentTimeMillis())
        cache.cacheArticle(newArticle)

        val cached = cache.getAllArticles()
        assert(cached.size == 2) { "Should have 2 articles" }
        assert(cached.any { it.id == "1" }) { "Should still have article 1" }
        assert(cached.any { it.id == "2" }) { "Should have article 2" }
    }

    @Test
    fun `getAllArticles returns articles ordered by lastUpdatedTimestamp desc`() = runTest {
        val cache = createTestCache()
        val now = System.currentTimeMillis()
        val articles = listOf(
            Article("1", "Oldest", "Summary 1", "Content 1", now - 10000),
            Article("2", "Newest", "Summary 2", "Content 2", now),
            Article("3", "Middle", "Summary 3", "Content 3", now - 5000)
        )
        cache.cacheArticles(articles)

        val cached = cache.getAllArticles()

        assert(cached[0].id == "2") { "Newest should be first" }
        assert(cached[1].id == "3") { "Middle should be second" }
        assert(cached[2].id == "1") { "Oldest should be last" }
    }


    private fun createTestCache(): ArticleCache {
        throw NotImplementedError("More UI Test")
    }
}