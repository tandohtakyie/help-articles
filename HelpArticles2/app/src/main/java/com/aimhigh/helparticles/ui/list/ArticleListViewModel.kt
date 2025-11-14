package com.aimhigh.helparticles.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimhigh.shared.data.ArticleRepository
import com.aimhigh.shared.domain.Article
import com.aimhigh.shared.domain.DataError
import com.aimhigh.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ArticleListUiState {
    data object Loading : ArticleListUiState()
    data class Success(val articles: List<Article>, val searchQuery: String = "") :
        ArticleListUiState()

    data class Error(val error: DataError, val cachedArticles: List<Article>? = null) :
        ArticleListUiState()
}

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleListUiState>(ArticleListUiState.Loading)
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadArticles()
        observeCachedArticles()
    }

    fun loadArticles(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = ArticleListUiState.Loading

            when (val result = repository.getArticles(forceRefresh)) {
                is Result.Success -> {
                    _uiState.value = ArticleListUiState.Success(
                        articles = result.data,
                        searchQuery = _searchQuery.value
                    )
                }

                is Result.Error -> {
                    // Check if we have cached data to show alongside error
                    val currentState = _uiState.value
                    val cachedArticles = if (currentState is ArticleListUiState.Success) {
                        currentState.articles
                    } else {
                        null
                    }

                    _uiState.value = ArticleListUiState.Error(
                        error = result.error,
                        cachedArticles = cachedArticles
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        val currentState = _uiState.value
        if (currentState is ArticleListUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
        }
    }

    fun retry() {
        loadArticles(forceRefresh = true)
    }

    private fun observeCachedArticles() {
        viewModelScope.launch {
            repository.observeArticles()
                .collect { articles ->
                    val currentState = _uiState.value

                    // Only update if we're in success state or have articles to show
                    if (currentState is ArticleListUiState.Success || articles.isNotEmpty()) {
                        _uiState.value = ArticleListUiState.Success(
                            articles = articles,
                            searchQuery = _searchQuery.value
                        )
                    }
                }
        }
    }
}
