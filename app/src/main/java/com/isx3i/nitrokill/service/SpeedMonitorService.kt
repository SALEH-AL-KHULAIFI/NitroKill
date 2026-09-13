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

    private fun generateSpeedIcon(text: String): IconCompat {
        val digits = text
            .substringBefore(" ")
            .substringBefore(".")
            .filter { it.isDigit() }
            .ifEmpty { "0" }
            .take(2)

        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = if (digits.length > 1) size * 0.62f else size * 0.8f
        }
        val strokePaint = Paint(fillPaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.06f
        }

        val yPos = (canvas.height / 2f) - ((fillPaint.descent() + fillPaint.ascent()) / 2f)
        canvas.drawText(digits, canvas.width / 2f, yPos, strokePaint)
        canvas.drawText(digits, canvas.width / 2f, yPos, fillPaint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
