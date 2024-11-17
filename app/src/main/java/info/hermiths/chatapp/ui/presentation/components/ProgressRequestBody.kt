package info.hermiths.chatapp.ui.presentation.components

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.internal.checkOffsetAndCount

import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import okio.source
import java.io.IOException


class ProgressRequestBody(private val delegate: RequestBody, private val listener: Listener) :
    RequestBody() {
    override fun contentType(): MediaType? {
        return delegate.contentType()
    }

    override fun contentLength(): Long {
        try {
            return delegate.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink: BufferedSink = countingSink.buffer()

        delegate.writeTo(bufferedSink)

        bufferedSink.flush()
    }

    internal inner class CountingSink(delegate: Sink?) : ForwardingSink(delegate!!) {
        private var bytesWritten: Long = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            listener.onRequestProgress(bytesWritten, contentLength())
        }
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        @JvmName("create")
        fun ByteArray.toRequestBody(
            contentType: MediaType? = null, offset: Int = 0, byteCount: Int = size
        ): RequestBody {
            checkOffsetAndCount(size.toLong(), offset.toLong(), byteCount.toLong())
            return object : RequestBody() {
                override fun contentType() = contentType

                override fun contentLength() = byteCount.toLong()

                override fun writeTo(sink: BufferedSink) {
                    this@toRequestBody.inputStream().use { source -> sink.writeAll(source.source()) }
                }
            }
        }
    }

    fun interface Listener {
        fun onRequestProgress(bytesWritten: Long, contentLength: Long)
    }
}