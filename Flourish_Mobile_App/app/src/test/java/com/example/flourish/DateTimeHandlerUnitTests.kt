package com.example.flourish
import com.example.flourish.helper.DateTimeHandler
import io.realm.kotlin.ext.toRealmList
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DateTimeHandlerUnitTests {
    @Test
    fun getCurrentDateTimeFormatted_returnsCurrentDateTimeInCorrectFormat() {
        val expected = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        val actual = DateTimeHandler.getCurrentDateTimeFormatted()
        assertEquals(expected, actual)
    }

    @Test
    fun getDayMonthFromString_returnsDayAndMonthFromDateString() {
        val date = "01/01/2022 12:00"
        val expected = "01/01"
        val actual = DateTimeHandler.getDayMonthFromString(date)
        assertEquals(expected, actual)
    }

    @Test
    fun getCurrentDateFormatted_returnsCurrentDateInCorrectFormat() {
        val expected = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val actual = DateTimeHandler.getCurrentDateFormatted()
        assertEquals(expected, actual)
    }

    @Test
    fun getDayOfWeekForWateringSchedule_returnsTodayWhenCurrentDayIsInSchedule() {
        val daysOfWeek = mutableListOf("MONDAY", "TUESDAY", "WEDNESDAY").toRealmList()
        val expected = "Today"
        val actual = DateTimeHandler.getDayOfWeekForWateringSchedule(daysOfWeek)
        assertEquals(expected, actual)
    }

    @Test
    fun getDateForFertilisingSchedule_returnsTodayWhenCurrentDateIsInSchedule() {
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val expected = "Today"
        val actual = DateTimeHandler.getDateForFertilisingSchedule(date)
        assertEquals(expected, actual)
    }

    @Test
    fun getDateForFertilisingSchedule_returnsNextDateWhenCurrentDateIsNotInSchedule() {
        val date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val expected = "Next $date"
        val actual = DateTimeHandler.getDateForFertilisingSchedule(date)
        assertEquals(expected, actual)
    }

}