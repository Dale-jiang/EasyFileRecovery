package com.file.easyfilerecovery.utils

import android.media.MediaMetadataRetriever
import com.file.easyfilerecovery.data.RecoverType
import java.io.File

object FileUtils {

    val imageMimeTypes = listOf(
        "image/png", "image/jpeg", "image/gif",
        "image/heif", "image/bmp", "image/webp"
    )
    private val videoMimeTypes = listOf(
        "video/mp4",
        "video/mpeg",
        "video/avi",
        "video/x-msvideo",
        "video/quicktime",
        "video/webm",
        "video/x-matroska",
        "video/3gpp",
        "video/3gpp2",
        "video/ogg"
    )
    private val audioMimeTypes = listOf(
        "audio/mpeg",
        "audio/mp3",
        "audio/wav",
        "audio/x-wav",
        "audio/flac",
        "audio/x-flac",
        "audio/ogg",
        "audio/vorbis",
        "audio/aac",
        "audio/x-aac",
        "audio/amr",
        "audio/3gpp",
        "audio/3gpp2"
    )
    private val documentMimeTypes = listOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.ms-xpsdocument",
        "text/plain",
        "text/csv",
        "application/rtf",
        "application/vnd.oasis.opendocument.text",
        "application/vnd.oasis.opendocument.spreadsheet",
        "application/vnd.oasis.opendocument.presentation",
        "application/epub+zip",
        "application/zip",
        "application/vnd.rar",
        "application/x-7z-compressed"
    )


    fun getMimeTypesFor(recoverType: RecoverType?): List<String> = when (recoverType) {
        RecoverType.PHOTO -> imageMimeTypes.toList()
        RecoverType.VIDEO -> videoMimeTypes.toList()
        RecoverType.AUDIO -> audioMimeTypes.toList()
        RecoverType.DOC -> documentMimeTypes.toList()
        else -> emptyList()
    }


    fun isHiddenFile(file: File): Boolean = generateSequence(file) { it.parentFile }.any { it.isHidden || it.name.startsWith(".") }

    fun getMediaDuration(path: String): Long {
        val retriever = MediaMetadataRetriever()
        val duration = runCatching {
            retriever.setDataSource(path)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            return@runCatching durationStr?.toLongOrNull() ?: 0L
        }.getOrNull() ?: 0L
        runCatching { retriever.release() }
        return duration
    }

}