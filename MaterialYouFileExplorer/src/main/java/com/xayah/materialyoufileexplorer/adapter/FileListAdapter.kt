package com.xayah.materialyoufileexplorer.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.ImageLoader
import coil.clear
import coil.decode.VideoFrameDecoder
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.io.SuFile
import com.xayah.materialyoufileexplorer.ExplorerActivity
import com.xayah.materialyoufileexplorer.ExplorerViewModel
import com.xayah.materialyoufileexplorer.R
import com.xayah.materialyoufileexplorer.databinding.ActivityExplorerBinding
import com.xayah.materialyoufileexplorer.databinding.AdapterFileBinding
import com.xayah.materialyoufileexplorer.databinding.DialogTextFieldBinding
import com.xayah.materialyoufileexplorer.model.FileInfo
import com.xayah.materialyoufileexplorer.util.PathUtil
import java.io.File


class FileListAdapter(private val mContext: Context, private val model: ExplorerViewModel) :
    RecyclerView.Adapter<FileListAdapter.Holder>() {
    class Holder(val binding: AdapterFileBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var activityBinding: ActivityExplorerBinding

    private var isFile = false

    private val supportExt = setOf(
        /* Video / Container */
        "264", "265", "3g2", "3ga", "3gp", "3gp2", "3gpp", "3gpp2", "3iv", "amr", "asf",
        "asx", "av1", "avc", "avf", "avi", "bdm", "bdmv", "clpi", "cpi", "divx", "dv", "evo",
        "evob", "f4v", "flc", "fli", "flic", "flv", "gxf", "h264", "h265", "hdmov", "hdv",
        "hevc", "lrv", "m1u", "m1v", "m2t", "m2ts", "m2v", "m4u", "m4v", "mkv", "mod", "moov",
        "mov", "mp2", "mp2v", "mp4", "mp4v", "mpe", "mpeg", "mpeg2", "mpeg4", "mpg", "mpg4",
        "mpl", "mpls", "mpv", "mpv2", "mts", "mtv", "mxf", "mxu", "nsv", "nut", "ogg", "ogm",
        "ogv", "ogx", "qt", "qtvr", "rm", "rmj", "rmm", "rms", "rmvb", "rmx", "rv", "rvx",
        "sdp", "tod", "trp", "ts", "tsa", "tsv", "tts", "vc1", "vfw", "vob", "vro", "webm",
        "wm", "wmv", "wmx", "x264", "x265", "xvid", "y4m", "yuv",

        /* Picture */
        "apng", "bmp", "exr", "gif", "j2c", "j2k", "jfif", "jp2", "jpc", "jpe", "jpeg", "jpg",
        "jpg2", "png", "tga", "tif", "tiff", "webp",
    )

    private lateinit var activity: AppCompatActivity

    private fun isThumbnailable (ext: String): Boolean {
        return supportExt.contains(ext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            AdapterFileBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = model.fileList[position]
        val binding = holder.binding
        binding.titleView.text = current.name
        binding.iconView.clear()
        if (current.isDir) {
            if (current.name == "..") {
                binding.iconView.background =
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_round_return)
            } else {
                    binding.iconView.background =
                        AppCompatResources.getDrawable(mContext, R.drawable.ic_round_folder)
            }
        } else {
            val file = File(model.getPath(current.name))
            if (isThumbnailable(file.extension)) {
                binding.iconView.background = null
                binding.iconView.load(file)
            } else {
                binding.iconView.background =
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_round_file)
            }
        }
        binding.content.setOnClickListener {
            val dirName = binding.titleView.text

            if (current.isDir) {
                if (current.name == "..") {
                    model.removePath()
                } else {
                    model.addPath(dirName.toString())
                }
            } else {
                if (isFile) {
                    MaterialAlertDialogBuilder(activity).setTitle(mContext.getString(R.string.tips))
                        .setMessage(mContext.getString(R.string.query_file))
                        .setNegativeButton(mContext.getString(R.string.cancel)) { _, _ -> }
                        .setPositiveButton(mContext.getString(R.string.confirm)) { _, _ ->
                            model.addPath(dirName.toString())
                            val intent = Intent().apply {
                                putExtra("path", model.getPath())
                                putExtra("isFile", isFile)
                            }
                            activity.setResult(Activity.RESULT_OK, intent)
                            activity.finish()
                        }.show()
                }
            }
        }

        binding.content.setOnLongClickListener {
            showPopupMenu(it, current)
            true
        }
    }

    override fun getItemCount(): Int {
        return model.fileList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun bind(binding: ActivityExplorerBinding) {
        this.activityBinding = binding
    }

    fun init(activity: AppCompatActivity, isFile: Boolean) {
        initializeCoil()
        this.activity = activity
        this.isFile = isFile
    }

    private fun initializeCoil() {
        Coil.setImageLoader(
            ImageLoader.Builder(mContext)
                .componentRegistry {
                    add(VideoFrameDecoder(mContext))
                }
                .build()
        )
    }

    private fun showPopupMenu(v: View, fileInfo: FileInfo) {
        val popupMenu = PopupMenu(v.context, v, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.menu_on_long_click, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_delete -> {
                    MaterialAlertDialogBuilder(activity).setTitle(mContext.getString(R.string.tips))
                        .setMessage(mContext.getString(R.string.query_delete_file))
                        .setNegativeButton(mContext.getString(R.string.cancel)) { _, _ -> }
                        .setPositiveButton(mContext.getString(R.string.confirm)) { _, _ ->
                            val filePath = "${model.getPath()}/${fileInfo.name}"
                            if (!model.getPath().contains(PathUtil.STORAGE_EMULATED_0) or model.getPath()
                                    .contains(PathUtil.STORAGE_EMULATED_0_ANDROID)
                            ) {
                                if (ExplorerActivity.rootAccess) {
                                    val file = SuFile(filePath)
                                    file.deleteRecursive()
                                    model.refreshPath()
                                }
                            } else {
                                val file = File(filePath)
                                file.deleteRecursively()
                                model.refreshPath()
                            }
                        }.show()
                }
                R.id.menu_rename -> {
                    val bindingDialogTextField =
                        DialogTextFieldBinding.inflate(activity.layoutInflater)
                    bindingDialogTextField.textLayout.hint = mContext.getString(R.string.new_name)
                    bindingDialogTextField.textField.setText(fileInfo.name)
                    MaterialAlertDialogBuilder(activity)
                        .setTitle(mContext.getString(R.string.rename))
                        .setView(bindingDialogTextField.root)
                        .setCancelable(true)
                        .setPositiveButton(mContext.getString(R.string.confirm)) { _, _ ->
                            val filePath = "${model.getPath()}/${fileInfo.name}"
                            val newFilePath =
                                "${model.getPath()}/${bindingDialogTextField.textField.text}"
                            if (!model.getPath().contains(PathUtil.STORAGE_EMULATED_0) or model.getPath()
                                    .contains(PathUtil.STORAGE_EMULATED_0_ANDROID)
                            ) {
                                if (ExplorerActivity.rootAccess) {
                                    val file = SuFile(filePath)
                                    val newFile = SuFile(newFilePath)
                                    file.renameTo(newFile)
                                    model.refreshPath()
                                }
                            } else {
                                val file = File(filePath)
                                val newFile = File(newFilePath)
                                file.renameTo(newFile)
                                model.refreshPath()
                            }
                        }
                        .setNegativeButton(mContext.getString(R.string.cancel)) { _, _ -> }
                        .show()
                }
            }
            true
        }
    }
}