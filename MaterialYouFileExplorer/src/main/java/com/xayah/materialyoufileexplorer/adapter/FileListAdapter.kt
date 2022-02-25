package com.xayah.materialyoufileexplorer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.xayah.materialyoufileexplorer.R
import com.xayah.materialyoufileexplorer.databinding.ActivityExplorerBinding
import com.xayah.materialyoufileexplorer.databinding.AdapterFileBinding
import com.xayah.materialyoufileexplorer.model.FileInfo
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


class FileListAdapter(private val mContext: Context) :
    RecyclerView.Adapter<FileListAdapter.Holder>() {
    class Holder(val binding: AdapterFileBinding) : RecyclerView.ViewHolder(binding.root)

    var fileList: MutableList<FileInfo> = mutableListOf()
    var path = mutableListOf("", "storage", "emulated", "0")
    private lateinit var activityBinding: ActivityExplorerBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            AdapterFileBinding
                .inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = fileList[position]
        val binding = holder.binding
        binding.titleView.text = current.name
        if (current.isDir) {
            binding.iconView.background =
                AppCompatResources.getDrawable(mContext, R.drawable.ic_round_folder)
        } else {
            binding.iconView.background =
                AppCompatResources.getDrawable(mContext, R.drawable.ic_round_file)
        }
        binding.content.setOnClickListener {
            if (current.isDir) {
                val dirName = binding.titleView.text
                path.add(dirName.toString())
                fileList = initFileList()
                activityBinding.topAppBar.subtitle = pathToString()
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
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

    fun init() {
        fileList = initFileList()
        activityBinding.topAppBar.subtitle = pathToString()
    }

    fun initFileList(): MutableList<FileInfo> {
        val path = Paths.get(pathToString())
        val folders = mutableListOf<FileInfo>()
        val files = mutableListOf<FileInfo>()

        try {
            Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    return if (dir == path) {
                        FileVisitResult.CONTINUE
                    } else {
                        folders.add(FileInfo(dir.toString().split("/").last(), true))
                        FileVisitResult.SKIP_SUBTREE
                    }
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    files.add(FileInfo(file.toString().split("/").last(), false))
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (folders + files) as MutableList<FileInfo>
    }

    fun pathToString(): String {
        return path.joinToString(separator = "/")
    }
}