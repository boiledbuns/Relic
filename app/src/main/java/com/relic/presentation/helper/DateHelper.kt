package com.relic.presentation.helper

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// TODO create additional format options and allow user to select
const val POST_DATE_FORMAT = "hh:mm a'-' E MM/dd/yyyy"

object DateHelper {

    private val HOUR_IN_ML = "3.6e6".toDouble()
    private val DAY_IN_ML = HOUR_IN_ML * 24L
    private val YEAR_IN_ML = DAY_IN_ML * 365L

    private val formatter = SimpleDateFormat("MMM dd',' hh:mm a", Locale.ROOT)

    private val current = Date()

    // difference between now and specified date
    fun getDateDifferenceString(date : Date) : String{
        return getDateDifferenceString(date, Date())
    }

    fun getDateDifferenceString(firstDate : Date, secondDate : Date) : String {
        val millisecondDiff : Long = secondDate.time - firstDate.time
        Timber.d("%s%s", secondDate, firstDate)

        return when {
            millisecondDiff < HOUR_IN_ML -> "${TimeUnit.MINUTES.convert(millisecondDiff, TimeUnit.MILLISECONDS)} minutes"
            millisecondDiff < DAY_IN_ML -> "${TimeUnit.HOURS.convert(millisecondDiff, TimeUnit.MILLISECONDS)} hours"
            millisecondDiff < YEAR_IN_ML -> "${TimeUnit.DAYS.convert(millisecondDiff, TimeUnit.MILLISECONDS)} days"
            else -> "~${TimeUnit.DAYS.convert(millisecondDiff, TimeUnit.MILLISECONDS).div(365)} years"
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