package com.example.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.example.data.HiddenAppDao
import com.example.data.HiddenAppEntity
import com.example.model.InstalledAppInfo
import com.example.model.toSafeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppsRepository(
    private val context: Context,
    private val hiddenAppDao: HiddenAppDao
) {
    val hiddenAppsFlow: Flow<List<HiddenAppEntity>> = hiddenAppDao.getAllHiddenApps()
    val hiddenPackageNamesFlow: Flow<List<String>> = hiddenAppDao.getAllHiddenPackageNames()

    suspend fun getInstalledLaunchableApps(hiddenPackages: Set<String>): List<InstalledAppInfo> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val currentPkg = context.packageName

            val list = mutableListOf<InstalledAppInfo>()
            val processedPackages = mutableSetOf<String>()

            for (ri in resolveInfos) {
                val pkgName = ri.activityInfo.packageName
                if (pkgName == currentPkg || processedPackages.contains(pkgName)) {
                    continue
                }
                processedPackages.add(pkgName)

                try {
                    val appInfo = pm.getApplicationInfo(pkgName, 0)
                    val label = pm.getApplicationLabel(appInfo).toString()
                    val iconDrawable = pm.getApplicationIcon(appInfo)
                    val iconBitmap = iconDrawable.toSafeImageBitmap()
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isHidden = hiddenPackages.contains(pkgName)

                    val packageInfo = try {
                        pm.getPackageInfo(pkgName, 0)
                    } catch (_: Exception) {
                        null
                    }

                    list.add(
                        InstalledAppInfo(
                            packageName = pkgName,
                            appName = label,
                            iconBitmap = iconBitmap,
                            isSystemApp = isSystem,
                            isHidden = isHidden,
                            versionName = packageInfo?.versionName
                        )
                    )
                } catch (_: Exception) {
                    // skip if error reading package info
                }
            }

            list.sortedBy { it.appName.lowercase() }
        }

    suspend fun hideApp(packageName: String, appName: String, isSystemApp: Boolean) {
        withContext(Dispatchers.IO) {
            hiddenAppDao.insertHiddenApp(
                HiddenAppEntity(
                    packageName = packageName,
                    appName = appName,
                    isSystemApp = isSystemApp
                )
            )
        }
    }

    suspend fun unhideApp(packageName: String) {
        withContext(Dispatchers.IO) {
            hiddenAppDao.deleteByPackageName(packageName)
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun openAppDetailsSettings(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // fallback
        }
    }

    fun openHomeSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // ignore
            }
        }
    }
}
