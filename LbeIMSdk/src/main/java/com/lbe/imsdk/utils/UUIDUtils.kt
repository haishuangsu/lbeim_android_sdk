package com.lbe.imsdk.utils

import java.util.UUID

object UUIDUtils {
    fun uuidGen(): String {
        return UUID.randomUUID().toString()
    }
}