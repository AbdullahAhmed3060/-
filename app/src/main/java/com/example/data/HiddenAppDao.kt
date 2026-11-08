package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenAppDao {

    @Query("SELECT * FROM hidden_apps ORDER BY hiddenAt DESC")
    fun getAllHiddenApps(): Flow<List<HiddenAppEntity>>

    @Query("SELECT packageName FROM hidden_apps")
    fun getAllHiddenPackageNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHiddenApp(app: HiddenAppEntity)

    @Delete
    suspend fun deleteHiddenApp(app: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM hidden_apps WHERE packageName = :packageName)")
    suspend fun isAppHidden(packageName: String): Boolean
}
