// room_multimedia.kt

package com.vidaensupalabra.vsp.room

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Entity(tableName = "multimedia")
data class MultimediaEntity(
    @ColumnInfo val name: String,
    @PrimaryKey val url: String, // URL remota
    @ColumnInfo val mimeType: String,
    @ColumnInfo val localPath: String?, // Ruta local en disco, si existe
    @ColumnInfo val date: String? = null
)

@Dao
interface MultimediaDao {
    @Query("SELECT * FROM multimedia")
    fun getAllFlow(): Flow<List<MultimediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MultimediaEntity>)

    @Query("DELETE FROM multimedia")
    suspend fun deleteAll()
}

@Database(entities = [MultimediaEntity::class], version = 1)
abstract class MultimediaDatabase : RoomDatabase() {
    abstract fun multimediaDao(): MultimediaDao

    companion object {
        @Volatile
        private var INSTANCE: MultimediaDatabase? = null

        fun getInstance(context: Context): MultimediaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MultimediaDatabase::class.java,
                    "multimedia_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Serializable
data class MultimediaItem(
    val name: String,
    val url: String,
    val mimeType: String,
    val date :String,
    val localPath: String? = null
)
