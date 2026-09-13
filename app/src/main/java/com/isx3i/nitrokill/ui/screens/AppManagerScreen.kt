package com.isx3i.nitrokill.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.isx3i.nitrokill.data.AppInfo
import com.isx3i.nitrokill.data.AppRepository
import com.isx3i.nitrokill.data.formatDataSize
import com.isx3i.nitrokill.ui.theme.DarkSurface
import com.isx3i.nitrokill.ui.theme.NitroGreen
import com.isx3i.nitrokill.ui.theme.NitroPurple
import com.isx3i.nitrokill.ui.theme.NitroRed
import com.isx3i.nitrokill.ui.theme.TextSecondary

@Composable
fun AppManagerScreen() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }

    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        apps = repository.getLaunchableApps()
    }

    var memInfo by remember { mutableStateOf(repository.getMemoryInfo()) }
    val usedMemory = memInfo.totalMem - memInfo.availMem
    val usedRatio = if (memInfo.totalMem > 0) usedMemory.toFloat() / memInfo.totalMem else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("إدارة التطبيقات", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Memory, contentDescription = null, tint = NitroPurple)
                    Spacer(Modifier.width(8.dp))
                    Text("استخدام الذاكرة", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { usedRatio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = NitroPurple,
                    trackColor = Color(0xFF2A2A40)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${formatDataSize(usedMemory)} مستخدمة من ${formatDataSize(memInfo.totalMem)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                val before = repository.getMemoryInfo().availMem
                repository.closeAll(apps)
                val after = repository.getMemoryInfo()
                memInfo = after
                val freed = (after.availMem - before).coerceAtLeast(0)
                statusMessage = if (freed > 1_048_576L) {
                    "تم تحرير ${formatDataSize(freed)} من الذاكرة"
                } else {
                    "لا توجد عمليات خلفية قابلة للإغلاق حالياً — النظام يديرها تلقائياً"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NitroRed)
        ) {
            Icon(Icons.Filled.CleaningServices, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("إغلاق كل التطبيقات", fontWeight = FontWeight.Bold)
        }

        statusMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = NitroGreen, fontSize = 12.sp)
        }

        Spacer(Modifier.height(14.dp))
        Text("التطبيقات المثبتة (${apps.size})", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppRow(
                    app = app,
                    onClose = {
                        val before = repository.getMemoryInfo().availMem
                        repository.closeApp(app.packageName)
                        val after = repository.getMemoryInfo()
                        memInfo = after
                        val freed = (after.availMem - before).coerceAtLeast(0)
                        statusMessage = if (freed > 524_288L) {
                            "تم إغلاق ${app.label} وتحرير ${formatDataSize(freed)}"
                        } else {
                            "${app.label} لم يكن يشغّل عمليات خلفية قابلة للإغلاق"
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AppRow(app: AppInfo, onClose: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bitmap = remember(app.packageName) {
                runCatching { app.icon?.toBitmap(96, 96) }.getOrNull()
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
                Text(
                    if (app.isSystemApp) "تطبيق نظام" else "تطبيق مثبت",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "إغلاق", tint = NitroRed)
            }
        }
    }
}
