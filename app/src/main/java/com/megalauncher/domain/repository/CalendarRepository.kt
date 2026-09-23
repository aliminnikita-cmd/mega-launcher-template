package com.megalauncher.domain.repository

import com.megalauncher.domain.model.CalendarEvent

interface CalendarRepository {
    fun eventsForDay(dayStartMillis: Long, dayEndMillis: Long): List<CalendarEvent>
    fun upcomingEvents(limit: Int): List<CalendarEvent>
}
