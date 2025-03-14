package com.xcc.mvi.improved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 改进的MVI ViewModel基类
 * 支持模块化状态管理和更灵活的状态访问
 */
abstract class ImprovedMviViewModel<S : MviState, I : MviIntent, E : MviEvent>(
    initialState: S
) : ViewModel() {
    
    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow: StateFlow<S> = _stateFlow.asStateFlow()
    
    private val _intentFlow = MutableSharedFlow<I>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    
    private val _eventFlow = MutableSharedFlow<EventWrapper<E>>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventFlow: SharedFlow<E> = _eventFlow
        .map { it.event }
        .shareIn(
            viewModelScope,
            SharingStarted.Eagerly,
            0
        )
    
    /**
     * 当前状态
     */
    val currentState: S
        get() = _stateFlow.value
    
    init {
        viewModelScope.launch {
            _intentFlow.collect {
                handleIntent(it)
            }
        }
    }
    
    /**
     * 处理用户意图
     */
    protected abstract fun handleIntent(intent: I)
    
    /**
     * 发送用户意图
     */
    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intentFlow.emit(intent)
        }
    }
    
    /**
     * 更新状态
     */
    protected fun updateState(reducer: S.() -> S) {
        _stateFlow.update { it.reducer() }
    }
    
    /**
     * 发送事件
     */
    protected fun sendEvent(event: E, priority: EventPriority = EventPriority.NORMAL) {
        viewModelScope.launch {
            _eventFlow.emit(EventWrapper(event, priority))
        }
    }
    
    /**
     * 创建状态选择器
     */
    protected fun <T> select(selector: (S) -> T): StateSelector<S, T> {
        return StateSelector(selector)
    }
    
    /**
     * 使用选择器观察状态
     */
    protected fun <T> observe(selector: StateSelector<S, T>): Flow<T> {
        return selector.selectFromFlow(stateFlow)
    }
    
    /**
     * 使用路径观察状态
     */
    protected fun <T> observe(path: StatePath<S, T>): Flow<T> {
        return observe(path.getSelector())
    }
    
    /**
     * 使用选择器函数观察状态
     */
    protected fun <T> observe(selector: (S) -> T): Flow<T> {
        return observe(select(selector))
    }
} 