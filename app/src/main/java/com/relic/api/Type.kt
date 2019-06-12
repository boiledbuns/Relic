package com.relic.api

sealed class Type(
    val name : String
) {
    object Comment : Type("t1")
    object Post : Type("t3")
}