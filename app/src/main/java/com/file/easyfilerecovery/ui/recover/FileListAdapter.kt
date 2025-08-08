package com.file.easyfilerecovery.ui.recover

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.FileInfo
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ItemFileMediaBinding
import com.file.easyfilerecovery.databinding.ItemFileOtherBinding
import com.file.easyfilerecovery.databinding.ItemFileTitleBinding
import com.file.easyfilerecovery.utils.CommonUtils

class FileListAdapter(
    private val context: Context,
    private val recoverType: RecoverType,
    private val list: MutableList<FileInfo>,
    private val onChecked: () -> Unit = {},
    private val onItemClick: (item: FileInfo, imgId: Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class TitleHolder(val binding: ItemFileTitleBinding) : RecyclerView.ViewHolder(binding.root)
    inner class MediaHolder(val binding: ItemFileMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: FileInfo) {
            Glide.with(context).load(item.filePath).transform(
                CenterCrop(),
                RoundedCorners(ConvertUtils.dp2px(11f))
            ).into(binding.ivCover)
            binding.ivPlay.isVisible = recoverType == RecoverType.VIDEO
            binding.ivCheck.setImageResource(if (item.checked) R.drawable.ic_item_checked else R.drawable.ic_item_uncheck_grey)
            binding.ivCheck.setOnClickListener {
                item.checked = !item.checked
                notifyItemChanged(layoutPosition)
                onChecked.invoke()
            }
            itemView.setOnClickListener {
                onItemClick.invoke(item, -1)
            }
        }
    }

    inner class FileHolder(val binding: ItemFileOtherBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(item: FileInfo) {
            val imageResource = if (item.mimeType.startsWith("audio")) {
                R.drawable.ic_audio
            } else {
                when (item.mimeType) {
                    "text/plain" -> R.drawable.ic_doc
                    "application/pdf" -> R.drawable.ic_item_pdf
                    "text/csv" -> R.drawable.ic_doc
                    "application/vnd.ms-xpsdocument" -> R.drawable.ic_doc
                    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> R.drawable.ic_item_ppt
                    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> R.drawable.ic_item_excel
                    "application/zip", "application/vnd.rar", "application/x-7z-compressed" -> R.drawable.ic_item_zip
                    else -> R.drawable.ic_item_doc
                }
            }
            binding.ivImage.setImageResource(imageResource)
            binding.tvName.text = item.fileName
            binding.itemSecond.text = CommonUtils.formatDateTime(item.lastModified, "yyyy/MM/dd HH:mm")
            binding.ivCheck.setImageResource(if (item.checked) R.drawable.ic_item_checked else R.drawable.ic_item_uncheck_grey)
            binding.ivCheck.setOnClickListener {
                item.checked = !item.checked
                notifyItemChanged(layoutPosition)
                onChecked.invoke()
            }
            itemView.setOnClickListener {

                onItemClick.invoke(item, imageResource)
            }
        }
    }


    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> TitleHolder(ItemFileTitleBinding.inflate(LayoutInflater.from(context), parent, false))
            1 -> MediaHolder(ItemFileMediaBinding.inflate(LayoutInflater.from(context), parent, false))
            else -> FileHolder(ItemFileOtherBinding.inflate(LayoutInflater.from(context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TitleHolder -> {
                val item = list[holder.layoutPosition]
                holder.binding.tvTitle.text = item.title
                holder.itemView.setOnClickListener(null)
            }

            is MediaHolder -> {
                val item = list[holder.layoutPosition]
                holder.bindData(item)

            }

            is FileHolder -> {
                val item = list[holder.layoutPosition]
                holder.bindData(item)

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = list[position]
        return if (item.isTitle) return 0 else if (recoverType == RecoverType.PHOTO || recoverType == RecoverType.VIDEO) 1 else 2
    }


}