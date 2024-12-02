package info.hermiths.lbesdk.utils

import java.util.UUID

object UUIDUtils {
    fun uuidGen(): String {
        return UUID.randomUUID().toString()
    }
}