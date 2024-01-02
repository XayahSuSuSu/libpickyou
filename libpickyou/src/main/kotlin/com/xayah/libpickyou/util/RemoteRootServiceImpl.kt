package com.xayah.libpickyou.util

import android.os.Parcel
import android.os.ParcelFileDescriptor
import com.topjohnwu.superuser.ShellUtils
import com.xayah.libpickyou.IRemoteRootService
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import java.io.File
import java.nio.file.Paths

internal class RemoteRootServiceImpl : IRemoteRootService.Stub() {
    private val lock = Any()

    init {
        /**
         * If [LibPickYouTokens.ParcelTmpFilePath] has incorrect SELinux context, the transaction will get failed:
         * Fatal Exception: android.os.DeadObjectException: Transaction failed on small parcel; remote process probably died, but this could also be caused by running out of binder buffe
         * Correct SELinux context should be: u:object_r:shell_data_file:s0
         *
         * If [LibPickYouTokens.ParcelTmpFilePath] doesn't exist, the transaction will failed:
         * pfd must not be null
         */
        ShellUtils.fastCmd(
            """
            mkdir -p "${LibPickYouTokens.ParcelTmpFilePath}/"
            """.trimIndent()
        )
        ShellUtils.fastCmd(
            """
            chcon -hR "u:object_r:shell_data_file:s0" "${LibPickYouTokens.ParcelTmpFilePath}/"
            """.trimIndent()
        )
    }

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
