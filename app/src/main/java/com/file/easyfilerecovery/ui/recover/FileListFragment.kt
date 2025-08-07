package com.file.easyfilerecovery.ui.recover

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.blankj.utilcode.util.LogUtils
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.data.StorageType
import com.file.easyfilerecovery.databinding.FragmentFileListBinding
import com.file.easyfilerecovery.ui.base.BaseFragment
import com.file.easyfilerecovery.ui.common.GlobalViewModel
import com.file.easyfilerecovery.ui.common.GlobalViewModel.Companion.allRecoverableFiles

class FileListFragment : BaseFragment<FragmentFileListBinding>(FragmentFileListBinding::inflate) {

    companion object {
        private const val ARG_RECOVER_TYPE = "arg_recover_type"
        private const val ARG_STORAGE_TYPE = "arg_storage_type"

        fun newInstance(recoverType: RecoverType, storageType: StorageType): FileListFragment {
            return FileListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RECOVER_TYPE, recoverType.name)
                    putString(ARG_STORAGE_TYPE, storageType.name)
                }
            }
        }
    }

    private lateinit var recoverType: RecoverType
    private lateinit var storageType: StorageType
    private val globalVm: GlobalViewModel by activityViewModels()


    override fun initUI() {
        arguments?.let {
            recoverType = RecoverType.valueOf(it.getString(ARG_RECOVER_TYPE)!!)
            storageType = StorageType.valueOf(it.getString(ARG_STORAGE_TYPE)!!)
        }


//        val activity = requireActivity() as FileRecoveryListActivity
//        val primaryFiltered = activity.filterFilesBySelection(recoverType, allRecoverableFiles)
//        val slice = primaryFiltered.filter { it.storageType == storageType }

//        LogUtils.e("-------${primaryFiltered.size}{--->>>>>${recoverType.getRecoverName(requireActivity())}--${storageType.name}--${slice.size}")

//            binding.recyclerView.adapter?.let { adapter ->
//                if (adapter is FileListAdapter) {
//                    adapter.submitList(slice)
//                }
//            }

    }

    override fun initListeners() {

    }

}