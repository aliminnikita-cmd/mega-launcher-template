package com.megalauncher.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.megalauncher.LauncherApp
import com.megalauncher.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    vm: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            (LocalContext.current.applicationContext as LauncherApp).calendarRepository,
            (LocalContext.current.applicationContext as LauncherApp).weatherRepository,
            (LocalContext.current.applicationContext as LauncherApp).settingsRepository
        )
    )
) {
    val state by vm.state.collectAsStateWithLifecycle()

    var now by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Calendar.getInstance().time
            delay(1000L)
        }
    }
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEEE, d MMMM", Locale("ru")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    timeFmt.format(now),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    dateFmt.format(now).replaceFirstChar { it.uppercase() },
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Настройки", tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(8.dp))

        val weather = state.weather
        val weatherText = when {
            state.weatherLoading -> "…"
            weather != null -> "${weather.temperature.toInt()}° ${weather.description}"
            else -> "—"
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { vm.refreshWeather() },
                title = "Погода",
                value = weatherText,
                subtitle = weather?.city
            )
            HomeCard(
                modifier = Modifier.weight(1f),
                title = "События",
                value = "${state.events.size} ближайших",
                subtitle = state.events.firstOrNull()?.title
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun HomeCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
        }
    }
}
