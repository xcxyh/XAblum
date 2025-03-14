package com.xcc.mvi.improved

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 状态模块接口，用于模块化状态管理
 */
interface StateModule<S : MviSubState, I : MviSubIntent> {
    /**
     * 模块状态流
     */
    val stateFlow: StateFlow<S>
    
    /**
     * 处理模块意图
     */
    fun handleIntent(intent: I)
}

/**
 * 状态模块基础实现
 */
abstract class BaseStateModule<S : MviSubState, I : MviSubIntent>(
    initialState: S,
    open val coroutineScope: CoroutineScope
) : StateModule<S, I> {
    
    private val _stateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<S> = _stateFlow.asStateFlow()
    
    /**
     * 当前状态
     */
    protected val currentState: S
        get() = _stateFlow.value
    
    /**
     * 更新状态
     */
    protected fun updateState(reducer: S.() -> S) {
        _stateFlow.update { it.reducer() }
    }
    
    /**
     * 创建状态选择器
     */
    protected fun <T> select(selector: (S) -> T): StateSelector<S, T> {
        return StateSelector(selector)
    }
} 