package com.file.easyfilerecovery.ui.base

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.file.easyfilerecovery.APP
import com.file.easyfilerecovery.databinding.ActivityAutoCloseBinding
import com.file.easyfilerecovery.utils.hasAllFilePermission
import com.file.easyfilerecovery.utils.launchActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoCloseActivity : BaseActivity<ActivityAutoCloseBinding>(ActivityAutoCloseBinding::inflate) {

    private var permissionCheckJob: Job? = null
    private var permissionHasRequested = false


    override fun initUI() {
        binding.root.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        when {
            permissionHasRequested || intent.action.isNullOrBlank() -> {
                cleanupAndFinish()
            }

            else -> {
                permissionHasRequested = true
                intent.action!!.let { _ ->
                    requestPermission()
                    startPermissionMonitor()
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun requestPermission() {
        APP.lifecycleManager.setNavigatingToSettings(true)
        runCatching {
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).also {
                it.data = "package:${packageName}".toUri()
                it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            })
        }.onFailure {
            runCatching {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).also {
                    it.data = "package:${packageName}".toUri()
                    it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                })
            }.onFailure {
                cleanupAndFinish()
            }
        }
    }


    private fun startPermissionMonitor() {
        cancelPermissionCheck()
        permissionCheckJob = lifecycleScope.launch {
            runCatching {
                while (!hasAllFilePermission()) {
                    delay(200)
                }
                if (!isFinishing) restartSelf()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private fun restartSelf() {
        launchActivity<AutoCloseActivity>(finish = true) {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    }

    private fun cancelPermissionCheck() {
        permissionCheckJob?.cancel()
        permissionCheckJob = null
    }

    private fun cleanupAndFinish() {
        cancelPermissionCheck()
        finish()
    }

    override fun onDestroy() {
        APP.lifecycleManager.setNavigatingToSettings(false)
        super.onDestroy()
        cancelPermissionCheck()
    }


}