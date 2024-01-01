package com.xayah.libpickyou.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.widget.Toast
import com.topjohnwu.superuser.ipc.RootService
import com.xayah.libpickyou.IRemoteRootService
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.pathString

internal class RemoteRootService(private val context: Context) {
    private var mService: IRemoteRootService? = null
    private var isFirstConnection = true

    class RemoteRootService : RootService() {
        override fun onBind(intent: Intent): IBinder {
            return RemoteRootServiceImpl()
        }
    }

    private suspend fun bindService(): IRemoteRootService = suspendCoroutine { continuation ->
        if (mService == null) {
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    mService = IRemoteRootService.Stub.asInterface(service)
                    continuation.resume(mService!!)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    mService = null
                    val msg = LibPickYouTokens.ServiceDisconnectedToast
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(RemoteException(msg))
                }

                override fun onBindingDied(name: ComponentName) {
                    mService = null
                    val msg = LibPickYouTokens.ServiceBindingDiedToast
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(RemoteException(msg))
                }

                override fun onNullBinding(name: ComponentName) {
                    mService = null
                    val msg = LibPickYouTokens.ServiceNullBindingToast
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(RemoteException(msg))
                }
            }
            val intent = Intent().apply {
                component = ComponentName(context.packageName, RemoteRootService::class.java.name)
            }
            RootService.bind(intent, connection)
        } else {
            mService
        }
    }

    private suspend fun getService(): IRemoteRootService {
        return if (mService == null) {
            val msg = LibPickYouTokens.ServiceNullToast
            if (isFirstConnection)
                isFirstConnection = false
            else
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            bindService()
        } else if (mService!!.asBinder().isBinderAlive.not()) {
            mService = null
            val msg = LibPickYouTokens.ServiceDeadToast
            withContext(Dispatchers.Main) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            bindService()
        } else {
            mService!!
        }
    }

    suspend fun traverse(path: Path): DirChildrenParcelable {
        val pfd = getService().traverse(path.pathString)
        val stream = ParcelFileDescriptor.AutoCloseInputStream(pfd)
        val bytes = stream.readBytes()
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        val children = DirChildrenParcelable.createFromParcel(parcel)
        parcel.recycle()
        return children
    }
}
