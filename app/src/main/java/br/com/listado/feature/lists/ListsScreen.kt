package br.com.listado.feature.lists

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.listado.core.model.ListStatus
import br.com.listado.core.model.ShoppingListForm
import br.com.listado.core.model.ShoppingListSummary
import br.com.listado.core.util.asDecimal
import br.com.listado.core.util.asCurrency
import br.com.listado.core.util.toBrazilianDoubleOrNull
import br.com.listado.ui.components.CollectMessages
import br.com.listado.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onOpenList: (Long) -> Unit,
    viewModel: ListsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    CollectMessages(messages = viewModel.events, snackbarHostState = snackbarHostState)

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editingItem = uiState.firstOrNull { it.id == editingId }
    val deletingItem = uiState.firstOrNull { it.id == deletingId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Listas em andamento") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingId = null
                showDialog = true
            }) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Criar lista")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Nenhuma lista aberta",
                        message = "Crie uma lista de compras, defina orçamento e comece o planejamento da compra.",
                    )
                }
            }

            items(uiState, key = { it.id }) { list ->
                androidx.compose.material3.Card {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(text = list.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = list.description.ifBlank { "Sem descrição" }, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${list.itemCount} itens • ${list.purchasedCount} marcados • ${list.status.name.lowercase()}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    editingId = list.id
                                    showDialog = true
                                }) {
                                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar lista")
                                }
                                IconButton(onClick = { deletingId = list.id }) {
                                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Excluir lista")
                                }
                            }
                        }

                        Text(text = "Total atual: ${list.total.asCurrency()}")
                        Text(
                            text = list.budgetLimit?.let { "Orçamento: ${it.asCurrency()}" } ?: "Sem orçamento definido",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onOpenList(list.id) }, modifier = Modifier.weight(1f)) {
                                Text(text = "Abrir")
                            }
                            if (list.status == ListStatus.PLANEJAMENTO) {
                                Button(
                                    onClick = { viewModel.startList(list.id) },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(text = "Iniciar compra")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ShoppingListDialog(
            initialItem = editingItem,
            onDismiss = { showDialog = false },
            onSave = { form ->
                viewModel.saveList(form)
                showDialog = false
            },
        )
    }

    if (deletingItem != null) {
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text(text = "Excluir lista") },
            text = {
                Text(text = "Deseja excluir a lista \"${deletingItem.name}\"? Os itens vinculados serão removidos.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteList(deletingItem.id)
                    deletingId = null
                }) {
                    Text(text = "Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) {
                    Text(text = "Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ShoppingListDialog(
    initialItem: ShoppingListSummary?,
    onDismiss: () -> Unit,
    onSave: (ShoppingListForm) -> Unit,
) {
    var name by remember(initialItem?.id) { mutableStateOf(initialItem?.name.orEmpty()) }
    var description by remember(initialItem?.id) { mutableStateOf(initialItem?.description.orEmpty()) }
    var budget by remember(initialItem?.id) {
        mutableStateOf(initialItem?.budgetLimit?.asDecimal().orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (initialItem == null) "Nova lista" else "Editar lista") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Nome") },
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Descrição") },
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Orçamento máximo (opcional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    ShoppingListForm(
                        id = initialItem?.id,
                        name = name,
                        description = description,
                        budgetLimit = budget.trim().takeIf { it.isNotBlank() }?.toBrazilianDoubleOrNull(),
                    ),
                )
            }) {
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
