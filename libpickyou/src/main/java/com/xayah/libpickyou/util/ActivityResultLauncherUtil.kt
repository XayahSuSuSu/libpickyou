package com.xayah.libpickyou.util

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.util.concurrent.atomic.AtomicInteger

/**
 * Register for activity result even after onStart().
 * @see <a href="https://www.jianshu.com/p/ff5b6161c29c">Here</a>
 */
internal fun <I, O> ComponentActivity.registerForActivityResultCompat(
    nextLocalRequestCode: AtomicInteger,
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
): ActivityResultLauncher<I> {
    val key = "activity_rq#${nextLocalRequestCode.getAndIncrement()}"
    var launcher: ActivityResultLauncher<I>? = null
    var observer: LifecycleEventObserver? = null
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

/**
 * Register for activity result even after onStart().
 * @see <a href="https://www.jianshu.com/p/ff5b6161c29c">Here</a>
 */
internal fun <I, O> Fragment.registerForActivityResultCompat(
    nextLocalRequestCode: AtomicInteger,
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
): ActivityResultLauncher<I> {
    val key = "activity_rq#${nextLocalRequestCode.getAndIncrement()}"
    var launcher: ActivityResultLauncher<I>? = null
    var observer: LifecycleEventObserver? = null
    observer = LifecycleEventObserver { _, event ->
        if (Lifecycle.Event.ON_DESTROY == event) {
            launcher?.unregister()
            if (observer != null)
                lifecycle.removeObserver(observer!!)
        }
    }
    lifecycle.addObserver(observer)
    launcher = requireActivity().activityResultRegistry.register(key, contract) {
        launcher?.unregister()
        lifecycle.removeObserver(observer)
        callback.onActivityResult(it)
    }
    return launcher
}
