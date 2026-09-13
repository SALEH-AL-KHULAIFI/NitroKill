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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * خدمة أمامية تعرض سرعة الإنترنت الحالية بشكل دائم في شريط الحالة.
 *
 * تم تكبير مساحة الرسم وحجم الرقم بشكل كبير،
 * مع استخدام خط عريض وواضح قدر الإمكان.
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

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            NOTIFICATION_ID,
            buildNotification(
                "0 Kbps",
                "0 Kbps"
            )
        )

        scope.launch {
            tracker.speedFlow().collect { speed ->

                val downText =
                    formatSpeed(speed.downloadBps)

                val upText =
                    formatSpeed(speed.uploadBps)

                val notification =
                    buildNotification(
                        downText,
                        upText
                    )

                val manager =
                    getSystemService(
                        NotificationManager::class.java
                    )

                manager?.notify(
                    NOTIFICATION_ID,
                    notification
                )
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
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

                description =
                    "يعرض سرعة الإنترنت الحالية بشكل مستمر في شريط الحالة"

                setShowBadge(false)
            }

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        downText: String,
        upText: String
    ): Notification {

        val openAppIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(
                    this,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_IMMUTABLE or
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

        val dynamicIcon =
            generateSpeedIcon(downText)

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("NitroKill")
            .setContentText(
                "⬇ $downText   ⬆ $upText"
            )
            .setSmallIcon(dynamicIcon)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .setCategory(
                NotificationCompat.CATEGORY_SERVICE
            )
            .build()
    }

    /**
     * ينشئ أيقونة السرعة.
     *
     * تم رفع حجم الصورة وحجم النص بدرجة كبيرة.
     */
    private fun generateSpeedIcon(
        text: String
    ): IconCompat {

        val displayText =
            shortenSpeedForStatusBar(text)

        /*
         * رفعنا مساحة الرسم من 192 إلى 512.
         *
         * ملاحظة:
         * Android قد يفرض حجمًا ثابتًا لأيقونة
         * شريط الحالة، لكن تكبير مساحة الرسم
         * وحجم النص يعطي أفضل نتيجة ممكنة
         * قبل الانتقال إلى طريقة عرض مختلفة.
         */
        val size = 512

        val bitmap =
            Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        val paint =
            Paint(
                Paint.ANTI_ALIAS_FLAG or
                    Paint.SUBPIXEL_TEXT_FLAG
            ).apply {

                color = Color.WHITE

                textAlign =
                    Paint.Align.CENTER

                typeface =
                    Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                    )

                /*
                 * تكبير الرقم بشكل كبير جدًا.
                 */
                textSize = when {

                    displayText.length <= 1 ->
                        360f

                    displayText.length == 2 ->
                        310f

                    displayText.length == 3 ->
                        260f

                    else ->
                        215f
                }

                /*
                 * تحسين سماكة الرقم.
                 */
                strokeWidth = 8f

                isSubpixelText = true
            }

        val fontMetrics =
            paint.fontMetrics

        val baseline =
            size / 2f -
                (
                    fontMetrics.ascent +
                        fontMetrics.descent
                    ) / 2f

        canvas.drawText(
            displayText,
            size / 2f,
            baseline,
            paint
        )

        return IconCompat.createWithBitmap(
            bitmap
        )
    }

    /**
     * يختصر قيمة السرعة حتى تبقى واضحة
     * داخل المساحة المحدودة لأيقونة شريط الحالة.
     *
     * أمثلة:
     *
     * 125 Kbps  -> 125
     * 1.4 Mbps  -> 1.4
     * 12.8 Mbps -> 12.8
     * 125.5 Mbps -> 125.
     */
    private fun shortenSpeedForStatusBar(
        text: String
    ): String {

        val number =
            text
                .trim()
                .substringBefore(" ")

        return when {

            number.length <= 4 ->
                number

            number.contains(".") ->
                number.take(4)

            else ->
                number.take(4)
        }
    }
}
