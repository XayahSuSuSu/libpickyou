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
                this,
                binding.radioButtonFile.isChecked,
                if (binding.checkBox.isChecked) binding.textInputEditTextTitle.text.toString() else "default",
                ArrayList(binding.textInputEditTextFilter.text.toString().split(",")),
                binding.checkBoxFilterWhitelist.isChecked,
                "/storage/emulated/0/Download"
            ) { path, _ -> binding.textInputEditText.setText(path) }
        }
    }
}