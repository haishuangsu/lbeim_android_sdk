package info.hermiths.chatapp.utils

object FileUtils {

    fun isImage(mime: String): Boolean {
        return mime.contains("image")
    }

}