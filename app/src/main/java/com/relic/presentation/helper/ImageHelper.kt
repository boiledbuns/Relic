package com.relic.presentation.helper

object ImageHelper {
    private val validImageEndings = listOf("jpg", "png", "gif")

    fun isValidImage(url : String) : Boolean {
        val lastThree = url.substring(url.length - 3)

        return validImageEndings.contains(lastThree)
    }
}