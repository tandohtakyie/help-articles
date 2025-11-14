package com.aimhigh.helparticles.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aimhigh.helparticles.ui.list.ArticleDetailScreen
import com.aimhigh.helparticles.ui.list.ArticleListScreen
import kotlinx.serialization.Serializable

@Serializable
object ArticleList

@Serializable
data class ArticleDetail(val articleId: String)

@Composable
fun HelpArticlesNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = ArticleList
    ) {
        composable<ArticleList> {
            ArticleListScreen(
                onArticleClick = { articleId ->
                    navController.navigate(ArticleDetail(articleId))
                }
            )
        }

        composable<ArticleDetail> { backStackEntry ->
            val articleDetail: ArticleDetail = backStackEntry.toRoute()
            ArticleDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

