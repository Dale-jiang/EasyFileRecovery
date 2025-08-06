package com.file.easyfilerecovery.ui.common

import androidx.activity.addCallback
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ActivityCommonScanBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.file.easyfilerecovery.ui.common.GlobalViewModel.Companion.allRecoverableFiles
import com.file.easyfilerecovery.ui.recover.FileRecoveryListActivity
import com.file.easyfilerecovery.ui.recover.FileRecoveryListActivity.Companion.RECOVER_TYPE_KEY
import com.file.easyfilerecovery.utils.launchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class CommonScanActivity : BaseActivity<ActivityCommonScanBinding>(ActivityCommonScanBinding::inflate) {

    private val recoverType by lazy { intent?.getSerializableExtra(RECOVER_TYPE_KEY) as? RecoverType }

    private val globalVm: GlobalViewModel by lazy {
        ViewModelProvider(application as ViewModelStoreOwner, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[GlobalViewModel::class.java]
    }

    override fun initUI() {
        globalVm.scanRecoverableFiles(this, recoverType)
    }

    override fun initListeners() {
        onBackPressedDispatcher.addCallback {
            finish()
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnOk.setOnClickListener {
            launchActivity<FileRecoveryListActivity>(finish = true) {
                putExtra(RECOVER_TYPE_KEY, recoverType)
            }
        }


        globalVm.onScanCompletedLiveData.observe(this) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(2000L)
                handleResult()
            }
        }
    }

    private fun handleResult() {
        binding.lottieView.animate().scaleX(0.5f).scaleY(0.5f).alpha(0f).setDuration(500L)
            .withEndAction {
                binding.lottieView.pauseAnimation()
                binding.groupScan.isVisible = false
                binding.tvResult.text = HtmlCompat.fromHtml(
                    String.format(getString(R.string.str_scan_result), allRecoverableFiles.size.toString()),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                binding.groupComplete.isVisible = true
                binding.ivComplete.apply {
                    scaleX = 0f
                    scaleY = 0f
                    alpha = 0f
                    isVisible = true
                    animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(400L).start()
                }
            }.start()
    }

}

