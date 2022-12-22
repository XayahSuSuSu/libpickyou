<div align="center">
	<span style="font-weight: bold"> English | <a href=README_CN.md> 中文 </a> </span>
</div>

# MaterialYouFileExplorer
[![GitHub release](https://img.shields.io/github/v/release/XayahSuSuSu/Android-MaterialYouFileExplorer?color=orange)](https://github.com/XayahSuSuSu/Android-MaterialYouFileExplorer/releases)  ![minSdk](https://img.shields.io/badge/minSdk-26-green) [![License](https://img.shields.io/github/license/XayahSuSuSu/Android-MaterialYouFileExplorer?color=ff69b4)](./LICENSE)

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
1. Enable `maven { url 'https://jitpack.io' }` in `settings.gradle`
```
repositories {
        ......
        maven { url 'https://jitpack.io' }
    }
```
2. Implementation
```
implementation 'com.github.XayahSuSuSu:Android-MaterialYouFileExplorer:1.3.1'
```

## Usage
1. Initialize in `onCreate()`
```
val materialYouFileExplorer = MaterialYouFileExplorer().apply {
    initialize(this@MainActivity)
}
```
2. Start the explorer activity and handle callback
```
materialYouFileExplorer.toExplorer(context) { path, isFile -> 
    // Code here
}
```
#### Custom title
```
materialYouFileExplorer.title = "Custom Title"
```

#### *Shell Initialization
If you are using the `com.github.topjohnwu.libsu` dependency in your main project, make sure to add the `FLAG_MOUNT_MASTER` flag when initializing the shell.
```
Shell.setDefaultBuilder(
    Shell.Builder.create()
        .setFlags(Shell.FLAG_MOUNT_MASTER or Shell.FLAG_REDIRECT_STDERR)
        .setTimeout(10)
)
```


## Sample
```
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
```

## Credits
- [libsu](https://github.com/topjohnwu/libsu)
- [PermissionX](https://github.com/guolindev/PermissionX)
- [MaterialFiles](https://github.com/zhanghai/MaterialFiles)
- [Coil](https://github.com/coil-kt/coil)
- [ActivityResultLauncher](https://github.com/DylanCaiCoding/ActivityResultLauncher)