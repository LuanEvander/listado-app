package br.com.listado.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.listado.core.util.asCurrency
import br.com.listado.ui.components.MetricCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    onOpenCatalog: () -> Unit,
    onOpenLists: () -> Unit,
    onOpenAnalytics: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Listado")
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Gestão offline de compras, listas e histórico de preços.",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "O painel resume o catálogo, as compras em andamento e o valor já consolidado no histórico.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricCard(title = "Itens ativos", value = uiState.activeItemCount.toString(), modifier = Modifier.fillMaxWidth())
                    MetricCard(title = "Listas abertas", value = uiState.openListCount.toString(), modifier = Modifier.fillMaxWidth())
                    MetricCard(title = "Listas concluídas", value = uiState.completedListCount.toString(), modifier = Modifier.fillMaxWidth())
                    MetricCard(title = "Planejado agora", value = uiState.currentPlannedTotal.asCurrency(), modifier = Modifier.fillMaxWidth())
                    MetricCard(title = "Histórico comprado", value = uiState.totalPurchasedValue.asCurrency(), modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
