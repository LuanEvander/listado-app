package br.com.listado.feature.listdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.listado.core.model.ListStatus
import br.com.listado.core.model.ShoppingListEntry
import br.com.listado.core.model.UnitMeasure
import br.com.listado.core.util.asCurrency
import br.com.listado.core.util.asDateTime
import br.com.listado.core.util.toBrazilianDoubleOrNull
import br.com.listado.ui.components.CollectMessages
import br.com.listado.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
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
                        Text(text = "Status: ${details.status.name.lowercase()}")
                        Text(text = "Total atual: ${details.total.asCurrency()}")
                        Text(text = details.budgetLimit?.let { "Orçamento: ${it.asCurrency()}" } ?: "Sem orçamento definido")
                        Text(text = "Itens marcados: ${details.purchasedCount}/${details.itemCount}")
                        Text(text = "Última atualização: ${details.updatedAt.asDateTime()}")
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
                    onUpdate = { quantity, price, unit ->
                        viewModel.updateQuantity(entry.id, quantity)
                        viewModel.updateUnitPrice(entry.id, price)
                        viewModel.updateSelectedUnit(entry.id, unit)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingListEntryCard(
    entry: ShoppingListEntry,
    canEdit: Boolean,
    onUpdatePurchased: (Boolean) -> Unit,
    onUpdate: (Double, Double, UnitMeasure) -> Unit,
    onRemove: () -> Unit,
) {
    var quantityText by remember(entry.id, entry.quantity) { mutableStateOf(entry.quantity.toString()) }
    var priceText by remember(entry.id, entry.unitPrice) { mutableStateOf(entry.unitPrice.toString()) }
    var selectedUnit by remember(entry.id, entry.selectedUnit) { mutableStateOf(entry.selectedUnit) }
    var expanded by remember { mutableStateOf(false) }

    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = entry.itemName, style = MaterialTheme.typography.titleMedium)
                    Text(text = entry.category, style = MaterialTheme.typography.bodyMedium)
                }
                if (canEdit) {
                    IconButton(onClick = onRemove) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Remover item")
                    }
                }
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = entry.isChecked, onCheckedChange = if (canEdit) onUpdatePurchased else null)
                Text(text = if (entry.isChecked) "Comprado" else "Pendente")
            }

            AssistChip(onClick = {}, label = { Text(text = "Preço base: ${entry.normalizedUnitPrice.asCurrency()} / ${entry.baseUnit.label}") })
            AssistChip(onClick = {}, label = { Text(text = "Subtotal: ${entry.subtotal.asCurrency()}") })

            if (canEdit) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Quantidade") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Preço por ${selectedUnit.label}") },
                    singleLine = true,
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedUnit.label,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        label = { Text(text = "Unidade da compra") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        UnitMeasure.compatibleOptions(entry.baseUnit.family).forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(text = unit.label) },
                                onClick = {
                                    selectedUnit = unit
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        val quantity = quantityText.toBrazilianDoubleOrNull() ?: return@Button
                        val price = priceText.toBrazilianDoubleOrNull() ?: return@Button
                        onUpdate(quantity, price, selectedUnit)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Atualizar item")
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
                                    Text(text = "${item.category} • ${item.defaultUnit.label}")
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
