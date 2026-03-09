package br.com.listado.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD(route = "dashboard", label = "Início", icon = Icons.Outlined.Home),
    CATALOG(route = "catalog", label = "Catálogo", icon = Icons.Outlined.Inventory2),
    LISTS(route = "lists", label = "Listas", icon = Icons.AutoMirrored.Outlined.ListAlt),
    HISTORY(route = "history", label = "Histórico", icon = Icons.Outlined.Schedule),
    ANALYTICS(route = "analytics", label = "Análises", icon = Icons.Outlined.Analytics),
}
