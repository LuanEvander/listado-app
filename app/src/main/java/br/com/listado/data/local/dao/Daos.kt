package br.com.listado.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import br.com.listado.core.model.ListStatus
import br.com.listado.data.local.entity.ItemEntity
import br.com.listado.data.local.entity.PriceHistoryEntity
import br.com.listado.data.local.entity.ShoppingListEntity
import br.com.listado.data.local.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

data class ShoppingListSummaryRow(
    val id: Long,
    val name: String,
    val description: String,
    val budgetLimit: Double?,
    val status: ListStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val itemCount: Int,
    val purchasedCount: Int,
    val total: Double,
)

@Dao
interface ItemDao {

    @Query(
        """
        SELECT * FROM items
        WHERE (:includeInactive = 1 OR isActive = 1)
          AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY isActive DESC, name ASC
        """,
    )
    fun observeItems(query: String, includeInactive: Boolean): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT DISTINCT i.*
        FROM items i
        INNER JOIN price_history ph ON ph.catalogItemId = i.id
        ORDER BY i.name ASC
        """,
    )
    fun observeItemsWithHistory(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ItemEntity?

    @Upsert
    suspend fun upsert(item: ItemEntity): Long
}

@Dao
interface ShoppingListDao {

    @Query(
        """
        SELECT sl.id,
               sl.name,
               sl.description,
               sl.budgetLimit,
               sl.status,
               sl.createdAt,
               sl.updatedAt,
               sl.completedAt,
               COUNT(sli.id) AS itemCount,
               COALESCE(SUM(CASE WHEN sli.isChecked THEN 1 ELSE 0 END), 0) AS purchasedCount,
             COALESCE(SUM(sli.units * sli.unitPrice), 0.0) AS total
        FROM shopping_lists sl
        LEFT JOIN shopping_list_items sli ON sli.listId = sl.id
        WHERE sl.status != 'CONCLUIDO'
        GROUP BY sl.id
        ORDER BY sl.updatedAt DESC
        """,
    )
    fun observeOpenLists(): Flow<List<ShoppingListSummaryRow>>

    @Query(
        """
        SELECT sl.id,
               sl.name,
               sl.description,
               sl.budgetLimit,
               sl.status,
               sl.createdAt,
               sl.updatedAt,
               sl.completedAt,
               COUNT(sli.id) AS itemCount,
               COALESCE(SUM(CASE WHEN sli.isChecked THEN 1 ELSE 0 END), 0) AS purchasedCount,
             COALESCE(SUM(sli.units * sli.unitPrice), 0.0) AS total
        FROM shopping_lists sl
        LEFT JOIN shopping_list_items sli ON sli.listId = sl.id
        WHERE sl.status = 'CONCLUIDO'
        GROUP BY sl.id
        ORDER BY sl.completedAt DESC, sl.updatedAt DESC
        """,
    )
    fun observeCompletedLists(): Flow<List<ShoppingListSummaryRow>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ShoppingListEntity?>

    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ShoppingListEntity?

    @Upsert
    suspend fun upsert(list: ShoppingListEntity): Long
}

@Dao
interface ShoppingListItemDao {

    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId ORDER BY orderIndex ASC, id ASC")
    fun observeByListId(listId: Long): Flow<List<ShoppingListItemEntity>>

    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId ORDER BY orderIndex ASC, id ASC")
    suspend fun getByListId(listId: Long): List<ShoppingListItemEntity>

    @Query("SELECT * FROM shopping_list_items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: Long): ShoppingListItemEntity?

    @Query(
        "SELECT * FROM shopping_list_items WHERE listId = :listId AND catalogItemId = :catalogItemId LIMIT 1",
    )
    suspend fun findByListAndCatalogItem(listId: Long, catalogItemId: Long): ShoppingListItemEntity?

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM shopping_list_items WHERE listId = :listId")
    suspend fun getMaxOrderIndex(listId: Long): Int

    @Upsert
    suspend fun upsert(item: ShoppingListItemEntity): Long

    @Query("DELETE FROM shopping_list_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    @Query("DELETE FROM shopping_list_items WHERE listId = :listId AND isChecked = 0")
    suspend fun deleteUncheckedByListId(listId: Long)
}

@Dao
interface PriceHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PriceHistoryEntity>)

    @Query("DELETE FROM price_history WHERE listId = :listId")
    suspend fun deleteByListId(listId: Long)

    @Query(
        """
        SELECT * FROM price_history
        WHERE catalogItemId = :itemId
          AND (:fromEpoch IS NULL OR purchasedAt >= :fromEpoch)
        ORDER BY purchasedAt ASC
        """,
    )
    fun observeHistory(itemId: Long, fromEpoch: Long?): Flow<List<PriceHistoryEntity>>
}
