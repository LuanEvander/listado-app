package br.com.listado.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.listado.core.model.CatalogItem
import br.com.listado.core.model.CatalogItemForm
import br.com.listado.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CatalogUiState(
    val searchQuery: String = "",
    val includeInactive: Boolean = false,
    val items: List<CatalogItem> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val includeInactive = MutableStateFlow(false)

    private val itemsFlow = combine(searchQuery, includeInactive) { query, inactive ->
        query to inactive
    }.flatMapLatest { (query, inactive) ->
        catalogRepository.observeItems(query = query, includeInactive = inactive)
    }

    val events = MutableSharedFlow<String>()

    val uiState: StateFlow<CatalogUiState> = combine(
        searchQuery,
        includeInactive,
        itemsFlow,
    ) { query, inactive, items ->
        CatalogUiState(
            searchQuery = query,
            includeInactive = inactive,
            items = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState(),
    )

    fun updateSearchQuery(value: String) {
        searchQuery.value = value
    }

    fun updateIncludeInactive(value: Boolean) {
        includeInactive.value = value
    }

    fun saveItem(form: CatalogItemForm) {
        viewModelScope.launch {
            runCatching {
                catalogRepository.saveItem(form)
            }.onSuccess {
                events.emit(if (form.id == null) "Item cadastrado." else "Item atualizado.")
            }.onFailure { error ->
                events.emit(error.message ?: "Não foi possível salvar o item.")
            }
        }
    }

    fun inactivateItem(itemId: Long) {
        viewModelScope.launch {
            runCatching {
                catalogRepository.inactivateItem(itemId)
            }.onSuccess {
                events.emit("Item inativado com sucesso.")
            }.onFailure { error ->
                events.emit(error.message ?: "Não foi possível inativar o item.")
            }
        }
    }
}
