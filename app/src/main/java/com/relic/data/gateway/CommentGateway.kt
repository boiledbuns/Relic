package com.relic.data.gateway

interface CommentGateway {
    suspend fun voteOnComment(fullname: String, voteStatus: Int)
}