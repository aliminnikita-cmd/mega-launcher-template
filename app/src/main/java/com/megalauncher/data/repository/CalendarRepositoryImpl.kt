package com.megalauncher.data.repository

import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import com.megalauncher.domain.model.CalendarEvent
import com.megalauncher.domain.repository.CalendarRepository

class CalendarRepositoryImpl(private val context: Context) : CalendarRepository {

    private val projection = arrayOf(
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
        CalendarContract.Instances.ALL_DAY,
        CalendarContract.Instances.EVENT_LOCATION
    )

    override fun eventsForDay(dayStart: Long, dayEnd: Long): List<CalendarEvent> {
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(dayStart.toString())
            .appendPath(dayEnd.toString())
            .build()
        return query(uri)
    }

    override fun upcomingEvents(limit: Int): List<CalendarEvent> {
        val now = System.currentTimeMillis()
        val weekAhead = now + 7L * 24 * 60 * 60 * 1000
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString())
            .appendPath(weekAhead.toString())
            .build()
        return query(uri).sortedBy { it.startMillis }.take(limit)
    }

    private fun query(uri: Uri): List<CalendarEvent> {
        val result = mutableListOf<CalendarEvent>()
        try {
            context.contentResolver.query(
                uri, projection, null, null, "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { c ->
                val idIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val beginIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val allDayIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                val locIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)

                while (c.moveToNext()) {
                    result.add(
                        CalendarEvent(
                            id = c.getLong(idIdx),
                            title = c.getString(titleIdx) ?: "(без названия)",
                            startMillis = c.getLong(beginIdx),
                            endMillis = c.getLong(endIdx),
                            allDay = c.getInt(allDayIdx) == 1,
                            location = c.getString(locIdx)
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
        }
        return result
    }
}
