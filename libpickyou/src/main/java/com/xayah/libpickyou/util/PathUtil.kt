package com.xayah.libpickyou.util

import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.parcelables.FileParcelable
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
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

internal class PathUtil {
    companion object {
        fun traverse(path: Path): DirChildrenParcelable {
            val files = mutableListOf<FileParcelable>()
            val directories = mutableListOf<FileParcelable>()
            tryOn {
                Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                    override fun visitFile(
                        file: Path?,
                        attrs: BasicFileAttributes?
                    ): FileVisitResult {
                        if (file != null && attrs != null) {
                            val attr = Files.readAttributes(file, BasicFileAttributes::class.java)
                            val creationTime = attr.creationTime().toMillis()
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
                                val attr =
                                    Files.readAttributes(dir, BasicFileAttributes::class.java)
                                val creationTime = attr.creationTime().toMillis()
                                val fileParcelable =
                                    FileParcelable(dir.fileName.pathString, creationTime)
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

            // Sort by alphabet
            files.sortBy { it.name }
            directories.sortBy { it.name }

            return DirChildrenParcelable(files = files, directories = directories)
        }
    }
}
