package com.aimhigh.shared.data

import com.aimhigh.shared.domain.Article
import com.aimhigh.shared.domain.DataError
import com.aimhigh.shared.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface ArticleApiClient {
    suspend fun getArticles(): Result<List<Article>>
    suspend fun getArticleDetail(id: String): Result<Article>
}

class ArticleApiClientImpl(
    private val httpClient: HttpClient = createHttpClient()
) : ArticleApiClient {
    companion object {
        private const val TIMEOUT_MS = 15000L

        fun createHttpClient() = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MS
                connectTimeoutMillis = TIMEOUT_MS
                socketTimeoutMillis = TIMEOUT_MS
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            // Mock engine - simulates various scenarios
            expectSuccess = false // Handle errors manually
        }
    }

    override suspend fun getArticles(): Result<List<Article>> {
        return try {
            // Simulate mock behavior
            val scenario = MockApiService.getScenario()

            when (scenario) {
                MockApiService.Scenario.TIMEOUT -> {
                    kotlinx.coroutines.delay(TIMEOUT_MS + 1000)
                    Result.Error(DataError.Timeout())
                }

                MockApiService.Scenario.SERVER_ERROR -> {
                    Result.Error(DataError.ServerError(500))
                }

                MockApiService.Scenario.BACKEND_ERROR -> {
                    val errorResponse = MockApiService.getMockBackendError()
                    Result.Error(
                        DataError.BackendError(
                            errorCode = errorResponse.errorCode,
                            errorTitle = errorResponse.errorTitle,
                            errorMessage = errorResponse.errorMessage
                        )
                    )
                }

                else -> {
                    // Success scenario
                    kotlinx.coroutines.delay(500) // Simulate network delay
                    val response = MockApiService.getMockArticles()
                    Result.Success(response.articles)
                }
            }
        } catch (e: Exception) {
            when (e) {
                is HttpRequestTimeoutException -> Result.Error(DataError.Timeout(e))
                is io.ktor.client.network.sockets.ConnectTimeoutException -> Result.Error(
                    DataError.Timeout(
                        e
                    )
                )

                is io.ktor.client.network.sockets.SocketTimeoutException -> Result.Error(
                    DataError.Timeout(
                        e
                    )
                )

                else -> Result.Error(DataError.NetworkError(e))
            }
        }
    }

    override suspend fun getArticleDetail(id: String): Result<Article> {
        return try {
            // Simulate mock behavior
            val scenario = MockApiService.getScenario()

            when (scenario) {
                MockApiService.Scenario.TIMEOUT -> {
                    kotlinx.coroutines.delay(TIMEOUT_MS + 1000)
                    Result.Error(DataError.Timeout())
                }

                MockApiService.Scenario.SERVER_ERROR -> {
                    Result.Error(DataError.ServerError(500))
                }

                MockApiService.Scenario.BACKEND_ERROR -> {
                    val errorResponse = MockApiService.getMockBackendError()
                    Result.Error(
                        DataError.BackendError(
                            errorCode = errorResponse.errorCode,
                            errorTitle = errorResponse.errorTitle,
                            errorMessage = errorResponse.errorMessage
                        )
                    )
                }

                else -> {
                    // Success scenario
                    kotlinx.coroutines.delay(300) // Simulate network delay
                    val response = MockApiService.getMockArticleDetail(id)
                    if (response != null) {
                        Result.Success(response.article)
                    } else {
                        Result.Error(
                            DataError.BackendError(
                                errorCode = "NOT_FOUND",
                                errorTitle = "Not Found",
                                errorMessage = "Article with ID $id not found"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            when (e) {
                is HttpRequestTimeoutException -> Result.Error(DataError.Timeout(e))
                else -> Result.Error(DataError.NetworkError(e))
            }
        }
    }
}
