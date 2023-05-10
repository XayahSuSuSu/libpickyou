package com.xayah.libpickyou.ui.animation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.xayah.libpickyou.ui.tokens.AnimationToken
import java.util.concurrent.CountDownLatch

@ExperimentalAnimationApi
@Composable
internal fun <T> CrossFade(
    targetState: MutableState<T>,
    latch: MutableState<CountDownLatch>,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(AnimationToken.CrossFadeDefaultDurationMillis),
    label: String = AnimationToken.CrossFadeLabel,
    content: @Composable (T) -> Unit
) {
    val transition = updateTransition(targetState.value, label)
    transition.Crossfade(modifier, animationSpec, content = {
        if (targetState.value == transition.currentState) {
            latch.value.countDown()
        }
        content(it)
    })
}
