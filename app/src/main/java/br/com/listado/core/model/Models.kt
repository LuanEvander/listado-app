package br.com.listado.core.model

enum class UnitFamily {
    MASS,
    VOLUME,
    COUNT,
}

enum class UnitMeasure(
    val label: String,
    val family: UnitFamily,
    val factorToBaseUnit: Double,
) {
    GRAMA(label = "g", family = UnitFamily.MASS, factorToBaseUnit = 1.0),
    KILOGRAMA(label = "kg", family = UnitFamily.MASS, factorToBaseUnit = 1000.0),
    MILILITRO(label = "ml", family = UnitFamily.VOLUME, factorToBaseUnit = 1.0),
    LITRO(label = "l", family = UnitFamily.VOLUME, factorToBaseUnit = 1000.0),
    UNIDADE(label = "un", family = UnitFamily.COUNT, factorToBaseUnit = 1.0),
    ;

    companion object {
        fun compatibleOptions(family: UnitFamily): List<UnitMeasure> =
            entries.filter { it.family == family }
    }
}

enum class ListStatus {
    PLANEJAMENTO,
    EXECUTANDO,
    CONCLUIDO,
}

data class CatalogItem(
    val id: Long,
    val name: String,
    val category: String,
    val description: String,
    val dimension: Double,
    val measurementUnit: UnitMeasure,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class CatalogItemForm(
    val id: Long? = null,
    val name: String,
    val category: String,
    val description: String,
    val dimension: Double,
    val measurementUnit: UnitMeasure,
)

data class ShoppingListForm(
    val id: Long? = null,
    val name: String,
    val description: String,
    val budgetLimit: Double?,
)

data class ShoppingListEntry(
    val id: Long,
    val catalogItemId: Long,
    val itemName: String,
    val category: String,
    val itemDimension: Double,
    val measurementUnit: UnitMeasure,
    val baseUnit: UnitMeasure,
    val units: Double,
    val unitPrice: Double,
    val isChecked: Boolean,
    val orderIndex: Int,
) {
    val subtotal: Double = units * unitPrice
    val contentPerUnitInBase: Double = itemDimension * measurementUnit.factorToBaseUnit
    val totalContentInBase: Double = units * contentPerUnitInBase
    val normalizedUnitPrice: Double = unitPrice / contentPerUnitInBase
}

data class ShoppingListSummary(
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

data class ShoppingListDetails(
    val id: Long,
    val name: String,
    val description: String,
    val budgetLimit: Double?,
    val status: ListStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val items: List<ShoppingListEntry>,
) {
    val total: Double = items.sumOf { it.subtotal }
    val purchasedCount: Int = items.count { it.isChecked }
    val itemCount: Int = items.size
    val canEdit: Boolean = status != ListStatus.CONCLUIDO
}

data class PricePoint(
    val itemId: Long,
    val itemName: String,
    val category: String,
    val purchasedAt: Long,
    val itemDimension: Double,
    val measurementUnit: UnitMeasure,
    val units: Double,
    val unitPrice: Double,
    val normalizedUnitPrice: Double,
)

data class DashboardStats(
    val activeItemCount: Int = 0,
    val openListCount: Int = 0,
    val completedListCount: Int = 0,
    val currentPlannedTotal: Double = 0.0,
    val totalPurchasedValue: Double = 0.0,
)

fun UnitMeasure.baseUnitLabel(): String = when (family) {
    UnitFamily.MASS -> UnitMeasure.GRAMA.label
    UnitFamily.VOLUME -> UnitMeasure.MILILITRO.label
    UnitFamily.COUNT -> UnitMeasure.UNIDADE.label
}

fun UnitMeasure.baseUnit(): UnitMeasure = when (family) {
    UnitFamily.MASS -> UnitMeasure.GRAMA
    UnitFamily.VOLUME -> UnitMeasure.MILILITRO
    UnitFamily.COUNT -> UnitMeasure.UNIDADE
}
