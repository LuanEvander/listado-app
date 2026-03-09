package br.com.listado.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import br.com.listado.core.util.asDateTime
import br.com.listado.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpenList: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Histórico") })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Nada por aqui ainda",
                        message = "Ao finalizar listas, elas serão preservadas aqui como compras concluídas e imutáveis.",
                    )
                }
            }

            items(uiState, key = { it.id }) { list ->
                androidx.compose.material3.Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = list.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = list.description.ifBlank { "Sem descrição" })
                        Text(text = "Concluída em: ${list.completedAt?.asDateTime().orEmpty()}")
                        Text(text = "Total consolidado: ${list.total.asCurrency()}")
                        Text(text = "Itens comprados: ${list.purchasedCount}/${list.itemCount}")
                        Button(onClick = { onOpenList(list.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Abrir detalhamento")
                        }
                    }
                }
            }
        }
    }
}
