<div align="center">
	<span style="font-weight: bold"> English | <a href=README_CN.md> 中文 </a> </span>
</div>

# MaterialYouFileExplorer
[![GitHub release](https://img.shields.io/github/v/release/XayahSuSuSu/Android-MaterialYouFileExplorer?color=orange)](https://github.com/XayahSuSuSu/Android-MaterialYouFileExplorer/releases) [![License](https://img.shields.io/github/license/XayahSuSuSu/Android-MaterialYouFileExplorer?color=ff69b4)](./LICENSE)

A file explorer(picker) with the style of Material You.

Use this library to pick files/directories quickly.

## Features
- Easy to import and use.
- Highly customizable.
- Support for both file and directory
- Support for filtering
- Support for managing file/directory (deleting, renaming) while picking.

## Screenshots

![Sample3](doc/images/Sample1.jpg "Sample1")

![Sample3](doc/images/Sample2.jpg "Sample2")

![Sample3](doc/images/Sample3.jpg "Sample3")

## Implementation
1. Enable `mavenCentral()` in `settings.gradle`
```
repositories {
        ......
        mavenCentral()
    }
```
2. Implementation
```
implementation 'io.github.xayahsususu:materialyoufileexplorer:1.0.7'
```

## Usage
1. Initialize in `onCreate()`
```
val materialYouFileExplorer = MaterialYouFileExplorer()
materialYouFileExplorer.initialize(this)
```
2. Start the explorer activity and handle callback
```
materialYouFileExplorer.toExplorer(this, isFile) { path, isFile -> 
    // Code here
}
```
#### Custom title
```
materialYouFileExplorer.toExplorer(this, isFile, "Custom Title") { path, isFile -> 
    // Code here
}
```


## Sample
```
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
                binding.checkBoxFilterWhitelist.isChecked
            ) { path, _ -> binding.textInputEditText.setText(path) }
        }
    }
}
```

## Credits
- [libsu](https://github.com/topjohnwu/libsu)
- [PermissionX](https://github.com/guolindev/PermissionX)
- [MaterialFiles](https://github.com/zhanghai/MaterialFiles)
- [Coil](https://github.com/coil-kt/coil)
- [ActivityResultLauncher](https://github.com/DylanCaiCoding/ActivityResultLauncher)