package com.isx3i.nitrokill.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.isx3i.nitrokill.data.NetworkSpeed
import com.isx3i.nitrokill.data.NetworkSpeedTracker
import com.isx3i.nitrokill.data.formatDataSize
import com.isx3i.nitrokill.data.formatSpeed
import com.isx3i.nitrokill.service.SpeedMonitorService
import com.isx3i.nitrokill.ui.components.SpeedGauge
import com.isx3i.nitrokill.ui.theme.DarkSurface
import com.isx3i.nitrokill.ui.theme.NitroBlue
import com.isx3i.nitrokill.ui.theme.NitroGreen
import com.isx3i.nitrokill.ui.theme.NitroPurple
import com.isx3i.nitrokill.ui.theme.TextSecondary

@Composable
fun SpeedScreen() {
    val context = LocalContext.current
    val tracker = remember { NetworkSpeedTracker() }
    var speed by remember { mutableStateOf(NetworkSpeed()) }
    var monitoringEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tracker.speedFlow().collect { speed = it }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            monitoringEnabled = true
            context.startForegroundService(Intent(context, SpeedMonitorService::class.java))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "مراقبة سرعة الإنترنت",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SpeedGauge(
                label = "تنزيل",
                valueText = formatSpeed(speed.downloadBps),
                progress = speed.downloadBps / 5_000_000f
            )
            SpeedGauge(
                label = "رفع",
                valueText = formatSpeed(speed.uploadBps),
                progress = speed.uploadBps / 2_000_000f
            )
        }

        Spacer(Modifier.height(28.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataStatItem(
                    icon = Icons.Filled.ArrowDownward,
                    label = "بيانات التنزيل",
                    value = formatDataSize(speed.totalDownloadedBytes),
                    color = NitroBlue
                )
                DataStatItem(
                    icon = Icons.Filled.ArrowUpward,
                    label = "بيانات الرفع",
                    value = formatDataSize(speed.totalUploadedBytes),
                    color = NitroPurple
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("إشعار السرعة الدائم", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        "يعرض رقم السرعة كأيقونة بجانب شريط الحالة",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = monitoringEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED

                            if (needsPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                monitoringEnabled = true
                                context.startForegroundService(Intent(context, SpeedMonitorService::class.java))
                            }
                        } else {
                            monitoringEnabled = false
                            context.stopService(Intent(context, SpeedMonitorService::class.java))
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NitroGreen,
                        checkedTrackColor = NitroGreen.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DataStatItem(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color)
        Spacer(Modifier.height(6.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}
