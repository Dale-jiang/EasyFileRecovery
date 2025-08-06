package com.file.easyfilerecovery.ui.common

import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ActivityMainBinding
import com.file.easyfilerecovery.ui.base.BaseStorageActivity
import com.file.easyfilerecovery.ui.recover.FileRecoveryListActivity.Companion.RECOVER_TYPE_KEY
import com.file.easyfilerecovery.ui.settings.SettingsActivity
import com.file.easyfilerecovery.utils.launchActivity

class MainActivity : BaseStorageActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun initUI() {

    }

    override fun initListeners() {

        binding.apply {

            btnSet.setOnClickListener {
                launchActivity<SettingsActivity>()
            }

            btnPic.setOnClickListener {
                checkPermission {
                    launchActivity<CommonScanActivity> {
                        putExtra(RECOVER_TYPE_KEY, RecoverType.PHOTO)
                    }
                }
            }

            btnVoice.setOnClickListener {

            }

            btnVideo.setOnClickListener {

            }

            btnDoc.setOnClickListener {

            }

            btnRestore.setOnClickListener {

            }

            btnBackup.setOnClickListener {

            }

        }
    }

}