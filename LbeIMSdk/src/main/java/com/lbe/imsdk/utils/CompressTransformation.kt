package com.lbe.imsdk.utils

import android.graphics.BitmapFactory

//class CompressTransformation(private val quality: Int) : BitmapTransformation() {
//    override suspend fun transform(input: Bitmap, size: coil.size.Size): Bitmap {
//        val outputStream = java.io.ByteArrayOutputStream()
//        input.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
//        val byteArray = outputStream.toByteArray()
//        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//    }
//}