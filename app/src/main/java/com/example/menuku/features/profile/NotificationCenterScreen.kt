package com.example.menuku.features.profile

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.example.menuku.core.components.rememberNotificationPermissionRequest
import com.example.menuku.data.models.NotificationCategory
import com.example.menuku.data.repository.MenuKuRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(repository: MenuKuRepository, onBackClick: () -> Unit, onNavigate: (String) -> Unit) {
    val notifications by repository.notifications.collectAsState()
    var filter by remember { mutableStateOf(NotificationCategory.ALL) }
    val requestPermission = rememberNotificationPermissionRequest()
    val context = LocalContext.current
    LaunchedEffect(Unit) { repository.refreshReminders() }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Pengingat Dapur") }, navigationIcon = {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
        }, actions = { TextButton(onClick = repository::markAllNotificationsRead) { Text("Tandai dibaca") } })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Pengingat berasal dari stok dan jadwal akun Anda. Waktu pengiriman dapat bergeser mengikuti penghematan baterai Android.")
                Button(onClick = requestPermission) { Text("Izinkan notifikasi") }
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                }) { Text("Buka pengaturan notifikasi") }
                if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) Text("Notifikasi perangkat belum diizinkan.")
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(NotificationCategory.ALL, NotificationCategory.PANTRY, NotificationCategory.COOKING)) { category ->
                        FilterChip(selected = filter == category, onClick = { filter = category }, label = { Text(category.label) })
                    }
                }
            }
            val visible = notifications.filter { filter == NotificationCategory.ALL || it.category == filter }
            if (visible.isEmpty()) item { Text("Belum ada pengingat. Tambahkan stok atau jadwal makan dan aktifkan pengingat di Profil.") }
            items(visible, key = { it.id }) { notification ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(notification.title, style = MaterialTheme.typography.titleSmall)
                        Text(notification.message)
                        Text("Dijadwalkan: ${notification.timeLabel}", style = MaterialTheme.typography.labelSmall)
                        if (notification.isRead) Text("Sudah dibaca", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
