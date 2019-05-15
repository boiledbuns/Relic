package com.relic.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.relic.data.entities.AccountEntity
import com.relic.data.models.UserModel

@Dao
abstract class AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAuthenticatedUser(account : AccountEntity)

    @Query("DELETE FROM AccountEntity WHERE name = :username")
    abstract fun deleteAuthenticatedUser(username : String) : Int
}