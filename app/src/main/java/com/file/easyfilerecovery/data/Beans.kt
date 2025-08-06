package com.file.easyfilerecovery.data

import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.file.easyfilerecovery.R

@Keep
enum class RecoverType(private val resId: Int) {
    PHOTO(R.string.str_picture_recover),
    VIDEO(R.string.str_video_recover),
    AUDIO(R.string.str_voice_recover),
    DOC(R.string.str_doc_recover);
    fun getRecoverName(activity: AppCompatActivity): String = activity.getString(resId)
}