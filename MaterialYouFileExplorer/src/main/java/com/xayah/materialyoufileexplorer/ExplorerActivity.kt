package com.xayah.materialyoufileexplorer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.xayah.materialyoufileexplorer.adapter.FileListAdapter
import com.xayah.materialyoufileexplorer.databinding.ActivityExplorerBinding
import com.xayah.materialyoufileexplorer.model.FileInfo
import com.xayah.materialyoufileexplorer.util.PathUtil
import com.xayah.materialyoufileexplorer.util.UriUtil
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit
import kotlin.io.path.extension


class ExplorerActivity : AppCompatActivity() {
    lateinit var binding: ActivityExplorerBinding
    lateinit var adapter: FileListAdapter
    val model: ExplorerViewModel by viewModels()
    lateinit var openDocumentTreeLauncher: ActivityResultLauncher<Uri>

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER or Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(TimeUnit.MILLISECONDS.toSeconds(15 * 1000L))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityExplorerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding()
        init()
    }

    override fun onBackPressed() {
        PathUtil.onBack(model, this)
    }

    private fun binding() {
        val isFile = intent.getBooleanExtra("isFile", false)
        val suffixFilter = intent.getStringArrayListExtra("suffixFilter")
        val hasFilter = suffixFilter != null
        val filterWhitelist = intent.getBooleanExtra("filterWhitelist", true)

        adapter = FileListAdapter(this, model)
        adapter.bind(binding)
        adapter.init(this, isFile)
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        binding.topAppBar.setNavigationOnClickListener { finish() }

        model.pathList.observe(this) {
            val pathStr = model.getPath()
            val path = Paths.get(pathStr)
            model.folders.clear()
            model.files.clear()
            if (pathStr != "")
                model.folders.add(FileInfo("..", true))
            PathUtil.handleSpecialPath(pathStr, {
                if (Shell.rootAccess()) {
                    val rootFile = SuFile.open(pathStr)
                    if (rootFile.exists()) {
                        try {
                            val list = rootFile.listFiles()!!
                            for (i in list) {
                                if (i.isFile) {
                                    if (!hasFilter || suffixFilter?.contains(i.extension) == filterWhitelist)
                                        model.files.add(FileInfo(i.name, false))
                                } else {
                                    model.folders.add(FileInfo(i.name, true))
                                }
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, {
                val file = File(pathStr)
                if (file.exists()) {
                    try {
                        val list = file.listFiles()!!
                        for (i in list) {
                            if (i.isFile) {
                                if (!hasFilter || suffixFilter?.contains(i.extension) == filterWhitelist) model.files.add(
                                    FileInfo(i.name, false)
                                )
                            } else {
                                model.folders.add(FileInfo(i.name, true))
                            }
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }, {
                if (Shell.rootAccess()) {
                    val rootFile = SuFile.open(pathStr)
                    if (rootFile.exists()) {
                        try {
                            val list = rootFile.listFiles()!!
                            for (i in list) {
                                if (i.isFile) {
                                    if (!hasFilter || suffixFilter?.contains(i.extension) == filterWhitelist)
                                        model.files.add(FileInfo(i.name, false))
                                } else {
                                    model.folders.add(FileInfo(i.name, true))
                                }
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                        if (!DocumentFile.fromTreeUri(
                                this,
                                UriUtil.DOCUMENT_URI_ANDROID_DATA_ACCESS
                            )!!.canRead()
                        ) {
                            openDocumentTreeLauncher.launch(UriUtil.DOCUMENT_URI_ANDROID_DATA)
                        }
                        val documentFile =
                            if (pathStr == PathUtil.STORAGE_EMULATED_0_ANDROID_DATA) {
                                DocumentFile.fromTreeUri(
                                    this,
                                    UriUtil.DOCUMENT_URI_ANDROID_DATA_ACCESS
                                )
                            } else {
                                model.documentFileList.last()
                            }
                        if (documentFile != null) {
                            if (documentFile.exists()) {
                                try {
                                    val list = documentFile.listFiles()
                                    for (i in list) {
                                        if (i.isFile) {
                                            if (!hasFilter || suffixFilter?.contains(
                                                    i.name!!.split(".").last()
                                                ) == filterWhitelist
                                            )
                                                model.files.add(
                                                    FileInfo(i.name ?: "", false, i)
                                                )
                                        } else {
                                            model.folders.add(FileInfo(i.name ?: "", true, i))
                                        }
                                    }
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }, {
                if (Shell.rootAccess()) {
                    val rootFile = SuFile.open(pathStr)
                    if (rootFile.exists()) {
                        try {
                            val list = rootFile.listFiles()!!
                            for (i in list) {
                                if (i.isFile) {
                                    if (!hasFilter || suffixFilter?.contains(i.extension) == filterWhitelist)
                                        model.files.add(FileInfo(i.name, false))
                                } else {
                                    model.folders.add(FileInfo(i.name, true))
                                }
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                        if (!DocumentFile.fromTreeUri(
                                this,
                                UriUtil.DOCUMENT_URI_ANDROID_OBB_ACCESS
                            )!!.canRead()
                        ) {
                            openDocumentTreeLauncher.launch(UriUtil.DOCUMENT_URI_ANDROID_OBB)
                        }
                        val documentFile = if (pathStr == PathUtil.STORAGE_EMULATED_0_ANDROID_OBB) {
                            DocumentFile.fromTreeUri(
                                this,
                                UriUtil.DOCUMENT_URI_ANDROID_OBB_ACCESS
                            )
                        } else {
                            model.documentFileList.last()
                        }
                        if (documentFile != null) {
                            if (documentFile.exists()) {
                                try {
                                    val list = documentFile.listFiles()
                                    for (i in list) {
                                        if (i.isFile) {
                                            if (!hasFilter || suffixFilter?.contains(
                                                    i.name!!.split(".").last()
                                                ) == filterWhitelist
                                            )
                                                model.files.add(
                                                    FileInfo(i.name ?: "", false, i)
                                                )
                                        } else {
                                            model.folders.add(FileInfo(i.name ?: "", true, i))
                                        }
                                    }
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }, {
                try {
                    Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                        @Throws(IOException::class)
                        override fun preVisitDirectory(
                            dir: Path, attrs: BasicFileAttributes
                        ): FileVisitResult {
                            return if (dir == path) {
                                FileVisitResult.CONTINUE
                            } else {
                                model.folders.add(FileInfo(dir.toString().split("/").last(), true))
                                FileVisitResult.SKIP_SUBTREE
                            }
                        }

                        @Throws(IOException::class)
                        override fun visitFile(
                            file: Path, attrs: BasicFileAttributes
                        ): FileVisitResult {
                            if (!hasFilter || suffixFilter?.contains(file.extension) == filterWhitelist)
                                model.files.add(FileInfo(file.toString().split("/").last(), false))
                            return FileVisitResult.CONTINUE
                        }

                        @Throws(IOException::class)
                        override fun visitFileFailed(
                            file: Path,
                            exc: IOException?
                        ): FileVisitResult {
                            return FileVisitResult.CONTINUE
                        }

                        @Throws(IOException::class)
                        override fun postVisitDirectory(
                            dir: Path,
                            exc: IOException?
                        ): FileVisitResult {
                            return FileVisitResult.CONTINUE
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

            model.fileList = (model.folders + model.files) as MutableList<FileInfo>
            binding.topAppBar.subtitle =
                "${if (pathStr != "") model.folders.size - 1 else model.folders.size} ${
                    getString(
                        R.string.folders
                    )
                }, ${model.files.size} ${getString(R.string.files)}"

            binding.chipGroup.removeAllViews()
            for ((index, i) in it.withIndex()) {
                if (i != "") {
                    val chip = Chip(this)
                    chip.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    chip.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    chip.chipStartPadding = resources.getDimension(R.dimen.chip_padding)
                    chip.chipEndPadding = resources.getDimension(R.dimen.chip_padding)
                    chip.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    chip.chipStrokeWidth = 0F
                    chip.chipIcon =
                        AppCompatResources.getDrawable(
                            this,
                            R.drawable.ic_round_keyboard_arrow_left
                        )
                    chip.setOnClickListener {
                        model.returnPath(index)
                    }
                    chip.text = i
                    binding.chipGroup.addView(chip)
                    binding.horizontalScrollView.post {
                        binding.horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }

        binding.floatingActionButton.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle(getString(R.string.tips))
                .setMessage(getString(R.string.query_dir))
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    val intent = Intent().apply {
                        putExtra("path", model.getPath())
                        putExtra("isFile", isFile)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }.show()
        }

        if (isFile) {
            binding.floatingActionButton.visibility = View.GONE
        }
        if (intent.getStringExtra("title") == "default") {
            binding.topAppBar.title =
                if (isFile) getString(R.string.choose_file) else getString(R.string.choose_dir)
        } else {
            binding.topAppBar.title = intent.getStringExtra("title")
        }
    }

    private fun init() {
        openDocumentTreeLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree(), this::onOpenDocumentTreeResult
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PermissionX.init(this).permissions(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            ).request { allGranted, _, _ ->
                if (!allGranted) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${this.packageName}")
                    startActivity(intent)
                }
            }
        } else {
            PermissionX.init(this).permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ).request { _, _, _ ->
            }
        }
    }

    private fun onOpenDocumentTreeResult(result: Uri?) {
        if (result != null) {
            contentResolver.takePersistableUriPermission(
                result,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            model.refreshPath()
        }
    }
}