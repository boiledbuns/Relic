package com.relic.presentation.helper

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateHelper {

    private val HOUR_IN_ML = "3.6e6".toDouble()
    private val DAY_IN_ML = HOUR_IN_ML * 24L
    private val YEAR_IN_ML = DAY_IN_ML * 365L

    // initialize the date formatter and date for "now"
    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.CANADA)
    private val current = Date()

    fun getDateDifferenceString(firstDate : Date, secondDate : Date) : String {
        val millisecondDiff : Long = secondDate.time - firstDate.time

        return when {
            millisecondDiff < HOUR_IN_ML -> "${TimeUnit.MINUTES.convert(millisecondDiff, TimeUnit.MILLISECONDS)} minutes ago"
            millisecondDiff < DAY_IN_ML -> "${TimeUnit.HOURS.convert(millisecondDiff, TimeUnit.MILLISECONDS)} hours ago"
            millisecondDiff < YEAR_IN_ML -> "${TimeUnit.DAYS.convert(millisecondDiff, TimeUnit.MILLISECONDS)} days ago"
            else -> "~${TimeUnit.DAYS.convert(millisecondDiff, TimeUnit.MILLISECONDS).div(365)} years ago"
        }
    }

    fun formatDate(date : Date) : String {
        return if (current.year != date.year) {
            date.year.toString() + " " + formatter.format(date)
        } else {
            formatter.format(date)
        }
    }

}