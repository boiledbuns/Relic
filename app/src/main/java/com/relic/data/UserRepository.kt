package com.relic.data

import com.relic.data.models.UserModel

interface UserRepository {
    suspend fun retrieveUser(username : String) : UserModel?
}