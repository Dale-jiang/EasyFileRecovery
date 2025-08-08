package com.file.easyfilerecovery.ui.recover

import androidx.core.view.isVisible
import com.blankj.utilcode.util.ConvertUtils
import com.bumptech.glide.Glide
import com.file.easyfilerecovery.data.FileInfo
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ActivityFileDetailsBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.file.easyfilerecovery.utils.CommonUtils

@Suppress("DEPRECATION")
class FileDetailsActivity : BaseActivity<ActivityFileDetailsBinding>(ActivityFileDetailsBinding::inflate) {

    companion object {
        const val RECOVER_TYPE_KEY = "recover_type_key"
        const val RECOVER_IMAGE_KEY = "recover_image_key"
        const val RECOVER_FILE_INFO_KEY = "recover_file_info_key"
    }

    private val recoverType by lazy { intent?.getSerializableExtra(RECOVER_TYPE_KEY) as? RecoverType }
    private val imageId by lazy { intent?.getIntExtra(RECOVER_IMAGE_KEY, -1) ?: -1 }
    private val fileInfo by lazy { intent?.getParcelableExtra<FileInfo>(RECOVER_FILE_INFO_KEY) }

    override fun initUI() {
        with(binding) {

            val type = recoverType

            tvTitle.text = type?.getRecoverName(this@FileDetailsActivity) ?: ""
            ivPlay.isVisible = type == RecoverType.VIDEO

            fileInfo?.let { info ->
                when {
                    (type == RecoverType.VIDEO || type == RecoverType.PHOTO) && info.filePath.isNotBlank() -> {
                        Glide.with(this@FileDetailsActivity).load(info.filePath).into(ivDetails)
                    }

                    imageId != -1 -> {
                        Glide.with(this@FileDetailsActivity)
                            .load(imageId)
                            .override(ConvertUtils.dp2px(80f), ConvertUtils.dp2px(80f))
                            .into(ivDetails)
                    }

                    else -> ivDetails.setImageDrawable(null)
                }

                imgContainer.setOnClickListener {
                    // TODO：
                }
                btnRecover.setOnClickListener {
                    // TODO：
                }

                tvName.text = info.fileName
                tvPath.text = info.filePath
                tvSaveTime.text = CommonUtils.formatDateTime(info.lastModified, "yyyy/MM/dd HH:mm")
            }
        }
    }

    override fun initListeners() {
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }


}

