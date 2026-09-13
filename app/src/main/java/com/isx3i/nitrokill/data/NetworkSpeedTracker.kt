package com.isx3i.nitrokill.data

import android.net.TrafficStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.util.Locale

data class NetworkSpeed(
    val downloadBps: Long = 0L,
    val uploadBps: Long = 0L,
    val totalDownloadedBytes: Long = 0L,
    val totalUploadedBytes: Long = 0L
)

/**
 * يقيس سرعة الإنترنت الحالية عبر مقارنة إجمالي البيانات المستلمة/المرسلة
 * للجهاز كل ثانية باستخدام TrafficStats (لا يحتاج أي إذن خاص).
 */
class NetworkSpeedTracker {

    fun speedFlow(intervalMs: Long = 1000L) = flow {
        val unsupported = TrafficStats.UNSUPPORTED.toLong()
        var lastRx = TrafficStats.getTotalRxBytes()
        var lastTx = TrafficStats.getTotalTxBytes()

        if (lastRx == unsupported || lastTx == unsupported) {
            emit(NetworkSpeed())
            return@flow
        }

        var sessionRx = 0L
        var sessionTx = 0L

        while (true) {
            delay(intervalMs)
            val currentRx = TrafficStats.getTotalRxBytes()
            val currentTx = TrafficStats.getTotalTxBytes()

            val downBytes = (currentRx - lastRx).coerceAtLeast(0)
            val upBytes = (currentTx - lastTx).coerceAtLeast(0)

            sessionRx += downBytes
            sessionTx += upBytes

            val downloadBps = (downBytes * 1000L) / intervalMs
            val uploadBps = (upBytes * 1000L) / intervalMs

            lastRx = currentRx
            lastTx = currentTx

            emit(
                NetworkSpeed(
                    downloadBps = downloadBps,
                    uploadBps = uploadBps,
                    totalDownloadedBytes = sessionRx,
                    totalUploadedBytes = sessionTx
                )
            )
        }
    }
}

fun formatSpeed(bytesPerSecond: Long): String {
    val bitsPerSecond = bytesPerSecond * 8
    return when {
        bitsPerSecond >= 1_000_000 -> String.format(Locale.US, "%.1f Mbps", bitsPerSecond / 1_000_000.0)
        bitsPerSecond >= 1_000 -> String.format(Locale.US, "%.0f Kbps", bitsPerSecond / 1_000.0)
        else -> "$bitsPerSecond bps"
    }
}

fun formatDataSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824L -> String.format(Locale.US, "%.2f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> String.format(Locale.US, "%.2f MB", bytes / 1_048_576.0)
        bytes >= 1024L -> String.format(Locale.US, "%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
