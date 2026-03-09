package br.com.listado.feature.listdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.listado.core.model.CatalogItem
import br.com.listado.core.model.ShoppingListDetails
import br.com.listado.data.repository.CatalogRepository
import br.com.listado.data.repository.ShoppingListRepository
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

data class ShoppingListDetailUiState(
    val details: ShoppingListDetails? = null,
    val catalogSearchQuery: String = "",
    val availableItems: List<CatalogItem> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shoppingListRepository: ShoppingListRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val listId: Long = checkNotNull(savedStateHandle["listId"])
    private val catalogSearchQuery = MutableStateFlow("")
    val events = MutableSharedFlow<String>()

    private val detailsFlow = shoppingListRepository.observeListDetails(listId)

    private val availableItemsFlow = combine(detailsFlow, catalogSearchQuery) { details, query ->
        details to query
    }.flatMapLatest { (details, query) ->
        catalogRepository.observeItems(query = query, includeInactive = false)
            .combine(MutableStateFlow(details)) { items, latestDetails ->
                val selectedIds = latestDetails?.items?.map { it.catalogItemId }?.toSet().orEmpty()
                items.filter { it.id !in selectedIds }
            }
    }

    val uiState: StateFlow<ShoppingListDetailUiState> = combine(
        detailsFlow,
        catalogSearchQuery,
        availableItemsFlow,
    ) { details, query, availableItems ->
        ShoppingListDetailUiState(
            details = details,
            catalogSearchQuery = query,
            availableItems = availableItems,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingListDetailUiState(),
    )

    fun updateCatalogSearchQuery(value: String) {
        catalogSearchQuery.value = value
    }

    fun addItem(catalogItemId: Long) {
        viewModelScope.launch {
            runCatching {
                shoppingListRepository.addItem(listId, catalogItemId)
            }.onSuccess {
                events.emit("Item adicionado à lista.")
            }.onFailure { error ->
                events.emit(error.message ?: "Não foi possível adicionar o item.")
            }
        }
    }

    fun updateQuantity(itemId: Long, quantity: Double) {
        executeMutation("Quantidade atualizada.") {
            shoppingListRepository.updateQuantity(itemId, quantity)
        }
    }

    fun updatePricing(itemId: Long, quantity: Double, unitPrice: Double) {
        executeMutation("Quantidade e preço atualizados.") {
            shoppingListRepository.updatePricing(itemId, quantity, unitPrice)
        }
    }

    fun updateUnitPrice(itemId: Long, unitPrice: Double) {
        executeMutation("Preço unitário atualizado.") {
            shoppingListRepository.updateUnitPrice(itemId, unitPrice)
        }
    }

    fun updatePurchased(itemId: Long, isChecked: Boolean) {
        executeMutation("Situação do item atualizada.") {
            shoppingListRepository.updatePurchased(itemId, isChecked)
        }
    }

    fun removeItem(itemId: Long) {
        executeMutation("Item removido da lista.") {
            shoppingListRepository.removeItem(itemId)
        }
    }

    fun startList() {
        executeMutation("Lista movida para execução.") {
            shoppingListRepository.startList(listId)
        }
    }

    fun finalizeList(removeUnchecked: Boolean) {
        executeMutation("Lista finalizada com sucesso.") {
            shoppingListRepository.finalizeList(listId = listId, removeUnchecked = removeUnchecked)
        }
    }

    private fun executeMutation(successMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }
                .onSuccess { events.emit(successMessage) }
                .onFailure { error -> events.emit(error.message ?: "Operação não concluída.") }
        }
    }
}
