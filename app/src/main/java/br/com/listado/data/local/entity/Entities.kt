package br.com.listado.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.listado.core.model.ListStatus
import br.com.listado.core.model.UnitMeasure

@Entity(
    tableName = "items",
    indices = [Index(value = ["name"]), Index(value = ["category"])],
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val description: String,
    val defaultUnit: UnitMeasure,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "shopping_lists",
    indices = [Index(value = ["status"]), Index(value = ["updatedAt"])],
)
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val budgetLimit: Double?,
    val status: ListStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
)

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["catalogItemId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["listId"]), Index(value = ["catalogItemId"]), Index(value = ["orderIndex"])],
)
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val catalogItemId: Long,
    val itemNameSnapshot: String,
    val categorySnapshot: String,
    val selectedUnit: UnitMeasure,
    val baseUnit: UnitMeasure,
    val quantity: Double,
    val unitPrice: Double,
    val isChecked: Boolean,
    val orderIndex: Int,
)

@Entity(
    tableName = "price_history",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["catalogItemId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["catalogItemId"]), Index(value = ["purchasedAt"]), Index(value = ["listId"])],
)
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogItemId: Long,
    val listId: Long,
    val itemNameSnapshot: String,
    val categorySnapshot: String,
    val purchasedAt: Long,
    val selectedUnit: UnitMeasure,
    val quantity: Double,
    val unitPrice: Double,
    val normalizedUnitPrice: Double,
)
