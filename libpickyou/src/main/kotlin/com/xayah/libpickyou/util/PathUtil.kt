package com.xayah.libpickyou.util

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.parcelables.FileParcelable
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.SpecialPathAndroid
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.SpecialPathAndroidData
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.SpecialPathAndroidObb
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

internal fun List<String>.subPath(index: Int): List<String> {
    return subList(0, index + 1)
}

internal fun List<String>.toPath(index: Int? = null): String {
    val path = if (index != null) subPath(index) else this
    return path.joinToString(separator = LibPickYouTokens.PathSeparator)
}

internal inline fun tryOn(onTry: () -> Unit) {
    return try {
        onTry()
    } catch (_: Exception) {
    }
}

internal object PathUtil {
    private fun traverseApi24(
        pathString: String,
        files: MutableList<FileParcelable>,
        directories: MutableList<FileParcelable>
    ) {
        val file = File(pathString)
        file.listFiles()?.forEach {
            val fileParcelable = FileParcelable(it.name, it.lastModified())
            if (it.isFile) {
                files.add(fileParcelable)
            } else if (file.isDirectory) {
                directories.add(fileParcelable)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun traverseApi26(
        pathString: String,
        files: MutableList<FileParcelable>,
        directories: MutableList<FileParcelable>
    ) {
        val path = Paths.get(pathString)
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun visitFile(
                file: Path?,
                attrs: BasicFileAttributes?
            ): FileVisitResult {
                if (file != null && attrs != null) {
                    var creationTime = 0L
                    tryOn {
                        val attr = Files.readAttributes(file, BasicFileAttributes::class.java)
                        creationTime = attr.creationTime().toMillis()
                    }
                    val fileParcelable = FileParcelable(file.fileName.pathString, creationTime)
                    tryOn {
                        if (Files.isSymbolicLink(file)) {
                            val link = Files.readSymbolicLink(file)
                            fileParcelable.link = link.pathString
                            if (link.isDirectory()) {
                                directories.add(fileParcelable)
                                return FileVisitResult.CONTINUE
                            }
                        }
                    }
                    files.add(fileParcelable)
                }
                return FileVisitResult.CONTINUE
            }

            override fun preVisitDirectory(
                dir: Path?,
                attrs: BasicFileAttributes?
            ): FileVisitResult {
                return if (dir != null && attrs != null) {
                    if (dir == path) {
                        FileVisitResult.CONTINUE
                    } else {
                        var creationTime = 0L
                        tryOn {
                            val attr = Files.readAttributes(dir, BasicFileAttributes::class.java)
                            creationTime = attr.creationTime().toMillis()
                        }
                        val fileParcelable = FileParcelable(dir.fileName.pathString, creationTime)
                        directories.add(fileParcelable)
                        FileVisitResult.SKIP_SUBTREE
                    }
                } else {
                    FileVisitResult.CONTINUE
                }
            }

            override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(
                dir: Path?,
                exc: IOException?
            ): FileVisitResult {
                return FileVisitResult.CONTINUE
            }
        })
    }

    fun traverse(pathString: String): DirChildrenParcelable {
        val files = mutableListOf<FileParcelable>()
        val directories = mutableListOf<FileParcelable>()
        tryOn {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                traverseApi26(pathString.ifEmpty { "/" }, files, directories)
            } else {
                traverseApi24(pathString, files, directories)
            }
        }

        // Sort by alphabet
        files.sortBy { it.name }
        directories.sortBy { it.name }

        return DirChildrenParcelable(files = files, directories = directories)
    }

    fun traverse(path: DocumentFile): DirChildrenParcelable {
        val files = mutableListOf<FileParcelable>()
        val directories = mutableListOf<FileParcelable>()
        val children = path.listFiles()
        children.forEach {
            runCatching {
                if (it.isDirectory) {
                    directories.add(FileParcelable(it.name!!, it.lastModified()))
                } else {
                    files.add(FileParcelable(it.name!!, it.lastModified()))
                }
            }
        }

        // Sort by alphabet
        files.sortBy { it.name }
        directories.sortBy { it.name }

        return DirChildrenParcelable(files = files, directories = directories)
    }

    fun mkdirs(src: String) = File(src).mkdirs()

    fun traverseSpecialPathAndroid(pathString: String): DirChildrenParcelable {
        val dirChildrenParcelable = traverse(pathString)
        var dataExists = false
        var obbExists = false
        val directories = dirChildrenParcelable.directories.toMutableList()
        directories.forEach {
            if (it.name == "data") dataExists = true
            else if (it.name == "obb") obbExists = true
        }
        if (dataExists.not()) {
            val dataFile = File(SpecialPathAndroidData.toPath())
            if (dataFile.exists()) directories.add(FileParcelable("data", dataFile.lastModified()))
        }
        if (obbExists.not()) {
            val obbFile = File(SpecialPathAndroidObb.toPath())
            if (obbFile.exists()) directories.add(FileParcelable("obb", obbFile.lastModified()))
        }
        dirChildrenParcelable.directories = directories.sortedBy { it.name }.toList()
        return dirChildrenParcelable
    }

    /**
     * Generate children via packages
     * @see <a href="https://github.com/folderv/androidDataWithoutRootAPI33/blob/6e033ddea44cc3366736fd04e85674ed11c7b8b7/app/src/main/java/com/android/test/AppSelectDialogFragment.kt#L58">androidDataWithoutRootAPI33</a>
     */
    fun traverseSpecialPathAndroidDataOrObb(
        pathString: String,
        pm: PackageManager
    ): DirChildrenParcelable {
        val directories = mutableListOf<FileParcelable>()
        val children = hashSetOf<String>()
        val activities = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }, 0)
        activities.forEach {
            val packageName = it.activityInfo.packageName
            if (children.contains(packageName).not()) {
                val child = File(pathString, packageName)
                if (child.exists()) directories.add(FileParcelable(packageName, child.lastModified()))
                children.add(packageName)
            }
        }
        val packages = pm.getInstalledApplications(0)
        packages.forEach {
            val packageName = it.packageName
            if (children.contains(packageName).not()) {
                val child = File(pathString, packageName)
                if (child.exists()) directories.add(FileParcelable(packageName, child.lastModified()))
                children.add(packageName)
            }
        }

        // Sort by alphabet
        directories.sortBy { it.name }

        return DirChildrenParcelable(files = mutableListOf(), directories = directories)
    }

    fun isSpecialPathAndroid(path: String) = path == SpecialPathAndroid.toPath()
    fun isSpecialPathAndroidData(path: String) = path == SpecialPathAndroidData.toPath()
    fun isSpecialPathAndroidObb(path: String) = path == SpecialPathAndroidObb.toPath()

    fun underSpecialPathAndroidData(path: List<String>) = path.toPath().let {
        isSpecialPathAndroidData(it).not() && it.contains(SpecialPathAndroidData.toPath())
    }

    fun underSpecialPathAndroidObb(path: List<String>) = path.toPath().let {
        isSpecialPathAndroidObb(it).not() && it.contains(SpecialPathAndroidObb.toPath())
    }
}
