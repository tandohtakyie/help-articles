package com.aimhigh.helparticles.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aimhigh.shared.domain.DataError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ArticleDetailUiState.Loading -> {
                    LoadingContent()
                }

                is ArticleDetailUiState.Success -> {
                    ArticleContent(state)
                }

                is ArticleDetailUiState.Error -> {
                    if (state.cachedArticle != null) {
                        Column {
                            ErrorBanner(
                                error = state.error,
                                onRetry = viewModel::retry
                            )
                            ArticleContent(ArticleDetailUiState.Success(state.cachedArticle))
                        }
                    } else {
                        ErrorScreen(
                            error = state.error,
                            onRetry = viewModel::retry
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleContent(state: ArticleDetailUiState.Success) {
    val article = state.article
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium
        )

        // Metadata
        Text(
            text = "Last updated: ${formatDate(article.lastUpdatedTimestamp)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        // Content (Markdown)
        Text(
            text = article.content,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorBanner(
    error: DataError,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
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
private fun ErrorScreen(
    error: DataError,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = getErrorTitle(error),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        error.message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Retry")
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

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun getErrorTitle(error: DataError): String = when (error) {
    is DataError.NetworkError -> "Network Error"
    is DataError.Timeout -> "Request Timeout"
    is DataError.ServerError -> "Server Error"
    is DataError.BackendError -> error.errorTitle
    is DataError.ParseError -> "Parse Error"
    is DataError.Unknown -> "Unknown Error"
}
