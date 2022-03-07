# MaterialYouFileExplorer
[![GitHub release](https://img.shields.io/github/v/release/XayahSuSuSu/Android-MaterialYouFileExplorer?color=orange)](https://github.com/XayahSuSuSu/Android-MaterialYouFileExplorer/releases) [![License](https://img.shields.io/github/license/XayahSuSuSu/Android-MaterialYouFileExplorer?color=ff69b4)](./LICENSE)

一个Material You风格的文件浏览器第三方库。

使用这个第三方库来快速选择文件/文件夹。

![Sample3](doc/images/Sample1.jpg "Sample1")

![Sample3](doc/images/Sample2.jpg "Sample2")

![Sample3](doc/images/Sample3.jpg "Sample3")

## 引入
1. 在 `settings.gradle` 中开启 `mavenCentral()`
```
repositories {
        ......
        mavenCentral()
    }
```
2. 引入MaterialYouFileExplorer
```
implementation 'io.github.xayahsususu:materialyoufileexplorer:1.0.6'
```

## 使用
1. 在 `onCreate()` 中初始化
```
val materialYouFileExplorer = MaterialYouFileExplorer()
materialYouFileExplorer.initialize(this)
```
2. 打开Explorer Activity并且处理回调
```
materialYouFileExplorer.toExplorer(this, isFile) { path, isFile -> 
    // Code here
}
```
#### 自定义标题
```
materialYouFileExplorer.toExplorer(this, isFile, "Custom Title") { path, isFile -> 
    // Code here
}
```


## 样本
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
                this, binding.radioButtonFile.isChecked, "Custom Title"
            ) { path, _ -> binding.textInputEditText.setText(path) }
        }
    }
}
```

## 鸣谢
- [libsu](https://github.com/topjohnwu/libsu)
- [PermissionX](https://github.com/guolindev/PermissionX)
- [MaterialFiles](https://github.com/zhanghai/MaterialFiles)