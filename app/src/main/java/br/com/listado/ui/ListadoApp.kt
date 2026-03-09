package br.com.listado.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.listado.feature.analytics.AnalyticsScreen
import br.com.listado.feature.catalog.CatalogScreen
import br.com.listado.feature.dashboard.DashboardScreen
import br.com.listado.feature.history.HistoryScreen
import br.com.listado.feature.listdetail.ShoppingListDetailScreen
import br.com.listado.feature.lists.ListsScreen
import br.com.listado.ui.navigation.TopLevelDestination
import br.com.listado.ui.theme.ListadoTheme

private const val SHOPPING_LIST_DETAIL_ROUTE = "shopping-list"

@Composable
fun ListadoApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelDestinations = TopLevelDestination.entries
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    ListadoTheme {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        topLevelDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(imageVector = destination.icon, contentDescription = destination.label)
                                },
                                label = { Text(text = destination.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TopLevelDestination.DASHBOARD.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(TopLevelDestination.DASHBOARD.route) {
                    DashboardScreen(
                        onOpenCatalog = { navController.navigate(TopLevelDestination.CATALOG.route) },
                        onOpenLists = { navController.navigate(TopLevelDestination.LISTS.route) },
                        onOpenAnalytics = { navController.navigate(TopLevelDestination.ANALYTICS.route) },
                    )
                }
                composable(TopLevelDestination.CATALOG.route) {
                    CatalogScreen()
                }
                composable(TopLevelDestination.LISTS.route) {
                    ListsScreen(
                        onOpenList = { listId ->
                            navController.navigate("$SHOPPING_LIST_DETAIL_ROUTE/$listId")
                        },
                    )
                }
                composable(TopLevelDestination.HISTORY.route) {
                    HistoryScreen(
                        onOpenList = { listId ->
                            navController.navigate("$SHOPPING_LIST_DETAIL_ROUTE/$listId")
                        },
                    )
                }
                composable(TopLevelDestination.ANALYTICS.route) {
                    AnalyticsScreen()
                }
                composable(
                    route = "$SHOPPING_LIST_DETAIL_ROUTE/{listId}",
                    arguments = listOf(navArgument("listId") { type = NavType.LongType }),
                ) {
                    ShoppingListDetailScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
