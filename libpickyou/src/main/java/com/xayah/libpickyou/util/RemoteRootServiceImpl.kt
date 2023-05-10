package com.xayah.libpickyou.util

import android.os.Parcel
import android.os.ParcelFileDescriptor
import com.xayah.libpickyou.IRemoteRootService
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import java.io.File
import java.nio.file.Paths

internal class RemoteRootServiceImpl : IRemoteRootService.Stub() {
    private val lock = Any()

    override fun traverse(path: String): ParcelFileDescriptor {
        synchronized(lock) {
            val parcel = Parcel.obtain()
            parcel.setDataPosition(0)
            val children = PathUtil.traverse(Paths.get(path))
            children.writeToParcel(parcel, 0)

            val tmp = File(LibPickYouTokens.ParcelTmpFilePath, LibPickYouTokens.ParcelTmpFileName)
            tmp.createNewFile()
            tmp.writeBytes(parcel.marshall())
            val pfd = ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_WRITE)
            tmp.deleteRecursively()

            parcel.recycle()
            return pfd
        }
    }
}
