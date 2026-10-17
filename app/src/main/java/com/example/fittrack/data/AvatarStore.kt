package com.example.fittrack.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File

/** Saves a picked photo as a small square JPEG inside the app's private storage (no storage permission needed). */
object AvatarStore {
    private const val SIZE = 512

    fun save(ctx: Context, uid: String, uri: Uri): String? = runCatching {
        val resolver = ctx.contentResolver

        // 1) read dimensions, then decode a down-sampled copy
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 1536 || bounds.outHeight / sample > 1536) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        // 2) respect the camera's rotation
        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val upright = if (degrees == 0f) decoded
        else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(degrees) }, true)

        // 3) centre-crop to a square and scale down
        val side = minOf(upright.width, upright.height)
        val square = Bitmap.createBitmap(upright, (upright.width - side) / 2, (upright.height - side) / 2, side, side)
        val scaled = Bitmap.createScaledBitmap(square, SIZE, SIZE, true)

        val file = File(ctx.filesDir, "avatar_${uid}_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        file.absolutePath
    }.getOrNull()

    fun delete(path: String) { if (path.isNotBlank()) runCatching { File(path).delete() } }
}
