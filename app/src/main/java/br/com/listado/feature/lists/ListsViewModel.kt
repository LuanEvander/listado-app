package br.com.listado.feature.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.listado.core.model.ShoppingListForm
import br.com.listado.core.model.ShoppingListSummary
import br.com.listado.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
) : ViewModel() {

    val events = MutableSharedFlow<String>()

    val uiState: StateFlow<List<ShoppingListSummary>> = shoppingListRepository.observeOpenLists()
        .map { items -> items }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun saveList(form: ShoppingListForm) {
        viewModelScope.launch {
            runCatching {
                shoppingListRepository.saveList(form)
            }.onSuccess {
                events.emit(if (form.id == null) "Lista criada." else "Lista atualizada.")
            }.onFailure { error ->
                events.emit(error.message ?: "Não foi possível salvar a lista.")
            }
        }
    }

    fun startList(listId: Long) {
        viewModelScope.launch {
            runCatching {
                shoppingListRepository.startList(listId)
            }.onSuccess {
                events.emit("Lista iniciada para execução.")
            }.onFailure { error ->
                events.emit(error.message ?: "Não foi possível iniciar a lista.")
            }
        }
    }
}
