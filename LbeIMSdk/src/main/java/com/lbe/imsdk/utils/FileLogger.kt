package com.lbe.imsdk.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger(context: Context) {
    private val logFile = File(context.filesDir, "app_log.txt")

    fun log(tag: String, message: String) {
        try {
            FileWriter(logFile, true).use { writer ->
                val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                writer.append("$timeStamp [$tag]: $message\n\n\n")
            }
        } catch (e: IOException) {
            Log.e("FileLogger", "Error writing log", e)
        }
    }
}
