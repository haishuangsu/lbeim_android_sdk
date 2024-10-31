package info.hermiths.chatapp.utils

import java.util.UUID

object UUIDUtils {
    fun uuidGen(): String {
        return UUID.randomUUID().toString()
    }
}