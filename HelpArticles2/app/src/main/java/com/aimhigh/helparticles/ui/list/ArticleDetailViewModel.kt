package com.aimhigh.helparticles.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aimhigh.helparticles.ui.navigation.ArticleDetail
import com.aimhigh.shared.data.ArticleRepository
import com.aimhigh.shared.domain.Article
import com.aimhigh.shared.domain.DataError
import com.aimhigh.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ArticleDetailUiState {
    data object Loading : ArticleDetailUiState()
    data class Success(val article: Article) : ArticleDetailUiState()
    data class Error(val error: DataError, val cachedArticle: Article? = null) :
        ArticleDetailUiState()
}

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArticleRepository
) : ViewModel() {

    private val articleId: String = savedStateHandle.toRoute<ArticleDetail>().articleId

    private val _uiState = MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
        observeCachedArticle()
    }

    fun loadArticle(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = ArticleDetailUiState.Loading

            when (val result = repository.getArticleDetail(articleId, forceRefresh)) {
                is Result.Success -> {
                    _uiState.value = ArticleDetailUiState.Success(result.data)
                }

                is Result.Error -> {
                    val currentState = _uiState.value
                    val cachedArticle = if (currentState is ArticleDetailUiState.Success) {
                        currentState.article
                    } else {
                        null
                    }

                    _uiState.value = ArticleDetailUiState.Error(
                        error = result.error,
                        cachedArticle = cachedArticle
                    )
                }
            }
        }
    }

    fun retry() {
        loadArticle(forceRefresh = true)
    }

    private fun observeCachedArticle() {
        viewModelScope.launch {
            repository.observeArticleDetail(articleId)
                .filterNotNull()
                .collect { article ->
                    _uiState.value = ArticleDetailUiState.Success(article)
                }
        }
    }
}
