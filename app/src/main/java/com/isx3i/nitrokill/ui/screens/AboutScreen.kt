package com.isx3i.nitrokill.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isx3i.nitrokill.ui.theme.DarkSurface
import com.isx3i.nitrokill.ui.theme.NitroBlue
import com.isx3i.nitrokill.ui.theme.NitroPurple
import com.isx3i.nitrokill.ui.theme.TextSecondary

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NitroPurple, NitroBlue))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text("NitroKill", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("الإصدار 1.0.0", color = TextSecondary, fontSize = 13.sp)

        Spacer(Modifier.height(12.dp))
        Text(
            "يجمع NitroKill بين مراقبة سرعة الإنترنت وإدارة إغلاق التطبيقات في تطبيق واحد بسيط وسريع.",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(28.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/iSx3i"))
                        context.startActivity(intent)
                    }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NitroBlue, NitroPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Telegram", tint = Color.White)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("المطوّر", color = TextSecondary, fontSize = 12.sp)
                    Text("صالح الخليفي", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("@iSx3i على تيليجرام", color = NitroBlue, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "تم التطوير بلغة Kotlin و Jetpack Compose",
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}
