package com.aimhigh.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val lastUpdatedTimestamp: Long,
)

@Serializable
data class ArticlesResponse(
    val articles: List<Article>,
)

@Serializable
data class ArticleDetailResponse(
    val article: Article,
)
