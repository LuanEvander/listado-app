package br.com.listado.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.listado.core.model.CatalogItem
import br.com.listado.core.model.PricePoint
import br.com.listado.data.repository.AnalyticsRepository
import br.com.listado.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class AnalyticsUiState(
    val items: List<CatalogItem> = emptyList(),
    val selectedItemId: Long? = null,
    val rangeDays: Int? = 90,
    val points: List<PricePoint> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    catalogRepository: CatalogRepository,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val selectedItemId = MutableStateFlow<Long?>(null)
    private val rangeDays = MutableStateFlow<Int?>(90)
    private val itemsFlow = catalogRepository.observeItemsWithHistory()

    private val historyFlow = combine(selectedItemId, rangeDays) { itemId, days -> itemId to days }
        .flatMapLatest { (itemId, days) ->
            if (itemId == null) {
                flowOf(emptyList())
            } else {
                val fromEpoch = days?.let {
                    Instant.now().minusSeconds(it.toLong() * 24L * 60L * 60L).toEpochMilli()
                }
                analyticsRepository.observeHistory(itemId = itemId, fromEpoch = fromEpoch)
            }
        }

    val uiState: StateFlow<AnalyticsUiState> = combine(
        itemsFlow,
        selectedItemId,
        rangeDays,
        historyFlow,
    ) { items, selectedId, days, points ->
        AnalyticsUiState(
            items = items,
            selectedItemId = selectedId,
            rangeDays = days,
            points = points,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalyticsUiState(),
    )

    fun selectItem(itemId: Long) {
        selectedItemId.value = itemId
    }

    fun updateRange(days: Int?) {
        rangeDays.value = days
    }
}
