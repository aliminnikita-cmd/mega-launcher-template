package com.megalauncher.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.megalauncher.LauncherApp
import com.megalauncher.core.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    vm: CalendarViewModel = viewModel(
        factory = CalendarViewModel.Factory(
            (LocalContext.current.applicationContext as LauncherApp).calendarRepository
        )
    )
) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.READ_CALENDAR)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Верхняя панель: месяц + иконки
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        SimpleDateFormat("LLLL", Locale("ru"))
                            .format(Date(state.selectedDate))
                            .replaceFirstChar { it.uppercase() },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO: уведомления */ }) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = TextSecondary)
            }
            IconButton(onClick = { /* TODO: поиск */ }) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Сетка календаря (используем WeekStrip как компактную версию, но с месяцами)
        WeekStrip(selectedDate = state.selectedDate, onSelect = vm::selectDate)

        Spacer(Modifier.height(16.dp))

        // Секция "Сегодня"
        val dateFmt = remember { SimpleDateFormat("d", Locale.getDefault()) }
        val dayFmt = remember { SimpleDateFormat("EEEE", Locale("ru")) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Сегодня", fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    dateFmt.format(Date(state.selectedDate)),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    dayFmt.format(Date(state.selectedDate)).replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // События
        if (!hasPermission) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Нужно разрешение на чтение календаря",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else if (state.events.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Событий нет", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.events, key = { it.id.toString() + it.startMillis }) { e ->
                    EventRow(e.title, e.startMillis, e.endMillis, e.allDay)
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(selectedDate: Long, onSelect: (Long) -> Unit) {
    val days = remember {
        val base = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        // Показываем 7 дней начиная с текущей недели (пн-вс)
        val dayOfWeek = base.get(java.util.Calendar.DAY_OF_WEEK)
        base.add(java.util.Calendar.DAY_OF_MONTH, -(dayOfWeek - 2))
        List(7) { offset ->
            val c = base.clone() as java.util.Calendar
            c.add(java.util.Calendar.DAY_OF_MONTH, offset)
            c.timeInMillis
        }
    }
    val dayFmt = remember { SimpleDateFormat("EEE", Locale("ru")) }
    val numFmt = remember { SimpleDateFormat("d", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { millis ->
            val isSelected = isSameDay(millis, selectedDate)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text(
                    dayFmt.format(Date(millis)).lowercase().take(2),
                    fontSize = 10.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    numFmt.format(Date(millis)),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val ca = java.util.Calendar.getInstance().apply { timeInMillis = a }
    val cb = java.util.Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(java.util.Calendar.YEAR) == cb.get(java.util.Calendar.YEAR) &&
            ca.get(java.util.Calendar.DAY_OF_YEAR) == cb.get(java.util.Calendar.DAY_OF_YEAR)
}

@Composable
private fun EventRow(title: String, start: Long, end: Long, allDay: Boolean) {
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(
                if (allDay) "Весь день" else "${fmt.format(Date(start))} – ${fmt.format(Date(end))}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
