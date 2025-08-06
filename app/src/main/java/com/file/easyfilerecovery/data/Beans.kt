package com.file.easyfilerecovery.data

import android.app.Activity
import android.os.Parcelable
import androidx.annotation.Keep
import com.file.easyfilerecovery.R
import kotlinx.parcelize.Parcelize

@Keep
enum class RecoverType(private val resId: Int) {
    PHOTO(R.string.str_picture_recover),
    VIDEO(R.string.str_video_recover),
    AUDIO(R.string.str_voice_recover),
    DOC(R.string.str_doc_recover);

    fun getRecoverName(activity: Activity): String = activity.getString(resId)
}

@Keep
enum class StorageType {
    HIDDEN, STORAGE, ALBUM
}

@Keep
@Parcelize
data class FileInfo(
    val fileName: String = "",
    val filePath: String = "",
    val fileSize: Long = 0L,
    val lastModified: Long = 0L,
    val mimeType: String = "",
    val title: String = "",
    val storageType: StorageType = StorageType.STORAGE,
    val isTitle: Boolean = false,
    var checked: Boolean = false
) : Parcelable