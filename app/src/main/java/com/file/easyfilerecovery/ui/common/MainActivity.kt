package com.file.easyfilerecovery.ui.common

import com.blankj.utilcode.util.ToastUtils
import com.file.easyfilerecovery.databinding.ActivityMainBinding
import com.file.easyfilerecovery.ui.base.BaseStorageActivity

class MainActivity : BaseStorageActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun initUI() {
        binding.btn.setOnClickListener {
            checkPermission {
                ToastUtils.showLong("获取到权限")
            }
        }
    }
}