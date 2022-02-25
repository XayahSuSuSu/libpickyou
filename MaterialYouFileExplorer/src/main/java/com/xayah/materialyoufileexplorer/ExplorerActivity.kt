package com.xayah.materialyoufileexplorer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import com.xayah.materialyoufileexplorer.adapter.FileListAdapter
import com.xayah.materialyoufileexplorer.databinding.ActivityExplorerBinding

class ExplorerActivity : AppCompatActivity() {
    lateinit var binding: ActivityExplorerBinding
    lateinit var adapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExplorerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding()
        init()
    }

    override fun onBackPressed() {
        if (adapter.pathToString() == "") {
            super.onBackPressed()
        } else {
            adapter.path.removeLast()
            val fileList = adapter.initFileList()
            adapter.fileList = fileList
            binding.topAppBar.subtitle = adapter.pathToString()
            adapter.notifyDataSetChanged()
        }
    }

    private fun binding() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
        binding.topAppBar.title = getString(R.string.choose_dir)
        adapter = FileListAdapter(this)
        adapter.bind(binding)
        adapter.init()
        adapter.initFileList()
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        binding.topAppBar.setNavigationOnClickListener { finish() }
        binding.floatingActionButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.tips))
                .setMessage(getString(R.string.query_dir))
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    val intent =
                        Intent().apply { putExtra("path", adapter.pathToString()) }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                .show()
        }
    }

    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                )
                .request { allGranted, _, _ ->
                    if (!allGranted) {
                        val intent =
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:${this.packageName}")
                        startActivity(intent)
                    }
                }
        } else {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
                .request { _, _, _ ->
                }
        }
    }
}