package com.relic.data

import android.arch.persistence.room.TypeConverter

class TypeConverters {
    @TypeConverter
    fun fromMoreList(moreList: List<String>?): String? {
        if (moreList == null) return null

        var stringForm = ""
        for (child in moreList) {
            stringForm += "$child,"
        }
        if (stringForm.isNotEmpty()) stringForm.dropLast(1)

        return stringForm
    }

    @TypeConverter
    fun toMoreList(moreList: String?): List<String>? {
        if (moreList == null) return null

        val children = ArrayList<String>()
        moreList.iterator().apply {
            var currentToken = ""
            var currChar: Char

            while (hasNext()) {
                currChar = nextChar()

                if (currChar == ',') {
                    children.add(currentToken)
                    currentToken = ""
                } else {
                    currentToken += currChar
                }
            }

            children.add(currentToken)
        }

        return children
    }
}