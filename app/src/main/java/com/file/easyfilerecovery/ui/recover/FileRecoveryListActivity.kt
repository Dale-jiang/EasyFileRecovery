package com.file.easyfilerecovery.ui.recover

import android.annotation.SuppressLint
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ActivityFileRecoverListBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator

@SuppressLint("CustomSplashScreen")
class FileRecoveryListActivity : BaseActivity<ActivityFileRecoverListBinding>(ActivityFileRecoverListBinding::inflate) {


    private val recoverType by lazy { intent?.getSerializableExtra("") as? RecoverType }
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

}

