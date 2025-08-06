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
import com.file.easyfilerecovery.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class GlobalViewModel : ViewModel() {


    val onScanCompletedLiveData = MutableLiveData<Boolean>()
    val allRecoverableFiles = mutableListOf<FileInfo>()

    fun scanRecoverableFiles(context: Context, recoverType: RecoverType?) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {

            val files = mutableListOf<FileInfo>()

            val protectPath = Environment.getExternalStorageDirectory().resolve("EasyFileRecoveryBackup").path

            val mimeTypes = FileUtils.getMimeTypesFor(recoverType)
            val selection = buildString {
                append("${MediaStore.Files.FileColumns.SIZE} > 0")
                append(" AND ${MediaStore.Files.FileColumns.DATA} NOT LIKE ?")
                if (mimeTypes.isNotEmpty()) {
                    append(" AND ${MediaStore.Files.FileColumns.MIME_TYPE} IN (")
                    append(mimeTypes.joinToString(",") { "?" })
                    append(")")
                }
            }
            val selectionArgs = mutableListOf("%$protectPath%").apply {
                addAll(mimeTypes)
            }.toTypedArray()

            val projection = arrayOf(
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
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

                while (cursor.moveToNext()) {
                    val path = cursor.getStringOrNull(dataIdx).orEmpty()
                    if (!File(path).exists()) continue

                    val storageType = when {
                        FileUtils.isHiddenFile(File(path)) -> StorageType.HIDDEN
                        (recoverType == RecoverType.PHOTO || recoverType == RecoverType.VIDEO) && path.contains("DCIM") -> StorageType.ALBUM
                        else -> StorageType.STORAGE
                    }

                    files += FileInfo(
                        fileName = cursor.getStringOrNull(nameIdx).orEmpty(),
                        filePath = path,
                        fileSize = cursor.getLongOrNull(sizeIdx) ?: 0L,
                        lastModified = (cursor.getLongOrNull(dateIdx) ?: 0L) * 1000L,
                        mimeType = cursor.getStringOrNull(mimeIdx).orEmpty(),
                        storageType = storageType
                    )
                }

                allRecoverableFiles.clear()
                allRecoverableFiles.addAll(files)
                onScanCompletedLiveData.postValue(true)
            }
        }
    }
}