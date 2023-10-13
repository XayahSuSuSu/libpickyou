package com.xayah.libpickyou.util

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.util.concurrent.atomic.AtomicInteger

private fun Context.findActivityAndLifecycle(): Pair<ActivityResultRegistry, Lifecycle> {
    var context: Context = this
    while (true) {
        when (context) {
            is ComponentActivity -> return context.activityResultRegistry to context.lifecycle
            !is ContextWrapper -> error("Cannot launch FilePicker on a Context without ComponentActivity!")
            else -> context = context.baseContext
        }
    }
}

/**
 * Register for activity result even after onStart().
 * @see <a href="https://www.jianshu.com/p/ff5b6161c29c">Here</a>
 */
internal fun <I, O> Context.registerForActivityResultCompat(
    nextLocalRequestCode: AtomicInteger,
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
): ActivityResultLauncher<I> {
    val key = "activity_rq#${nextLocalRequestCode.getAndIncrement()}"
    var launcher: ActivityResultLauncher<I>? = null
    var observer: LifecycleEventObserver? = null
    val (activityResultRegistry, lifecycle) = findActivityAndLifecycle()
    observer = LifecycleEventObserver { _, event ->
        if (Lifecycle.Event.ON_DESTROY == event) {
            launcher?.unregister()
            if (observer != null)
                lifecycle.removeObserver(observer!!)
        }
    }
    lifecycle.addObserver(observer)
    launcher = activityResultRegistry.register(key, contract) {
        launcher?.unregister()
        lifecycle.removeObserver(observer)
        callback.onActivityResult(it)
    }
    return launcher
}
