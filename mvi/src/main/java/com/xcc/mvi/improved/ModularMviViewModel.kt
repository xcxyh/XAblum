package com.xcc.mvi.improved

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * 模块化MVI ViewModel
 * 支持多个状态模块的组合
 */
abstract class ModularMviViewModel<S : MviState, I : MviIntent, E : MviEvent>(
    initialState: S
) : ImprovedMviViewModel<S, I, E>(initialState) {
    
    /**
     * 注册的模块列表
     */
    private val modules = mutableListOf<Any>()
    
    /**
     * 注册状态模块
     */
    protected fun <MS : MviSubState, MI : MviSubIntent> registerModule(
        module: StateModule<MS, MI>,
        stateUpdater: (currentState: S, moduleState: MS) -> S
    ) {
        modules.add(module)
        
        // 监听模块状态变化并更新主状态
        viewModelScope.launch {
            module.stateFlow.collect { moduleState ->
                updateState { stateUpdater(this, moduleState) }
            }
        }
    }
    
    /**
     * 获取已注册的模块
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <M : StateModule<*, *>> getModule(moduleClass: Class<M>): M? {
        return modules.find { moduleClass.isInstance(it) } as? M
    }
    
    /**
     * 分发意图到相应的模块
     */
    protected fun <MI : MviSubIntent> dispatchToModule(
        intent: MI,
        module: StateModule<*, MI>
    ) {
        module.handleIntent(intent)
    }
    
    /**
     * 创建组合状态选择器
     */
    protected fun <MS : MviSubState, T> selectFromModule(
        moduleSelector: (S) -> MS,
        valueSelector: (MS) -> T
    ): StateSelector<S, T> {
        return select { moduleSelector(it) }.then(valueSelector)
    }
} 