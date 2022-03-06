package com.xayah.sample.materialyoufileexplorer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.xayah.materialyoufileexplorer.MaterialYouFileExplorer
import com.xayah.sample.materialyoufileexplorer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val materialYouFileExplorer = MaterialYouFileExplorer()
        materialYouFileExplorer.initialize(this)

        binding.filledButton.setOnClickListener {
            materialYouFileExplorer.toExplorer(
                this, binding.radioButtonFile.isChecked, "Custom Title"
            ) { path, _ -> binding.textInputEditText.setText(path) }
        }
    }

}