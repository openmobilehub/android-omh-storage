package com.openmobilehub.android.storage.core.utils

import java.util.Date

object DateUtils {
    fun getNewerDate(firstDate: Date, secondDate: Date): Date {
        return if (firstDate.after(secondDate)) {
            firstDate
        } else {
            secondDate
        }
    }
}
