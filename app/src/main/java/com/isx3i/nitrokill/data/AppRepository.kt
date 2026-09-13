package com.isx3i.nitrokill.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 * ملاحظة تقنية مهمة:
 * منذ إصدارات أندرويد الحديثة (5.0 فما فوق)، لا يمكن لأي تطبيق عادي (بدون صلاحيات
 * جذر/نظام) أن "يقتل" تطبيقاً آخر بشكل كامل وفوري لأسباب أمنية. الدالة
 * killBackgroundProcesses() تطلب من النظام تحرير العمليات الخلفية المخبأة (cached)
 * الخاصة بالتطبيق فقط، وهذا هو نفس الأسلوب الذي تعمل به تطبيقات "منظف الذاكرة"
 * الموجودة فعلياً على متجر Google Play اليوم (مثل KillApp وغيرها).
 */
class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    fun getLaunchableApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = packageManager.queryIntentActivities(mainIntent, 0)
        val selfPackage = context.packageName

        return resolved
            .mapNotNull { it.activityInfo?.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != selfPackage }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    label = appInfo.loadLabel(packageManager).toString(),
                    icon = runCatching { appInfo.loadIcon(packageManager) }.getOrNull(),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun closeApp(packageName: String) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(packageName)
    }

    fun closeAll(apps: List<AppInfo>) {
        apps.forEach { closeApp(it.packageName) }
    }

    fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return info
    }
}
