package com.xayah.materialyoufileexplorer.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.ImageLoader
import coil.clear
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.loadAny
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xayah.materialyoufileexplorer.ExplorerViewModel
import com.xayah.materialyoufileexplorer.R
import com.xayah.materialyoufileexplorer.databinding.ActivityExplorerBinding
import com.xayah.materialyoufileexplorer.databinding.AdapterFileBinding
import java.io.File


class FileListAdapter(private val mContext: Context, private val model: ExplorerViewModel) :
    RecyclerView.Adapter<FileListAdapter.Holder>() {
    class Holder(val binding: AdapterFileBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var activityBinding: ActivityExplorerBinding

    private var isFile = false

    private val supportExt = arrayListOf("jpg", "png", "mp4")

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
                binding.iconView.loadAny(file)
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
                notifyDataSetChanged()
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

    fun initializeCoil() {
        Coil.setImageLoader(
            ImageLoader.Builder(mContext)
                .componentRegistry {
                    add(ImageDecoderDecoder(mContext))
                    add(GifDecoder())
                    add(SvgDecoder(mContext, false))
                }
                .build()
        )
    }
}