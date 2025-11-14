package com.aimhigh.helparticles.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aimhigh.shared.domain.Article
import com.aimhigh.shared.domain.DataError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    onArticleClick: (String) -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Articles") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search field
            SearchField(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Content based on state
            when (val state = uiState) {
                is ArticleListUiState.Loading -> {
                    LoadingContent()
                }
                is ArticleListUiState.Success -> {
                    val filteredArticles = filterArticles(state.articles, searchQuery)

                    if (filteredArticles.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isNotEmpty()) {
                                "No articles match your search"
                            } else {
                                "No articles available"
                            }
                        )
                    } else {
                        ArticleList(
                            articles = filteredArticles,
                            onArticleClick = onArticleClick
                        )
                    }
                }
                is ArticleListUiState.Error -> {
                    ErrorContent(
                        error = state.error,
                        cachedArticles = state.cachedArticles,
                        searchQuery = searchQuery,
                        onRetry = viewModel::retry,
                        onArticleClick = onArticleClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search articles...") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        singleLine = true
    )
}

@Composable
private fun ArticleList(
    articles: List<Article>,
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles, key = { it.id }) { article ->
            ArticleCard(
                article = article,
                onClick = { onArticleClick(article.id) }
            )
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Updated ${formatTimestamp(article.lastUpdatedTimestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: DataError,
    cachedArticles: List<Article>?,
    searchQuery: String,
    onRetry: () -> Unit,
    onArticleClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Show error banner
        ErrorBanner(error = error, onRetry = onRetry)

        // Show cached articles if available
        if (!cachedArticles.isNullOrEmpty()) {
            val filteredArticles = filterArticles(cachedArticles, searchQuery)

            Text(
                text = "Showing cached articles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ArticleList(
                articles = filteredArticles,
                onArticleClick = onArticleClick
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    error: DataError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getErrorTitle(error),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                error.message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun filterArticles(articles: List<Article>, query: String): List<Article> {
    if (query.isBlank()) return articles

    val lowerQuery = query.lowercase()
    return articles.filter {
        it.title.lowercase().contains(lowerQuery) ||
                it.summary.lowercase().contains(lowerQuery)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 60 * 1000 -> {
            val minutes = (diff / (60 * 1000)).toInt()
            "$minutes minute${if (minutes != 1) "s" else ""} ago"
        }
        diff < 24 * 60 * 60 * 1000 -> {
            val hours = (diff / (60 * 60 * 1000)).toInt()
            "$hours hour${if (hours != 1) "s" else ""} ago"
        }
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

private fun getErrorTitle(error: DataError): String = when (error) {
    is DataError.NetworkError -> "Network Error"
    is DataError.Timeout -> "Request Timeout"
    is DataError.ServerError -> "Server Error"
    is DataError.BackendError -> error.errorTitle
    is DataError.ParseError -> "Parse Error"
    is DataError.Unknown -> "Unknown Error"
}




