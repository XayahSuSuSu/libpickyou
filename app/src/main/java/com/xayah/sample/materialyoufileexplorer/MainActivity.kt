package com.xayah.sample.materialyoufileexplorer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.xayah.materialyoufileexplorer.ExplorerActivity
import com.xayah.sample.materialyoufileexplorer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val path = it.data?.getStringExtra("path")
                if (path != null) {
                    binding.textInputEditText.setText(path)
                }
            }

        binding.filledButton.setOnClickListener {
            activityResultLauncher.launch(Intent(this, ExplorerActivity::class.java))
        }
    }
}