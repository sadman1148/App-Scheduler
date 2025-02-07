package com.meldcx.appscheduler.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utility {

    fun parseTime(time: Long): String {
        return SimpleDateFormat("hh:mm a_dd MMM yyyy", Locale.US).format(Date(time))
    }

}