package com.file.easyfilerecovery.ui.common

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.file.easyfilerecovery.data.FileInfo
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.data.StorageType
import com.file.easyfilerecovery.utils.CommonUtils.formatDateTime
import com.file.easyfilerecovery.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class GlobalViewModel : ViewModel() {

    companion object {
        val allRecoverableFiles = mutableListOf<FileInfo>()
    }

    val onScanCompletedLiveData = MutableLiveData(false)
    private var scanJob: Job? = null

    fun scanRecoverableFiles(context: Context, recoverType: RecoverType?) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {

            val minDurationMs = 2_000L
            val startAt = System.currentTimeMillis()

            val files = mutableListOf<FileInfo>()

            val protectPath = Environment.getExternalStorageDirectory().resolve("EasyFileRecoveryOwner").path

            val mimeTypes = FileUtils.getMimeTypesFor(recoverType)

            val mimeClause = if (recoverType == RecoverType.AUDIO || recoverType == RecoverType.VIDEO) {
                mimeTypes.joinToString(
                    prefix = "(",
                    separator = " OR ",
                    postfix = ")"
                ) { "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?" }
            } else if (mimeTypes.isNotEmpty()) {
                "${MediaStore.Files.FileColumns.MIME_TYPE} IN (${mimeTypes.joinToString { "?" }})"
            } else {
                "1"
            }

            val selection = buildString {
                append("${MediaStore.Files.FileColumns.SIZE} > 0")
                append(" AND ${MediaStore.Files.FileColumns.DATA} NOT LIKE ?")
                append(" AND $mimeClause")
            }

            val selectionArgs = arrayOf(protectPath) + mimeTypes.toTypedArray()


            val projection = arrayOf(
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                //    MediaStore.MediaColumns.DURATION
            )

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, sortOrder
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeIdx = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                // val durationIdx = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)

                while (cursor.moveToNext()) {
                    val path = cursor.getStringOrNull(dataIdx).orEmpty()
                    if (!File(path).exists()) continue

                    val storageType = when {
                        FileUtils.isHiddenFile(File(path)) -> StorageType.HIDDEN
                        (recoverType == RecoverType.PHOTO || recoverType == RecoverType.VIDEO) && path.contains("DCIM") -> StorageType.ALBUM
                        else -> StorageType.STORAGE
                    }

                    val modifyTime = (cursor.getLongOrNull(dateIdx) ?: 0L) * 1000L

                    files += FileInfo(
                        fileName = cursor.getStringOrNull(nameIdx).orEmpty(),
                        filePath = path,
                        fileSize = cursor.getLongOrNull(sizeIdx) ?: 0L,
                        lastModified = modifyTime,
                        duration = if (recoverType == RecoverType.VIDEO || recoverType == RecoverType.AUDIO) FileUtils.getMediaDuration(path) else 0L,
                        mimeType = cursor.getStringOrNull(mimeIdx).orEmpty(),
                        storageType = storageType,
                        title = formatDateTime(modifyTime, "MMMM yyyy")
                    )
                }

                val elapsed = System.currentTimeMillis() - startAt
                if (elapsed < minDurationMs) {
                    delay(minDurationMs - elapsed)
                }

                allRecoverableFiles.clear()
                allRecoverableFiles.addAll(files)
                onScanCompletedLiveData.postValue(true)
            }
        }
    }
}