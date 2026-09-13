package com.isx3i.nitrokill.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.isx3i.nitrokill.MainActivity
import com.isx3i.nitrokill.data.NetworkSpeedTracker
import com.isx3i.nitrokill.data.formatSpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * خدمة أمامية (Foreground Service) تعرض سرعة الإنترنت الحالية كإشعار دائم
 * منخفض الأولوية، مع أيقونة مُولّدة ديناميكياً تعرض رقم سرعة التنزيل الحالي
 * في شريط الحالة - تماماً كما تفعل تطبيقات "Internet Speed Meter".
 */
class SpeedMonitorService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)
    private val tracker = NetworkSpeedTracker()

    companion object {
        const val CHANNEL_ID = "nitrokill_speed_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("0 Kbps", "0 Kbps"))

        scope.launch {
            tracker.speedFlow().collect { speed ->
                val downText = formatSpeed(speed.downloadBps)
                val upText = formatSpeed(speed.uploadBps)
                val notification = buildNotification(downText, upText)
                val manager = getSystemService(NotificationManager::class.java)
                manager?.notify(NOTIFICATION_ID, notification)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "مراقبة سرعة الإنترنت",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "يعرض سرعة الإنترنت الحالية بشكل مستمر في شريط الحالة"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(downText: String, upText: String): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val dynamicIcon = generateSpeedIcon(downText)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NitroKill")
            .setContentText("⬇ $downText   ⬆ $upText")
            .setSmallIcon(dynamicIcon)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /** يرسم رقم سرعة التنزيل الحالي على شكل أيقونة نصية لعرضها في شريط الحالة. */
    private fun generateSpeedIcon(text: String): IconCompat {
        val displayText = text.substringBefore(" ").take(3)
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = if (displayText.length > 2) 30f else 40f
        }
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(displayText, canvas.width / 2f, yPos, paint)
        return IconCompat.createWithBitmap(bitmap)
    }
}
