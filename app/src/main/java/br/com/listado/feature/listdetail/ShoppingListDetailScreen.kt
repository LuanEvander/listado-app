package br.com.listado.feature.listdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.listado.core.model.ItemPurchaseMode
import br.com.listado.core.model.ListStatus
import br.com.listado.core.model.ShoppingListEntry
import br.com.listado.core.util.asDecimal
import br.com.listado.core.util.asCurrency
import br.com.listado.core.util.toBrazilianDoubleOrNull
import br.com.listado.ui.components.CollectMessages
import br.com.listado.ui.components.EmptyStateCard
import br.com.listado.ui.components.MetricCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShoppingListDetailScreen(
    onBack: () -> Unit,
    viewModel: ShoppingListDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectMessages(messages = viewModel.events, snackbarHostState = snackbarHostState)

    var showAddDialog by remember { mutableStateOf(false) }
    var showFinalizeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = uiState.details?.name ?: "Lista") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiState.details?.canEdit == true) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Adicionar item")
                }
            }
        },
    ) { innerPadding ->
        val details = uiState.details
        if (details == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            ) {
                Text(text = "Carregando lista…")
            }
            return@Scaffold
        }
        val purchasedTotal = details.items.filter { it.isChecked }.sumOf { it.subtotal }
        val budgetReference = if (details.status == ListStatus.PLANEJAMENTO) details.total else purchasedTotal
        val budgetBalance = details.budgetLimit?.minus(budgetReference)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(text = details.description.ifBlank { "Sem descrição" }, style = MaterialTheme.typography.bodyLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            MetricCard(
                                title = "Total da lista",
                                value = details.total.asCurrency(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            MetricCard(
                                title = "Total comprado",
                                value = purchasedTotal.asCurrency(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            MetricCard(
                                title = "Itens comprados",
                                value = "${details.purchasedCount}/${details.itemCount}",
                                modifier = Modifier.fillMaxWidth(),
                            )
                            details.budgetLimit?.let { budget ->
                                MetricCard(
                                    title = "Orçamento",
                                    value = budget.asCurrency(),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                MetricCard(
                                    title = if ((budgetBalance ?: 0.0) >= 0.0) "Saldo" else "Acima do orçamento",
                                    value = (budgetBalance ?: 0.0).let { balance ->
                                        if (balance >= 0.0) balance.asCurrency() else (-balance).asCurrency()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        details.budgetLimit?.let {
                            Text(
                                text = if ((budgetBalance ?: 0.0) >= 0.0) {
                                    if (details.status == ListStatus.PLANEJAMENTO) {
                                        "Planejamento dentro do orçamento."
                                    } else {
                                        "Compra atual dentro do orçamento."
                                    }
                                } else {
                                    if (details.status == ListStatus.PLANEJAMENTO) {
                                        "Planejamento excedeu o orçamento definido."
                                    } else {
                                        "Compra atual já excedeu o orçamento definido."
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (details.status == ListStatus.PLANEJAMENTO) {
                                Button(onClick = viewModel::startList, modifier = Modifier.weight(1f)) {
                                    Text(text = "Iniciar compra")
                                }
                            }
                            if (details.status == ListStatus.EXECUTANDO) {
                                Button(onClick = { showFinalizeDialog = true }, modifier = Modifier.weight(1f)) {
                                    Text(text = "Finalizar lista")
                                }
                            }
                        }
                    }
                }
            }

            if (details.items.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Lista vazia",
                        message = "Adicione itens do catálogo para começar o planejamento desta compra.",
                    )
                }
            }

            items(details.items, key = { it.id }) { entry ->
                ShoppingListEntryCard(
                    entry = entry,
                    canEdit = details.canEdit,
                    onUpdatePurchased = { checked -> viewModel.updatePurchased(entry.id, checked) },
                    onUpdate = { quantity, price ->
                        viewModel.updatePricing(entry.id, quantity, price)
                    },
                    onRemove = { viewModel.removeItem(entry.id) },
                )
            }
        }
    }

    if (showAddDialog) {
        AddListItemDialog(
            searchQuery = uiState.catalogSearchQuery,
            items = uiState.availableItems,
            onSearchChange = viewModel::updateCatalogSearchQuery,
            onDismiss = { showAddDialog = false },
            onAdd = { id ->
                viewModel.addItem(id)
                showAddDialog = false
            },
        )
    }

    if (showFinalizeDialog) {
        FinalizeListDialog(
            onDismiss = { showFinalizeDialog = false },
            onConfirm = { removeUnchecked ->
                viewModel.finalizeList(removeUnchecked)
                showFinalizeDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ShoppingListEntryCard(
    entry: ShoppingListEntry,
    canEdit: Boolean,
    onUpdatePurchased: (Boolean) -> Unit,
    onUpdate: (Double, Double) -> Unit,
    onRemove: () -> Unit,
) {
    var isExpanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    var quantityText by rememberSaveable(entry.id) { mutableStateOf(entry.quantity.asDecimal()) }
    var priceText by rememberSaveable(entry.id) { mutableStateOf(entry.unitPrice.asDecimal()) }
    val previewQuantity = quantityText.toBrazilianDoubleOrNull() ?: entry.quantity
    val previewPrice = priceText.toBrazilianDoubleOrNull() ?: entry.unitPrice
    val previewSubtotal = previewQuantity * previewPrice

    LaunchedEffect(entry.id, isExpanded) {
        if (!isExpanded) {
            quantityText = entry.quantity.asDecimal()
            priceText = entry.unitPrice.asDecimal()
        }
    }

    LaunchedEffect(
        quantityText,
        priceText,
        entry.quantity,
        entry.unitPrice,
        isExpanded,
        canEdit,
    ) {
        if (!canEdit || !isExpanded) return@LaunchedEffect

        val parsedQuantity = quantityText.toBrazilianDoubleOrNull() ?: return@LaunchedEffect
        val parsedPrice = priceText.toBrazilianDoubleOrNull() ?: return@LaunchedEffect
        if (parsedQuantity <= 0.0 || parsedPrice < 0.0) return@LaunchedEffect
        if (parsedQuantity == entry.quantity && parsedPrice == entry.unitPrice) return@LaunchedEffect

        delay(450)
        onUpdate(parsedQuantity, parsedPrice)
    }

    Card(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = entry.itemName, style = MaterialTheme.typography.titleMedium)
                    Text(text = entry.category, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = entry.isChecked, onCheckedChange = if (canEdit) onUpdatePurchased else null)
                        Text(text = if (entry.isChecked) "Comprado" else "Pendente")
                    }
                    if (canEdit) {
                        IconButton(onClick = onRemove) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Remover item")
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { isExpanded = !isExpanded },
                    label = {
                        Text(
                            text = if (entry.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Dimensão por unidade: ${entry.itemDimension?.asDecimal().orEmpty()} ${entry.measurementUnit.label}"
                            } else {
                                "Compra por medida variável em ${entry.measurementUnit.label}"
                            },
                        )
                    },
                )
                AssistChip(
                    onClick = { isExpanded = !isExpanded },
                    label = { Text(text = "Subtotal: ${previewSubtotal.asCurrency()}") },
                )
            }

            if (isExpanded && canEdit) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = if (entry.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Unidades da compra"
                            } else {
                                "Quantidade comprada"
                            },
                        )
                    },
                    supportingText = {
                        Text(
                            text = if (entry.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Quantidade de embalagens/unidades levadas • salvamento automático"
                            } else {
                                "Quantidade total comprada em ${entry.measurementUnit.label} • salvamento automático"
                            },
                        )
                    },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = if (entry.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Preço por unidade do item"
                            } else {
                                "Preço por ${entry.measurementUnit.label}"
                            },
                        )
                    },
                    supportingText = {
                        Text(
                            text = if (entry.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                "Cada unidade contém ${entry.itemDimension?.asDecimal().orEmpty()} ${entry.measurementUnit.label} • salvamento automático"
                            } else {
                                "Ex.: preço por ${entry.measurementUnit.label} multiplicado pela quantidade comprada • salvamento automático"
                            },
                        )
                    },
                    singleLine = true,
                )
                Text(
                    text = "As alterações são salvas automaticamente após uma breve pausa na digitação.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else if (isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (entry.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                            "Quantidade comprada: ${entry.quantity.asDecimal()} unidades"
                        } else {
                            "Quantidade comprada: ${entry.quantity.asDecimal()} ${entry.measurementUnit.label}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = if (entry.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                            "Preço informado: ${entry.unitPrice.asCurrency()} por unidade"
                        } else {
                            "Preço informado: ${entry.unitPrice.asCurrency()} por ${entry.measurementUnit.label}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddListItemDialog(
    searchQuery: String,
    items: List<br.com.listado.core.model.CatalogItem>,
    onSearchChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: (Long) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Adicionar item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Pesquisar item") },
                    singleLine = true,
                )
                if (items.isEmpty()) {
                    Text(text = "Nenhum item disponível para inclusão.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(item.id) },
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = if (item.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                            "${item.category} • ${item.dimension?.asDecimal().orEmpty()} ${item.measurementUnit.label} por unidade"
                                        } else {
                                            "${item.category} • vendido por ${item.measurementUnit.label}"
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Fechar")
            }
        },
    )
}

@Composable
private fun FinalizeListDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit,
) {
    var removeUnchecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Finalizar lista") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Itens não marcados podem ser mantidos no histórico como não comprados ou removidos antes da conclusão.")
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = removeUnchecked, onCheckedChange = { removeUnchecked = it })
                    Text(text = "Remover itens pendentes antes de concluir")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(removeUnchecked) }) {
                Text(text = "Concluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}
