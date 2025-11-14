package com.aimhigh.helparticles.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aimhigh.shared.data.ArticleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.aimhigh.shared.domain.Result as DomainResult

@HiltWorker
class ArticleRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ArticleRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "article_refresh_work"
    }

    override suspend fun doWork(): Result {
        return try {
            when (repository.refreshArticles()) {
                is DomainResult.Success -> {
                    Result.success()
                }

                is DomainResult.Error -> {
                    // Retry with backoff if failed
                    Result.retry()
                }
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
