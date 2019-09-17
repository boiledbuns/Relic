package com.relic.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.relic.persistence.entities.TokenStoreEntity
import com.relic.domain.models.TokenStoreModel

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