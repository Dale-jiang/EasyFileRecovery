package com.file.easyfilerecovery.ui.common

import android.annotation.SuppressLint
import androidx.activity.addCallback
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.databinding.ActivityCommonScanBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.file.easyfilerecovery.ui.recover.FileRecoveryListActivity
import com.file.easyfilerecovery.utils.launchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class CommonScanActivity : BaseActivity<ActivityCommonScanBinding>(ActivityCommonScanBinding::inflate) {


    override fun initUI() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(2500L)
            handleResult()
        }
    }

    override fun initListeners() {
        onBackPressedDispatcher.addCallback {
            finish()
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnOk.setOnClickListener {
            launchActivity<FileRecoveryListActivity>(finish = true)
        }
    }

    private fun handleResult() {
        binding.lottieView.animate().scaleX(0.5f).scaleY(0.5f).alpha(0f).setDuration(500L)
            .withEndAction {
                binding.lottieView.pauseAnimation()
                binding.groupScan.isVisible = false
                binding.tvResult.text = HtmlCompat.fromHtml(String.format(getString(R.string.str_scan_result), "20"), HtmlCompat.FROM_HTML_MODE_LEGACY)
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

