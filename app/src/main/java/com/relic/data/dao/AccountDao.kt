package com.relic.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.relic.data.entities.AccountEntity
import com.relic.domain.models.AccountModel

@Dao
abstract class AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAccount(account : AccountEntity)

    @Query("DELETE FROM AccountEntity WHERE name = :username")
    abstract fun deleteAccount(username : String) : Int

    @Query("SELECT name FROM AccountEntity")
    abstract fun getAccountNames() : LiveData<List<String>>

    @Query("SELECT * FROM AccountEntity")
    abstract fun getAccounts() : LiveData<List<AccountModel>>

    @Query("SELECT * FROM AccountEntity WHERE name = :username")
    abstract fun getAccount(username : String) : LiveData<AccountModel>
}