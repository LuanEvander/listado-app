package br.com.listado.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import br.com.listado.core.model.ListStatus
import br.com.listado.core.model.UnitMeasure
import br.com.listado.data.local.dao.ItemDao
import br.com.listado.data.local.dao.PriceHistoryDao
import br.com.listado.data.local.dao.ShoppingListDao
import br.com.listado.data.local.dao.ShoppingListItemDao
import br.com.listado.data.local.entity.ItemEntity
import br.com.listado.data.local.entity.PriceHistoryEntity
import br.com.listado.data.local.entity.ShoppingListEntity
import br.com.listado.data.local.entity.ShoppingListItemEntity

class AppConverters {

    @TypeConverter
    fun fromUnitMeasure(value: UnitMeasure): String = value.name

    @TypeConverter
    fun toUnitMeasure(value: String): UnitMeasure = UnitMeasure.valueOf(value)

    @TypeConverter
    fun fromListStatus(value: ListStatus): String = value.name

    @TypeConverter
    fun toListStatus(value: String): ListStatus = ListStatus.valueOf(value)
}

@Database(
    entities = [
        ItemEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        PriceHistoryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun priceHistoryDao(): PriceHistoryDao
}
