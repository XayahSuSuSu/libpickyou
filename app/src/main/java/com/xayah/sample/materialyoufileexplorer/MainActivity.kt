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

        val materialYouFileExplorer = MaterialYouFileExplorer().apply {
            initialize(this@MainActivity)
        }

        binding.filledButton.setOnClickListener {
            materialYouFileExplorer.apply {
                isFile = binding.radioButtonFile.isChecked
                title =
                    if (binding.checkBox.isChecked) binding.textInputEditTextTitle.text.toString() else "default"
                suffixFilter = ArrayList(binding.textInputEditTextFilter.text.toString().split(","))
                filterWhitelist = binding.checkBoxFilterWhitelist.isChecked
                defPath = "/storage/emulated/0/Download"

                toExplorer(it.context) { path, _ ->
                    binding.textInputEditText.setText(
                        path
                    )
                }
            }
        }
    }
}