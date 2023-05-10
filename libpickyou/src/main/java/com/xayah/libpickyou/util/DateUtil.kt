package com.xayah.libpickyou.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class DateUtil {
    companion object {
        fun timestampToDateString(timestamp: Long): String {
            return SimpleDateFormat(
                "yy-MM-dd HH:mm:ss",
                Locale.getDefault(Locale.Category.FORMAT)
            ).format(Date(timestamp))
        }
    }
}
