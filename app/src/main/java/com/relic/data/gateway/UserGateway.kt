package com.relic.data.gateway

interface UserGateway {
    suspend fun getUser(username: String)
}
