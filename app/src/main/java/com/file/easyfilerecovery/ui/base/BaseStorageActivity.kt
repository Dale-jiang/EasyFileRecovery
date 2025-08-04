package com.file.easyfilerecovery.ui.base

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ToastUtils
import com.file.easyfilerecovery.APP
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.utils.hasAllFilePermission
import com.file.easyfilerecovery.utils.isFirstRequestStorage

abstract class BaseStorageActivity<VB : ViewBinding>(inflate: (LayoutInflater) -> VB) : BaseActivity<VB>(inflate) {

    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var onGrantedCallback: (() -> Unit)? = null

    private val extraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        APP.lifecycleManager.setNavigatingToSettings(false)
        if (hasAllFilePermission()) {
            onGrantedCallback?.invoke()
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (hasAllFilePermission()) {
            onGrantedCallback?.invoke()
        }
    }

    private val manageAllFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (hasAllFilePermission()) {
                onGrantedCallback?.invoke()
            }
        }


    fun checkPermission(onGranted: () -> Unit) {
        onGrantedCallback = onGranted

        if (hasAllFilePermission()) {
            onGranted()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (isFirstRequestStorage) {
                isFirstRequestStorage = false
                permissionLauncher.launch(permissions)
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionLauncher.launch(permissions)
            } else {
                runCatching {
                    APP.lifecycleManager.setNavigatingToSettings(true)
                    extraLauncher.launch(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:$packageName".toUri()
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        }
                    )
                }.onFailure {
                    ToastUtils.showLong(getString(R.string.str_unknown_error))
                }
            }
        } else {
            Intent(this, AutoCloseActivity::class.java).also {
                it.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                manageAllFilesLauncher.launch(it)
            }
        }
    }


}