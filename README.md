<div align="center">
	<span style="font-weight: bold"> English</span>
</div>

# PickYou
[![GitHub release](https://img.shields.io/github/v/tag/XayahSuSuSu/AndroidModule-PickYou?color=orange)](https://github.com/XayahSuSuSu/AndroidModule-PickYou/tags)  ![minSdk](https://img.shields.io/badge/minSdk-26-green) [![License](https://img.shields.io/github/license/XayahSuSuSu/AndroidModule-PickYou?color=ff69b4)](./LICENSE)

File picker module with the style of Material You.

Use this module to pick files/directories easily.

## Features
- Write in [**compose**](https://developer.android.com/jetpack/compose)
- Easy to import and use.
- Highly customizable.
- Support for single/multiple selection.

## Screenshots
<div align="center">
	<img src="./doc/images/1.jpg" width="275px"><img src="./doc/images/2.jpg" width="275px"><img src="./doc/images/3.jpg" width="275px">
	<img src="./doc/images/4.jpg" width="275px"><img src="./doc/images/5.jpg" width="275px"><img src="./doc/images/6.jpg" width="275px">
</div>

## Implementation
1. Enable **JitPack** in `settings.gradle`/`settings.gradle.kts`
* **Groovy**
```
repositories {
    // ......
    maven { url 'https://jitpack.io' }
}
```
* **Kotlin**
```
repositories {
    // ......
    maven("https://jitpack.io")
}
```
2. Implementation
* **Groovy**
```
implementation 'com.github.XayahSuSuSu:AndroidModule-PickYou:$PickYouVersion'
```

* **Kotlin**
```
implementation("com.github.XayahSuSuSu:AndroidModule-PickYou:$PickYouVersion")
```

## Usage
1. Launch anywhere
```
val launcher = PickYouLauncher()
launcher.launch(this) { path ->
    // Code here.
}
```

2. Customization

See [PickYouLauncher](./libpickyou/src/main/java/com/xayah/libpickyou/ui/PickYouLauncher.kt)

## Sample
See [sample](./app/src/main/java/com/xayah/pickyou/MainActivity.kt)

## Credits
- [libsu](https://github.com/topjohnwu/libsu)
- [MaterialFiles](https://github.com/zhanghai/MaterialFiles)
