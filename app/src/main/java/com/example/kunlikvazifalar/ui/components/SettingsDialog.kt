package com.example.kunlikvazifalar.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.kunlikvazifalar.BuildConfig
import com.example.kunlikvazifalar.data.preferences.ThemeMode
import com.example.kunlikvazifalar.data.preferences.UserPreferences
import com.example.kunlikvazifalar.notification.NotificationHelper

@Composable
fun SettingsDialog(
    currentUsername: String,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
    onSaveUsername: (String) -> Unit,
    repeatReminders: Boolean,
    onRepeatsChange: (Boolean) -> Unit,
    animations: Boolean,
    onAnimationsChange: (Boolean) -> Unit,
    haptics: Boolean,
    onHapticsChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferences = remember { UserPreferences(context) }
    var usernameText by rememberSaveable { mutableStateOf(currentUsername) }
    var hasError by rememberSaveable { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(NotificationHelper.areNotificationsEnabled(context)) }
    var exactAlarmsEnabled by remember { mutableStateOf(NotificationHelper.canScheduleExactAlarms(context)) }
    var permissionRequested by remember { mutableStateOf(preferences.isNotificationPermissionRequested()) }
    var showNotificationRationale by remember {
        mutableStateOf(context.findActivity()?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
        } == true)
    }

    fun refreshPermissions() {
        notificationsEnabled = NotificationHelper.areNotificationsEnabled(context)
        exactAlarmsEnabled = NotificationHelper.canScheduleExactAlarms(context)
        showNotificationRationale = context.findActivity()?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
        } == true
    }

    fun openSettings(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Tizim sozlamalarini ochib bo‘lmadi. Qurilma sozlamalaridan Kenzo App’ni tanlang.", Toast.LENGTH_LONG).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // MainActivity.onResume restores future reminders after the system prompt returns.
        refreshPermissions()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshPermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AlertDialog(
        modifier = Modifier.imePadding(),
        properties = DialogProperties(decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        title = { Text("Sozlamalar", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = usernameText,
                    onValueChange = {
                        usernameText = it
                        if (it.isNotBlank()) hasError = false
                    },
                    label = { Text("Ismingiz") },
                    isError = hasError,
                    supportingText = if (hasError) {
                        { Text("Ism bo‘sh bo‘lishi mumkin emas", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Mavzu", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(ThemeMode.SYSTEM to "Tizim", ThemeMode.LIGHT to "Yorug‘", ThemeMode.DARK to "Qorong‘i").forEach { (mode, label) ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = {
                                if (themeMode != mode) {
                                    onThemeModeChange(mode)
                                    if (Build.VERSION.SDK_INT < 31) context.findActivity()?.recreate()
                                }
                            },
                            label = { Text(label, maxLines = 1) }
                        )
                    }
                }
                Text("Mavzu tanlovi darhol saqlanadi.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                PreferenceSwitch("Takroriy eslatmalar", repeatReminders, onRepeatsChange)
                Text("Bajarilmagan vaqtli vazifa uchun +10 va +30 daqiqada, ko‘pi bilan ikki marta.", style = MaterialTheme.typography.bodySmall)
                PreferenceSwitch("Animatsiyalar", animations, onAnimationsChange)
                PreferenceSwitch("Vibratsiya", haptics, onHapticsChange)
                Text("Vibratsiya vazifani bajarishda yengil teginish javobini boshqaradi. Bildirishnoma tebranishi Android kanal sozlamalariga bog‘liq.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                Text("Eslatma ruxsatlari", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (notificationsEnabled) "Bildirishnomalar: yoqilgan" else "Bildirishnomalar: o‘chiq. Eslatma ko‘rinmaydi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (notificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
                if (!notificationsEnabled) {
                    val runtimeDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    val canRequest = runtimeDenied && (!permissionRequested || showNotificationRationale)
                    OutlinedButton(
                        onClick = {
                            if (canRequest) {
                                preferences.setNotificationPermissionRequested(true)
                                permissionRequested = true
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                openSettings(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    } else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (canRequest) "Bildirishnoma ruxsatini so‘rash" else "Bildirishnoma sozlamalari") }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (exactAlarmsEnabled) "Aniq signal: yoqilgan" else "Aniq signal: o‘chiq. Eslatma kechikishi mumkin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!exactAlarmsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    OutlinedButton(
                        onClick = { openSettings(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Aniq signalga ruxsat berish") }
                }
                Text(
                    "Vazifalar ruxsatlarsiz ham saqlanadi. Ruxsat berilgach, kelajakdagi eslatmalar qayta belgilanadi.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(16.dp))
                Text("Kenzo App · ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ilova oflayn ishlaydi. Ma’lumotlaringiz qurilmangizda saqlanadi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmed = usernameText.trim()
                if (trimmed.isBlank()) hasError = true else {
                    onSaveUsername(trimmed)
                    onDismiss()
                }
            }) { Text("Ismni saqlash") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Yopish") } }
    )
}

@Composable
private fun PreferenceSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange,
            modifier = Modifier.semantics { contentDescription = label })
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
