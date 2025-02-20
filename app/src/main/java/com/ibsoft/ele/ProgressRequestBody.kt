package com.ibsoft.ele

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

interface ProgressListener {
    fun onProgressUpdate(percentage: Int)
}

class ProgressRequestBody(
    private val delegate: RequestBody,
    private val listener: ProgressListener
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = try {
        delegate.contentLength()
    } catch (e: IOException) {
        -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten = 0L
        private val contentLength = contentLength()

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            val percentage = if (contentLength > 0) {
                ((bytesWritten.toDouble() / contentLength) * 100).toInt()
            } else 0
            listener.onProgressUpdate(percentage)
        }
    }
}
