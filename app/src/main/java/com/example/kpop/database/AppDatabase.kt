package com.example.kpop.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kpop.model.CartEntity
import com.example.kpop.model.Order
import com.example.kpop.model.Product
import com.example.kpop.model.Review
import com.example.kpop.model.User

@Database(
    entities = [Product::class, CartEntity::class , User::class, Order::class, Review::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao

    abstract fun userDao(): UserDao

    abstract fun orderDao(): OrderDao

    abstract fun reviewDao(): ReviewDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kpop_database"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}