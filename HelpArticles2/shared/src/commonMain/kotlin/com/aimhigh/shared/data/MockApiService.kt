package com.aimhigh.shared.data

import com.aimhigh.shared.domain.Article
import com.aimhigh.shared.domain.ArticleDetailResponse
import com.aimhigh.shared.domain.ArticlesResponse
import com.aimhigh.shared.domain.BackendErrorResponse
import kotlinx.serialization.json.Json

/**
 * Mock API service that simulates various backend scenarios:
 * - Normal successful responses
 * - Backend error responses (with errorCode, errorTitle, errorMessage)
 * - Transport errors (500, timeout)
 */
object MockApiService {

    private val json = Json { prettyPrint = true }

    // Simulate different scenarios with a counter
    private var callCount = 0

    fun getScenario(): Scenario {
        callCount++
        return when {
            callCount % 10 == 0 -> Scenario.TIMEOUT
            callCount % 15 == 0 -> Scenario.SERVER_ERROR
            callCount % 20 == 0 -> Scenario.BACKEND_ERROR
            else -> Scenario.SUCCESS
        }
    }

    fun getMockArticles(): ArticlesResponse {
        val currentTime = System.currentTimeMillis()
        return ArticlesResponse(
            articles = listOf(
                Article(
                    id = "1",
                    title = "Getting Started with Help Articles",
                    summary = "Learn the basics of navigating and using our help system.",
                    content = """
                        # Getting Started
                        
                        Welcome to our comprehensive help system! This guide will walk you through the basics.
                        
                        ## Key Features
                        - Search functionality to find articles quickly
                        - Offline support for viewing previously loaded articles
                        - Automatic updates when new content is available
                        
                        ## Navigation
                        Simply tap on any article title to view the full content. Use the search bar at the top to filter articles by keywords.
                        
                        ## Offline Mode
                        Articles are automatically cached for offline viewing. The cache is refreshed every 24 hours when you're online.
                    """.trimIndent(),
                    lastUpdatedTimestamp = currentTime - (2 * 60 * 60 * 1000) // 2 hours ago
                ),
                Article(
                    id = "2",
                    title = "Account Management",
                    summary = "Manage your account settings, password, and preferences.",
                    content = """
                        # Account Management
                        
                        Your account is the central hub for all your personalization settings.
                        
                        ## Changing Your Password
                        1. Navigate to Settings
                        2. Select "Security"
                        3. Tap "Change Password"
                        4. Enter your current and new password
                        
                        ## Privacy Settings
                        Control what information is shared and how it's used. You can customize:
                        - Data collection preferences
                        - Notification settings
                        - Account visibility
                        
                        ## Two-Factor Authentication
                        We strongly recommend enabling 2FA for enhanced security. This adds an extra layer of protection to your account.
                    """.trimIndent(),
                    lastUpdatedTimestamp = currentTime - (5 * 60 * 60 * 1000) // 5 hours ago
                ),
                Article(
                    id = "3",
                    title = "Troubleshooting Common Issues",
                    summary = "Solutions to frequently encountered problems and errors.",
                    content = """
                        # Troubleshooting Guide
                        
                        Having issues? Check out these common solutions.
                        
                        ## App Won't Load
                        - Check your internet connection
                        - Clear app cache (Settings > Apps > Help Articles > Clear Cache)
                        - Restart the app
                        
                        ## Content Not Updating
                        - Pull down on the articles list to refresh
                        - Ensure you have a stable internet connection
                        - Check if background data is enabled for this app
                        
                        ## Search Not Working
                        - Make sure you're using keywords that appear in article titles or summaries
                        - Try broadening your search terms
                        - Clear the search field and try again
                        
                        ## Still Having Problems?
                        If these solutions don't help, please contact our support team with details about your issue.
                    """.trimIndent(),
                    lastUpdatedTimestamp = currentTime - (10 * 60 * 60 * 1000) // 10 hours ago
                ),
                Article(
                    id = "4",
                    title = "Privacy Policy",
                    summary = "Understand how we collect, use, and protect your data.",
                    content = """
                        # Privacy Policy
                        
                        Last updated: ${
                        java.text.SimpleDateFormat("MMMM dd, yyyy").format(currentTime)
                    }
                        
                        ## Data Collection
                        We collect minimal data necessary to provide you with the best experience:
                        - Usage statistics (anonymized)
                        - Device information for compatibility
                        - Cached content for offline access
                        
                        ## Data Usage
                        Your data is used solely to:
                        - Improve app performance
                        - Provide personalized content
                        - Fix bugs and issues
                        
                        ## Data Protection
                        We employ industry-standard security measures including:
                        - Encrypted data transmission
                        - Secure local storage
                        - Regular security audits
                        
                        ## Your Rights
                        You have the right to:
                        - Access your data
                        - Request data deletion
                        - Opt out of data collection
                        
                        For detailed information, visit our website or contact privacy@example.com
                    """.trimIndent(),
                    lastUpdatedTimestamp = currentTime - (24 * 60 * 60 * 1000) // 1 day ago
                ),
                Article(
                    id = "5",
                    title = "Advanced Features",
                    summary = "Explore powerful features for experienced users.",
                    content = """
                        # Advanced Features
                        
                        Unlock the full potential of the app with these advanced capabilities.
                        
                        ## Keyboard Shortcuts
                        For devices with keyboards:
                        - `Ctrl+F` - Quick search
                        - `Esc` - Close current article
                        - `Ctrl+R` - Refresh articles
                        
                        ## Background Sync
                        The app automatically refreshes content in the background once daily. This ensures you always have the latest articles available offline.
                        
                        ### Sync Configuration
                        Background sync runs when:
                        - Device is connected to Wi-Fi or mobile data
                        - Battery is not critically low
                        - App hasn't been force-stopped
                        
                        ## Customization
                        Advanced users can customize:
                        - Cache duration (default: 24 hours)
                        - Auto-refresh behavior
                        - Offline mode preferences
                        
                        ## Export/Import
                        Save your favorite articles or bookmarks and transfer them between devices using the export/import feature in Settings.
                    """.trimIndent(),
                    lastUpdatedTimestamp = currentTime - (3 * 60 * 60 * 1000) // 3 hours ago
                )
            )
        )
    }

    fun getMockArticleDetail(id: String): ArticleDetailResponse? {
        val article = getMockArticles().articles.find { it.id == id }
        return article?.let { ArticleDetailResponse(it) }
    }

    fun getMockBackendError(): BackendErrorResponse {
        return BackendErrorResponse(
            errorCode = "ARTICLE_NOT_FOUND",
            errorTitle = "Article Not Found",
            errorMessage = "The requested article could not be found. It may have been deleted or moved."
        )
    }

    enum class Scenario {
        SUCCESS,
        BACKEND_ERROR,
        SERVER_ERROR,
        TIMEOUT
    }
}
