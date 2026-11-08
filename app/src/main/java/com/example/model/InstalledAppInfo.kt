package com.example.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val iconBitmap: ImageBitmap?,
    val isSystemApp: Boolean,
    val isHidden: Boolean,
    val versionName: String? = null
)

fun Drawable.toSafeImageBitmap(): ImageBitmap? {
    return try {
        if (this is BitmapDrawable && this.bitmap != null) {
            return this.bitmap.asImageBitmap()
        }
        val width = if (intrinsicWidth > 0) intrinsicWidth.coerceIn(48, 192) else 96
        val height = if (intrinsicHeight > 0) intrinsicHeight.coerceIn(48, 192) else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bitmap.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}
