package br.com.listado.feature.catalog

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.listado.core.model.CatalogItem
import br.com.listado.core.model.CatalogItemForm
import br.com.listado.core.model.ItemPurchaseMode
import br.com.listado.core.model.ProductCategory
import br.com.listado.core.model.UnitMeasure
import br.com.listado.core.util.asDecimal
import br.com.listado.core.util.toBrazilianDoubleOrNull
import br.com.listado.ui.components.CollectMessages
import br.com.listado.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectMessages(messages = viewModel.events, snackbarHostState = snackbarHostState)

    var dialogItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val currentItem = uiState.items.firstOrNull { it.id == dialogItemId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Catálogo") },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dialogItemId = null
                showDialog = true
            }) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Adicionar item")
            }
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
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Pesquisar por nome ou categoria") },
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "Exibir itens inativos", style = MaterialTheme.typography.bodyMedium)
                        Checkbox(
                            checked = uiState.includeInactive,
                            onCheckedChange = viewModel::updateIncludeInactive,
                        )
                    }
                }
            }

            if (uiState.items.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Nenhum item encontrado",
                        message = "Cadastre itens com nome, categoria e modo de compra para começar a montar listas.",
                    )
                }
            }

            items(uiState.items, key = { it.id }) { item ->
                androidx.compose.material3.Card {
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
                                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = item.category, style = MaterialTheme.typography.bodyMedium)
                                if (item.description.isNotBlank()) {
                                    Text(text = item.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    dialogItemId = item.id
                                    showDialog = true
                                }) {
                                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar")
                                }
                                if (item.isActive) {
                                    IconButton(onClick = { viewModel.inactivateItem(item.id) }) {
                                        Icon(imageVector = Icons.Outlined.VisibilityOff, contentDescription = "Inativar")
                                    }
                                }
                            }
                        }
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = if (item.isActive) {
                                        if (item.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                            "Dimensão: ${item.dimension?.asDecimal().orEmpty()} ${item.measurementUnit.label} por unidade"
                                        } else {
                                            "Venda por ${item.measurementUnit.label} sem dimensão fixa"
                                        }
                                    } else {
                                        if (item.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                            "Inativo • ${item.dimension?.asDecimal().orEmpty()} ${item.measurementUnit.label} por unidade"
                                        } else {
                                            "Inativo • venda por ${item.measurementUnit.label}"
                                        }
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CatalogItemDialog(
            initialItem = currentItem,
            onDismiss = { showDialog = false },
            onSave = { form ->
                viewModel.saveItem(form)
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogItemDialog(
    initialItem: CatalogItem?,
    onDismiss: () -> Unit,
    onSave: (CatalogItemForm) -> Unit,
) {
    var name by remember(initialItem?.id) { mutableStateOf(initialItem?.name.orEmpty()) }
    var purchaseMode by remember(initialItem?.id) {
        mutableStateOf(initialItem?.purchaseMode ?: ItemPurchaseMode.FIXED_DIMENSION)
    }
    var selectedCategory by remember(initialItem?.id) {
        mutableStateOf(
            ProductCategory.fromLabel(initialItem?.category.orEmpty()) ?: ProductCategory.BEBIDAS,
        )
    }
    var description by remember(initialItem?.id) { mutableStateOf(initialItem?.description.orEmpty()) }
    var dimension by remember(initialItem?.id) { mutableStateOf(initialItem?.dimension?.toString().orEmpty()) }
    var selectedUnit by remember(initialItem?.id) { mutableStateOf(initialItem?.measurementUnit ?: UnitMeasure.UNIDADE) }
    var modeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (initialItem == null) "Novo item" else "Editar item")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Nome") },
                    singleLine = true,
                )
                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = !modeExpanded },
                ) {
                    OutlinedTextField(
                        value = purchaseMode.label,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        label = { Text(text = "Modo de compra") },
                        supportingText = {
                            Text(
                                text = if (purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                    "Use para embalagens e unidades com conteúdo fixo"
                                } else {
                                    "Use para itens comprados por peso, volume ou medida variável"
                                },
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded)
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false },
                    ) {
                        ItemPurchaseMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(text = mode.label) },
                                onClick = {
                                    purchaseMode = mode
                                    if (mode == ItemPurchaseMode.VARIABLE_MEASURE) {
                                        dimension = ""
                                    }
                                    modeExpanded = false
                                },
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedCategory.label,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        label = { Text(text = "Categoria") },
                        supportingText = { Text(text = "Categorias são definidas pelo sistema") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        ProductCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category.label) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Descrição") },
                )
                if (purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                    OutlinedTextField(
                        value = dimension,
                        onValueChange = { dimension = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Dimensão por unidade") },
                        supportingText = { Text(text = "Ex.: refrigerante 2 litros, pacote com 12 unidades") },
                        singleLine = true,
                    )
                }
                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }) {
                    OutlinedTextField(
                        value = selectedUnit.label,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        label = { Text(text = "Unidade padrão") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        UnitMeasure.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(text = "${unit.label} • ${unit.family.name.lowercase()}") },
                                onClick = {
                                    selectedUnit = unit
                                    unitExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        CatalogItemForm(
                            id = initialItem?.id,
                            name = name,
                            category = selectedCategory.label,
                            description = description,
                            purchaseMode = purchaseMode,
                            dimension = if (purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
                                dimension.toBrazilianDoubleOrNull()
                            } else {
                                null
                            },
                            measurementUnit = selectedUnit,
                        ),
                    )
                },
            ) {
                Text(text = "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}
