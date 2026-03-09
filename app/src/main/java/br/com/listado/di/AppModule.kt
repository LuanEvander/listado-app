package br.com.listado.di

import android.content.Context
import androidx.room.Room
import br.com.listado.data.local.AppDatabase
import br.com.listado.data.local.dao.ItemDao
import br.com.listado.data.local.dao.PriceHistoryDao
import br.com.listado.data.local.dao.ShoppingListDao
import br.com.listado.data.local.dao.ShoppingListItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "listado.db",
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideShoppingListDao(database: AppDatabase): ShoppingListDao = database.shoppingListDao()

    @Provides
    fun provideShoppingListItemDao(database: AppDatabase): ShoppingListItemDao = database.shoppingListItemDao()

    @Provides
    fun providePriceHistoryDao(database: AppDatabase): PriceHistoryDao = database.priceHistoryDao()
}
