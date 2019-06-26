package com.relic.data

import android.arch.persistence.room.TypeConverter
import com.relic.domain.models.Gildings
import com.squareup.moshi.Moshi
import java.util.*
import javax.inject.Inject

class TypeConverters {

    val gildingsAdapter = moshi.adapter(Gildings::class.java)!!

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

    @TypeConverter
    fun fromDate(date : Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp:  Long?): Date? {
        return timestamp?.let { Date(it) }
    }


    @TypeConverter
    fun fromGildings(gildings: Gildings?): String? {
        return when (gildings) {
            null -> null
            else -> gildingsAdapter.toJson(gildings)
        }
    }

    @TypeConverter
    fun toGildings(gildings:  String?): Gildings? {
        return gildings?.let { gildingsAdapter.fromJson(it)}
    }

    companion object {
        lateinit var moshi : Moshi
    }
}