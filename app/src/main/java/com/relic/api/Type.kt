package com.relic.api

sealed class Type(
    val name : String
) {
    object Comment : Type("T1")
    object Post : Type("T3")
}