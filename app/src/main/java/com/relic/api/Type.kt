package com.relic.api

sealed class Type(
    val name : String
) {
    object Post : Type("t3")
    object Comment : Type("t1")
    object More : Type("more")
}