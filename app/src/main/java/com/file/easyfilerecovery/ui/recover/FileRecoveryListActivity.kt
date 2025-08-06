package com.file.easyfilerecovery.ui.recover

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ActivityFileRecoverListBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator

@Suppress("DEPRECATION")
class FileRecoveryListActivity : BaseActivity<ActivityFileRecoverListBinding>(ActivityFileRecoverListBinding::inflate) {

    companion object {
        const val RECOVER_TYPE_KEY = "recover_type_key"
    }

    private val recoverType by lazy { intent?.getSerializableExtra(RECOVER_TYPE_KEY) as? RecoverType }
    private var currentTabIndex = 0
    private var tabMediator: TabLayoutMediator? = null

    override fun initUI() {

        binding.apply {

            tvTitle.text = recoverType?.getRecoverName(this@FileRecoveryListActivity) ?: ""


            val tabTitles = listOf(
                "${getString(R.string.str_hidden)}(10)",
                "${getString(R.string.str_storage)}(10)",
                "${getString(R.string.str_album)}(10)"
            )

            currentTabIndex = binding.tabLayout.selectedTabPosition
            tabMediator?.detach()

            val fragments =
                listOf(FileListFragment(), FileListFragment(), FileListFragment())

            binding.viewPager.offscreenPageLimit = fragments.size
            binding.viewPager.adapter = object : FragmentStateAdapter(this@FileRecoveryListActivity) {
                override fun getItemCount(): Int = fragments.size
                override fun createFragment(position: Int) = fragments[position]
            }

            tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position -> tab.text = tabTitles.getOrNull(position) }
            tabMediator?.attach()
            tabLayout.setScrollPosition(currentTabIndex, 0f, false)
            tabLayout.getTabAt(currentTabIndex)?.select()

        }


    }

    override fun initListeners() {

        binding.apply {

            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnFilter.setOnClickListener {

            }

        }

    }


    private fun edgeToEdge() {
        runCatching {
            enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                window.decorView.setPadding(0, systemBarsInsets.top, 0, 0)
                binding.btnContainer.setPadding(0, 0, 0, systemBarsInsets.bottom)
                insets
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
    }

    override fun onAttachedToWindow() {
        edgeToEdge()
    }

}

