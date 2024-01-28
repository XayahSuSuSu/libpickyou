package com.xayah.libpickyou.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import com.xayah.libpickyou.IRemoteRootService
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.pathString

internal class RemoteRootService(private val context: Context) {
    private var mService: IRemoteRootService? = null
    private var mConnection: ServiceConnection? = null
    private var retries = 0
    private val intent by lazy {
        Intent().apply {
            component = ComponentName(context.packageName, RemoteRootService::class.java.name)
        }
    }

    companion object {
        private fun getShellBuilder() = Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER or Shell.FLAG_REDIRECT_STDERR)
            .setInitializers(EnvInitializer::class.java)
            .setTimeout(3)

        fun initService() = Shell.setDefaultBuilder(getShellBuilder())
    }

    private class EnvInitializer : Shell.Initializer() {
        companion object {
            fun initShell(shell: Shell) {
                shell.newJob()
                    .add("nsenter -t 1 -m su") // Switch to global namespace
                    .add("set -o pipefail") // Ensure that the exit code of each command is correct.
                    .exec()
            }
        }

        override fun onInit(context: Context, shell: Shell): Boolean {
            initShell(shell)
            return true
        }
    }

    class RemoteRootService : RootService() {
        override fun onBind(intent: Intent): IBinder = RemoteRootServiceImpl()
    }

    private suspend fun bindService(): IRemoteRootService = run {
        delay(1000)
        suspendCoroutine { continuation ->
            if (mService == null) {
                retries++
                destroyService()
                mConnection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName, service: IBinder) {
                        mService = IRemoteRootService.Stub.asInterface(service)
                        if (continuation.context.isActive) continuation.resume(mService!!)
                    }

                    override fun onServiceDisconnected(name: ComponentName) {
                        mService = null
                        mConnection = null
                        val msg = "Service disconnected."
                        if (continuation.context.isActive) continuation.resumeWithException(RemoteException(msg))
                    }

                    override fun onBindingDied(name: ComponentName) {
                        mService = null
                        mConnection = null
                        val msg = "Binding died."
                        if (continuation.context.isActive) continuation.resumeWithException(RemoteException(msg))
                    }

                    override fun onNullBinding(name: ComponentName) {
                        mService = null
                        mConnection = null
                        val msg = "Null binding."
                        if (continuation.context.isActive) continuation.resumeWithException(RemoteException(msg))
                    }
                }
                RootService.bind(intent, mConnection!!)
            } else {
                retries = 0
                mService
            }
        }
    }

    /**
     * Destroy the service.
     */
    fun destroyService() {
        if (mConnection != null) {
            RootService.unbind(mConnection!!)
        }
        RootService.stopOrTask(intent)

        mConnection = null
        mService = null
    }

    private suspend fun getService(): IRemoteRootService {
        return runCatching {
            withContext(Dispatchers.Main) {
                if (mService == null) {
                    bindService()
                } else if (mService!!.asBinder().isBinderAlive.not()) {
                    mService = null
                    bindService()
                } else {
                    mService!!
                }
            }
        }.getOrElse {
            withContext(Dispatchers.Main) {
                mService = null
                bindService()
            }
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

    suspend fun mkdirs(src: String) = getService().mkdirs(src)
}
