package com.example.flourish.helper

import io.realm.kotlin.types.RealmList
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// DateTimeHandler class is a helper class to handle date and time related operations
class DateTimeHandler {
    companion object {
        // getCurrentDateTimeFormatted function returns the current date and time in the format "dd/MM/yyyy HH:mm"
        fun getCurrentDateTimeFormatted(): String {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            return currentDateTime.format(formatter)
        }

        // getDayMonthFromString function returns the day and month "dd/MM" from the given date string in the format "dd/MM/yyyy HH:mm"
        fun getDayMonthFromString(date: String): String {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM")
            val dateTime = LocalDateTime.parse(date, inputFormatter)

            return dateTime.format(outputFormatter)
        }

        // getDateFormatted function returns the date in the format "dd/MM/yyyy" from the given date string
        fun getDateFormatted(date: String): String {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val localDate = LocalDate.parse(date, formatter)
            return localDate.format(formatter)
        }

        // getCurrentDateFormatted function returns the current date in the format "dd/MM/yyyy"
        fun getCurrentDateFormatted(): String {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return currentDateTime.format(formatter)
        }

        // getDayOfWeekForWateringSchedule function returns the day of the week for the next watering schedule
        fun getDayOfWeekForWateringSchedule(daysOfWeek: RealmList<String>): String {
            val currentDayOfWeek = LocalDateTime.now().dayOfWeek.name

            // Check if the current day of the week is in the list of days for watering schedule
            return if (daysOfWeek.any { it.equals(currentDayOfWeek, ignoreCase = true) }) {
                "Today"
            } // Find the next day of the week in the list of days for watering schedule
            else {
                (1..7)
                    .asSequence()
                    .map { LocalDateTime.now().plusDays(it.toLong()).dayOfWeek.name }
                    .firstOrNull { nextDayOfWeek ->
                        daysOfWeek.any { it.equals(nextDayOfWeek, ignoreCase = true) }
                    }
                    ?.let { "Next $it" } ?: "Not scheduled"
            }
        }

        // getDateForFertilisingSchedule function returns the date for the next fertilising schedule
        fun getDateForFertilisingSchedule(date: String?): String {
            return if (getCurrentDateFormatted() == date) {
                "Today"
            } else {
                "Next $date"
            }
        }
    }
}