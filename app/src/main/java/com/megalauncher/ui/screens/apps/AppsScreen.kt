package com.megalauncher.ui.screens.apps

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.megalauncher.LauncherApp
import com.megalauncher.core.ui.theme.TextSecondary
import com.megalauncher.domain.model.AppInfo

@Composable
fun AppsScreen(
    vm: AppsViewModel = viewModel(
        factory = AppsViewModel.Factory(
            (LocalContext.current.applicationContext as LauncherApp).appRepository
        )
    )
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        SearchBar(query = state.query, onQueryChange = vm::onQueryChange)
        Spacer(Modifier.height(16.dp))

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Загрузка…", color = TextSecondary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.apps, key = { it.packageName }) { app ->
                    AppItem(app)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Поиск приложений", color = TextSecondary, fontSize = 15.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItem(app: AppInfo) {
    val context = LocalContext.current
    val bitmap = remember(app.packageName) {
        app.icon.toBitmap(128, 128).asImageBitmap()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                },
                onLongClick = {}
            )
            .padding(4.dp)
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = app.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = app.label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}
