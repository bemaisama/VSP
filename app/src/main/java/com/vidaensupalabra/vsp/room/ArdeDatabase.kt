// ArdeDatabase.kt
package com.vidaensupalabra.vsp.room


import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

// ------------------ Room -------------------- //
@Entity(tableName = "arde_data")
data class ArdeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val month: Int,
    val day: Int,
    val reference: String,
    val devocional: String
)

@Dao
interface ArdeDao {
    @Query("SELECT * FROM arde_data")
    fun getAll(): List<ArdeEntity>

    @Insert
    fun insertAll(vararg ardes: ArdeEntity)

    @Query("SELECT COUNT(*) FROM arde_data")
    fun count(): Int

    @Query("SELECT * FROM arde_data WHERE year = :year AND month = :month AND day = :day")
    suspend fun findByDate(year: Int, month: Int, day: Int): List<ArdeEntity>

    @Update
    suspend fun updateArde(arde: ArdeEntity)
}

@Database(entities = [ArdeEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ardeDao(): ArdeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arde_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

