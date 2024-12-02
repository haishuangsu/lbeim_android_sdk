package info.hermiths.lbesdk.utils

object FileUtils {

    fun isImage(mime: String): Boolean {
        return mime.contains("image")
    }

    fun isGif(mime: String): Boolean {
        return mime.contains("gif")
    }

}