package com.relic.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.relic.persistence.entities.AccountEntity
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