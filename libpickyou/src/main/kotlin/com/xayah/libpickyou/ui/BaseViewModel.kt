package com.xayah.libpickyou.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface UiState
interface UiIntent
interface UiEffect

interface IBaseViewModel<S : UiState, I : UiIntent, E : UiEffect> {
    suspend fun onEvent(state: S, intent: I)
    suspend fun onEffect(effect: E)
}

abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(state: S) : IBaseViewModel<S, I, E>, ViewModel() {
    private val _uiState = MutableStateFlow(state)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    override suspend fun onEvent(state: S, intent: I) {}

    override suspend fun onEffect(effect: E) {}

    suspend fun emitState(state: S) = withMainContext { _uiState.emit(state) }

    fun emitStateOnMain(state: S) = launchOnMain { emitState(state) }

    suspend fun emitIntent(intent: I) = onEvent(state = uiState.value, intent = intent)

    fun emitIntentOnIO(intent: I) = launchOnIO { emitIntent(intent) }

    suspend fun emitEffect(effect: E) = onEffect(effect = effect)

    fun emitEffectOnIO(effect: E) = launchOnIO { emitEffect(effect) }

    suspend fun withIOContext(block: suspend CoroutineScope.() -> Unit) = withContext(Dispatchers.IO, block = block)

    suspend fun withMainContext(block: suspend CoroutineScope.() -> Unit) = withContext(Dispatchers.Main, block = block)

    fun launchOnIO(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(context = Dispatchers.IO, block = block)

    fun launchOnMain(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(context = Dispatchers.Main, block = block)

    @DelicateCoroutinesApi
    fun launchOnGlobal(block: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(block = block)

    fun <T> Flow<T>.stateInScope(initialValue: T): StateFlow<T> = stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = initialValue
    )

    fun <T> Flow<T>.flowOnIO(): Flow<T> = flowOn(Dispatchers.IO)
}
