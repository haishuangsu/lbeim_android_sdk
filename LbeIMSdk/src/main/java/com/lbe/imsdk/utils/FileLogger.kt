package com.lbe.imsdk.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger(private val context: Context) {
//    fun log(tag: String, message: String) {
//        val logFile = File(context.filesDir, "app_log.txt")
//        try {
//            FileOutputStream(logFile, true).use { fos ->
//                OutputStreamWriter(fos).use { writer ->
//                    val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
//                    writer.append("$timeStamp [$tag]: $message\n")
//                    writer.flush()
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("FileLogger", "Error writing log", e)
//        }
//    }

    private var fileUri: Uri? = null

    init {
        fileUri = getLogFileUri()
    }

    private fun getLogFileUri(): Uri? {
        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection =
            "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        val selectionArgs = arrayOf("lbe_log.txt", "Download/")

        resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                return Uri.withAppendedPath(collection, id.toString())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "lbe_log.txt")
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
            }

            return resolver.insert(collection, values)
        }
        return null
    }

    fun log(tag: String, message: String) {
        fileUri?.let { uri ->
            try {
                val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val logMessage = "$timeStamp [$tag]: $message\n\n\n"

                context.contentResolver.openOutputStream(uri, "wa")?.use { fos ->
                    OutputStreamWriter(fos).use { writer ->
                        writer.append(logMessage)
                        writer.flush()
                    }
                }
            } catch (e: Exception) {
                Log.e("FileLogger", "Error writing log", e)
            }
        }
    }
}
