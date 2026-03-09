package br.com.listado.feature.analytics

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.listado.core.model.ItemPurchaseMode
import br.com.listado.core.model.baseUnitLabel
import br.com.listado.core.util.asDecimal
import br.com.listado.core.util.asCurrency
import br.com.listado.core.util.asDate
import br.com.listado.ui.components.EmptyStateCard
import br.com.listado.ui.components.MetricCard
import br.com.listado.ui.components.SimpleLineChart

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.items, uiState.selectedItemId) {
        if (uiState.selectedItemId == null && uiState.items.isNotEmpty()) {
            viewModel.selectItem(uiState.items.first().id)
        }
    }

    val points = uiState.points
    val average = points.map { it.normalizedUnitPrice }.average().takeIf { !it.isNaN() } ?: 0.0
    val min = points.minOfOrNull { it.normalizedUnitPrice } ?: 0.0
    val max = points.maxOfOrNull { it.normalizedUnitPrice } ?: 0.0
    val latest = points.lastOrNull()?.normalizedUnitPrice ?: 0.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Análises") })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = uiState.items.firstOrNull { it.id == uiState.selectedItemId }?.name.orEmpty(),
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            label = { Text(text = "Item para análise") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            uiState.items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(text = "${item.name} • ${item.category}") },
                                    onClick = {
                                        viewModel.selectItem(item.id)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            30 to "30 dias",
                            90 to "90 dias",
                            180 to "180 dias",
                            365 to "1 ano",
                        ).forEach { (days, label) ->
                            FilterChip(
                                selected = uiState.rangeDays == days,
                                onClick = { viewModel.updateRange(days) },
                                label = { Text(text = label) },
                            )
                        }
                        FilterChip(
                            selected = uiState.rangeDays == null,
                            onClick = { viewModel.updateRange(null) },
                            label = { Text(text = "Tudo") },
                        )
                    }
                }
            }

            if (uiState.items.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Análises indisponíveis",
                        message = "Finalize compras com preços preenchidos para construir o histórico de preços por item.",
                    )
                }
            } else {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MetricCard(title = "Preço médio base", value = average.asCurrency(), modifier = Modifier.fillMaxWidth())
                        MetricCard(title = "Menor preço base", value = min.asCurrency(), modifier = Modifier.fillMaxWidth())
                        MetricCard(title = "Maior preço base", value = max.asCurrency(), modifier = Modifier.fillMaxWidth())
                        MetricCard(title = "Último preço base", value = latest.asCurrency(), modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    SimpleLineChart(values = points.map { it.normalizedUnitPrice.toFloat() })
                }
            }

            items(points, key = { "${it.itemId}-${it.purchasedAt}" }) { point ->
                androidx.compose.material3.Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = point.itemName, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Data: ${point.purchasedAt.asDate()}")
                        Text(
                            text = if (point.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Dimensão por unidade: ${point.itemDimension?.asDecimal().orEmpty()} ${point.measurementUnit.label}"
                            } else {
                                "Compra por medida variável em ${point.measurementUnit.label}"
                            },
                        )
                        Text(
                            text = if (point.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Preço informado: ${point.unitPrice.asCurrency()} por unidade"
                            } else {
                                "Preço informado: ${point.unitPrice.asCurrency()} por ${point.measurementUnit.label}"
                            },
                        )
                        Text(text = "Preço normalizado: ${point.normalizedUnitPrice.asCurrency()} / ${point.measurementUnit.baseUnitLabel()}")
                        Text(
                            text = if (point.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Unidades compradas: ${point.quantity.asDecimal()}"
                            } else {
                                "Quantidade comprada: ${point.quantity.asDecimal()} ${point.measurementUnit.label}"
                            },
                        )
                    }
                }
            }
        }
    }
}
