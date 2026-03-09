package br.com.listado.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.listado.core.model.ShoppingListSummary
import br.com.listado.data.repository.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(
    shoppingListRepository: ShoppingListRepository,
) : ViewModel() {

    val uiState: StateFlow<List<ShoppingListSummary>> = shoppingListRepository.observeCompletedLists().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}
