package com.relic.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.relic.data.entities.TokenStoreEntity
import com.relic.data.models.TokenStoreModel

@Dao
abstract class TokenStoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTokenStore(token : TokenStoreEntity)

    @Query("SELECT * FROM TokenStoreEntity WHERE accountName = :accountName")
    abstract fun getTokenStoreLiveData(accountName : String) : LiveData<TokenStoreModel>

    @Query("SELECT * FROM TokenStoreEntity WHERE accountName = :accountName")
    abstract fun getTokenStore(accountName : String) : TokenStoreModel

    @Query("UPDATE TokenStoreEntity SET access = :newAccessToken WHERE accountName = :accountName")
    abstract fun updateAccessToken(accountName : String, newAccessToken : String)
}