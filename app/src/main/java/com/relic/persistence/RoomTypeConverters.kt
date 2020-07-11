package com.relic.persistence

import androidx.room.TypeConverter
import com.relic.domain.models.Award
import com.relic.domain.models.Gildings
import com.relic.domain.models.MediaList
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.*

/**
 * handles conversion of complex fields for db serialization
 */
class RoomTypeConverters {

    private val gildingsAdapter = moshi.adapter(Gildings::class.java)!!
    private val mediaListAdapter = moshi.adapter(MediaList::class.java)!!

    private val awards = Types.newParameterizedType(List::class.java, Award::class.java)
    private val awardsAdapter = moshi.adapter<List<Award>>(awards)!!

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

    @TypeConverter
    fun fromMediaList(mediaList: MediaList?): String? {
        return when (mediaList) {
            null -> null
            else -> mediaListAdapter.toJson(mediaList)
        }
    }

    @TypeConverter
    fun toMediaList(mediaList:  String?): MediaList? {
        return mediaList?.let { mediaListAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromAwards(awards: List<Award>?): String? {
        return when (awards) {
            null -> null
            else -> awardsAdapter.toJson(awards)
        }
    }

    @TypeConverter
    fun toAward(awards:  String?): List<Award>? {
        return awards?.let { awardsAdapter.fromJson(it) }
    }

    companion object {
        lateinit var moshi : Moshi
    }
}