package br.com.listado.data.repository

import androidx.room.withTransaction
import br.com.listado.core.model.CatalogItem
import br.com.listado.core.model.CatalogItemForm
import br.com.listado.core.model.DashboardStats
import br.com.listado.core.model.ItemPurchaseMode
import br.com.listado.core.model.ListStatus
import br.com.listado.core.model.PricePoint
import br.com.listado.core.model.ProductCategory
import br.com.listado.core.model.ShoppingListDetails
import br.com.listado.core.model.ShoppingListEntry
import br.com.listado.core.model.ShoppingListForm
import br.com.listado.core.model.ShoppingListSummary
import br.com.listado.core.model.baseUnit
import br.com.listado.data.local.AppDatabase
import br.com.listado.data.local.dao.ItemDao
import br.com.listado.data.local.dao.PriceHistoryDao
import br.com.listado.data.local.dao.ShoppingListDao
import br.com.listado.data.local.dao.ShoppingListItemDao
import br.com.listado.data.local.entity.ItemEntity
import br.com.listado.data.local.entity.PriceHistoryEntity
import br.com.listado.data.local.entity.ShoppingListEntity
import br.com.listado.data.local.entity.ShoppingListItemEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class CatalogRepository @Inject constructor(
    private val itemDao: ItemDao,
) {

    fun observeItems(query: String, includeInactive: Boolean): Flow<List<CatalogItem>> =
        itemDao.observeItems(query = query.trim(), includeInactive = includeInactive)
            .map { items -> items.map(ItemEntity::toModel) }

    fun observeItemsWithHistory(): Flow<List<CatalogItem>> =
        itemDao.observeItemsWithHistory().map { items -> items.map(ItemEntity::toModel) }

    suspend fun saveItem(form: CatalogItemForm) {
        require(form.name.isNotBlank()) { "Informe o nome do item." }
        require(form.category.isNotBlank()) { "Informe a categoria do item." }
        require(ProductCategory.fromLabel(form.category) != null) {
            "Selecione uma categoria válida definida pelo sistema."
        }
        if (form.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION) {
            require((form.dimension ?: 0.0) > 0.0) { "Informe uma dimensão maior que zero." }
        }

        val now = System.currentTimeMillis()
        val current = form.id?.let { itemDao.getById(it) }
        val normalizedCategory = ProductCategory.fromLabel(form.category)?.label.orEmpty()
        val entity = ItemEntity(
            id = current?.id ?: 0,
            name = form.name.trim(),
            category = normalizedCategory,
            description = form.description.trim(),
            purchaseMode = form.purchaseMode,
            dimension = form.dimension?.takeIf { form.purchaseMode == ItemPurchaseMode.FIXED_DIMENSION },
            measurementUnit = form.measurementUnit,
            isActive = current?.isActive ?: true,
            createdAt = current?.createdAt ?: now,
            updatedAt = now,
        )
        itemDao.upsert(entity)
    }

    suspend fun inactivateItem(itemId: Long) {
        val current = itemDao.getById(itemId) ?: return
        itemDao.upsert(
            current.copy(
                isActive = false,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun getItem(itemId: Long): CatalogItem? = itemDao.getById(itemId)?.toModel()
}

@Singleton
class ShoppingListRepository @Inject constructor(
    private val database: AppDatabase,
    private val shoppingListDao: ShoppingListDao,
    private val shoppingListItemDao: ShoppingListItemDao,
    private val itemDao: ItemDao,
    private val priceHistoryDao: PriceHistoryDao,
) {

    fun observeOpenLists(): Flow<List<ShoppingListSummary>> =
        shoppingListDao.observeOpenLists().map { rows -> rows.map { it.toModel() } }

    fun observeCompletedLists(): Flow<List<ShoppingListSummary>> =
        shoppingListDao.observeCompletedLists().map { rows -> rows.map { it.toModel() } }

    fun observeListDetails(listId: Long): Flow<ShoppingListDetails?> =
        combine(
            shoppingListDao.observeById(listId),
            shoppingListItemDao.observeByListId(listId),
        ) { list, items ->
            list?.toDetails(items.map(ShoppingListItemEntity::toModel))
        }

    suspend fun saveList(form: ShoppingListForm): Long {
        require(form.name.isNotBlank()) { "Informe o nome da lista." }

        val now = System.currentTimeMillis()
        val current = form.id?.let { shoppingListDao.getById(it) }
        if (current?.status == ListStatus.CONCLUIDO) {
            error("Listas concluídas são imutáveis.")
        }

        val entity = ShoppingListEntity(
            id = current?.id ?: 0,
            name = form.name.trim(),
            description = form.description.trim(),
            budgetLimit = form.budgetLimit,
            status = current?.status ?: ListStatus.PLANEJAMENTO,
            createdAt = current?.createdAt ?: now,
            updatedAt = now,
            completedAt = current?.completedAt,
        )
        return shoppingListDao.upsert(entity)
    }

    suspend fun startList(listId: Long) {
        val current = editableList(listId)
        shoppingListDao.upsert(
            current.copy(
                status = ListStatus.EXECUTANDO,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteList(listId: Long) {
        editableList(listId)
        shoppingListDao.deleteById(listId)
    }

    suspend fun addItem(listId: Long, catalogItemId: Long) {
        val list = editableList(listId)
        val catalogItem = itemDao.getById(catalogItemId)
            ?.takeIf { it.isActive }
            ?: error("Item indisponível para inclusão na lista.")
        val existing = shoppingListItemDao.findByListAndCatalogItem(listId, catalogItemId)
        require(existing == null) { "Este item já está presente na lista." }

        shoppingListItemDao.upsert(
            ShoppingListItemEntity(
                listId = list.id,
                catalogItemId = catalogItem.id,
                itemNameSnapshot = catalogItem.name,
                categorySnapshot = catalogItem.category,
                purchaseModeSnapshot = catalogItem.purchaseMode,
                itemDimensionSnapshot = catalogItem.dimension,
                measurementUnitSnapshot = catalogItem.measurementUnit,
                baseUnit = catalogItem.measurementUnit.baseUnit(),
                quantity = 1.0,
                unitPrice = 0.0,
                isChecked = false,
                orderIndex = shoppingListItemDao.getMaxOrderIndex(listId) + 1,
            ),
        )
        touchList(list)
    }

    suspend fun removeItem(itemId: Long) {
        val entry = shoppingListItemDao.getById(itemId) ?: return
        editableList(entry.listId)
        shoppingListItemDao.deleteById(itemId)
        shoppingListDao.getById(entry.listId)?.let { touchList(it) }
    }

    suspend fun updateQuantity(itemId: Long, quantity: Double) {
        require(quantity > 0.0) { "A quantidade deve ser maior que zero." }
        val current = editableEntry(itemId)
        shoppingListItemDao.upsert(current.copy(quantity = quantity))
        shoppingListDao.getById(current.listId)?.let { touchList(it) }
    }

    suspend fun updatePricing(itemId: Long, quantity: Double, unitPrice: Double) {
        require(quantity > 0.0) { "A quantidade deve ser maior que zero." }
        require(unitPrice >= 0.0) { "O preço unitário não pode ser negativo." }

        val current = editableEntry(itemId)
        shoppingListItemDao.upsert(
            current.copy(
                quantity = quantity,
                unitPrice = unitPrice,
            ),
        )
        shoppingListDao.getById(current.listId)?.let { touchList(it) }
    }

    suspend fun updateUnitPrice(itemId: Long, unitPrice: Double) {
        require(unitPrice >= 0.0) { "O preço unitário não pode ser negativo." }
        val current = editableEntry(itemId)
        shoppingListItemDao.upsert(current.copy(unitPrice = unitPrice))
        shoppingListDao.getById(current.listId)?.let { touchList(it) }
    }

    suspend fun updatePurchased(itemId: Long, isChecked: Boolean) {
        val current = editableEntry(itemId)
        shoppingListItemDao.upsert(current.copy(isChecked = isChecked))
        shoppingListDao.getById(current.listId)?.let { touchList(it) }
    }

    suspend fun finalizeList(listId: Long, removeUnchecked: Boolean) {
        val list = editableList(listId)
        require(list.status == ListStatus.EXECUTANDO) {
            "Somente listas em execução podem ser concluídas."
        }

        database.withTransaction {
            val currentItems = shoppingListItemDao.getByListId(listId)
            val checkedItems = currentItems.filter { it.isChecked }
            require(checkedItems.isNotEmpty()) {
                "Marque pelo menos um item comprado antes de concluir a lista."
            }

            if (removeUnchecked) {
                shoppingListItemDao.deleteUncheckedByListId(listId)
            }

            val finalItems = if (removeUnchecked) checkedItems else shoppingListItemDao.getByListId(listId)
            val completedAt = System.currentTimeMillis()

            shoppingListDao.upsert(
                list.copy(
                    status = ListStatus.CONCLUIDO,
                    updatedAt = completedAt,
                    completedAt = completedAt,
                ),
            )

            priceHistoryDao.deleteByListId(listId)
            val projection = finalItems
                .filter { it.isChecked }
                .map { entry ->
                    PriceHistoryEntity(
                        catalogItemId = entry.catalogItemId,
                        listId = listId,
                        itemNameSnapshot = entry.itemNameSnapshot,
                        categorySnapshot = entry.categorySnapshot,
                        purchasedAt = completedAt,
                        purchaseModeSnapshot = entry.purchaseModeSnapshot,
                        itemDimensionSnapshot = entry.itemDimensionSnapshot,
                        measurementUnitSnapshot = entry.measurementUnitSnapshot,
                        quantity = entry.quantity,
                        unitPrice = entry.unitPrice,
                        normalizedUnitPrice = when (entry.purchaseModeSnapshot) {
                            ItemPurchaseMode.FIXED_DIMENSION -> entry.unitPrice /
                                ((entry.itemDimensionSnapshot ?: 0.0) * entry.measurementUnitSnapshot.factorToBaseUnit)
                            ItemPurchaseMode.VARIABLE_MEASURE -> entry.unitPrice / entry.measurementUnitSnapshot.factorToBaseUnit
                        },
                    )
                }
            priceHistoryDao.insertAll(projection)
        }
    }

    private suspend fun editableList(listId: Long): ShoppingListEntity {
        val current = shoppingListDao.getById(listId) ?: error("Lista não encontrada.")
        require(current.status != ListStatus.CONCLUIDO) { "Listas concluídas são imutáveis." }
        return current
    }

    private suspend fun editableEntry(itemId: Long): ShoppingListItemEntity {
        val entry = shoppingListItemDao.getById(itemId) ?: error("Item da lista não encontrado.")
        editableList(entry.listId)
        return entry
    }

    private suspend fun touchList(list: ShoppingListEntity) {
        shoppingListDao.upsert(list.copy(updatedAt = System.currentTimeMillis()))
    }
}

@Singleton
class DashboardRepository @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val shoppingListRepository: ShoppingListRepository,
) {

    fun observeStats(): Flow<DashboardStats> =
        combine(
            catalogRepository.observeItems(query = "", includeInactive = false),
            shoppingListRepository.observeOpenLists(),
            shoppingListRepository.observeCompletedLists(),
        ) { items, openLists, completedLists ->
            DashboardStats(
                activeItemCount = items.count { it.isActive },
                openListCount = openLists.size,
                completedListCount = completedLists.size,
                currentPlannedTotal = openLists.sumOf { it.total },
                totalPurchasedValue = completedLists.sumOf { it.total },
            )
        }
}

@Singleton
class AnalyticsRepository @Inject constructor(
    private val priceHistoryDao: PriceHistoryDao,
) {

    fun observeHistory(itemId: Long, fromEpoch: Long?): Flow<List<PricePoint>> =
        priceHistoryDao.observeHistory(itemId = itemId, fromEpoch = fromEpoch)
            .map { items -> items.map(PriceHistoryEntity::toModel) }
}

private fun ItemEntity.toModel(): CatalogItem = CatalogItem(
    id = id,
    name = name,
    category = category,
    description = description,
    purchaseMode = purchaseMode,
    dimension = dimension,
    measurementUnit = measurementUnit,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun ShoppingListItemEntity.toModel(): ShoppingListEntry = ShoppingListEntry(
    id = id,
    catalogItemId = catalogItemId,
    itemName = itemNameSnapshot,
    category = categorySnapshot,
    purchaseMode = purchaseModeSnapshot,
    itemDimension = itemDimensionSnapshot,
    measurementUnit = measurementUnitSnapshot,
    baseUnit = baseUnit,
    quantity = quantity,
    unitPrice = unitPrice,
    isChecked = isChecked,
    orderIndex = orderIndex,
)

private fun br.com.listado.data.local.dao.ShoppingListSummaryRow.toModel(): ShoppingListSummary = ShoppingListSummary(
    id = id,
    name = name,
    description = description,
    budgetLimit = budgetLimit,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    itemCount = itemCount,
    purchasedCount = purchasedCount,
    total = total,
)

private fun ShoppingListEntity.toDetails(items: List<ShoppingListEntry>): ShoppingListDetails = ShoppingListDetails(
    id = id,
    name = name,
    description = description,
    budgetLimit = budgetLimit,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    items = items,
)

private fun PriceHistoryEntity.toModel(): PricePoint = PricePoint(
    itemId = catalogItemId,
    itemName = itemNameSnapshot,
    category = categorySnapshot,
    purchasedAt = purchasedAt,
    purchaseMode = purchaseModeSnapshot,
    itemDimension = itemDimensionSnapshot,
    measurementUnit = measurementUnitSnapshot,
    quantity = quantity,
    unitPrice = unitPrice,
    normalizedUnitPrice = normalizedUnitPrice,
)
