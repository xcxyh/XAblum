package com.xcc.mvi.improved

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

/**
 * MVI View接口
 * 提供状态订阅和事件处理的扩展函数
 */
interface MviView {
    
    /**
     * 订阅状态的协程作用域
     */
    val subscriptionScope: CoroutineScope?
        get() = try {
            val lifecycleOwner = (this as? Fragment)?.viewLifecycleOwner ?: this as? LifecycleOwner
            lifecycleOwner?.lifecycleScope
        } catch (e: IllegalStateException) {
            null
        }
    
    /**
     * 订阅ViewModel的状态
     */
    fun <S : MviState, I : MviIntent, E : MviEvent> ImprovedMviViewModel<S, I, E>.observeState(
        action: suspend (S) -> Unit
    ) {
        val scope = subscriptionScope ?: return
        scope.launch {
            stateFlow.collectLatest(action)
        }
    }
    
    /**
     * 使用选择器订阅ViewModel的状态
     */
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        selector: StateSelector<S, T>,
        action: suspend (T) -> Unit
    ) {
        val scope = subscriptionScope ?: return
        scope.launch {
            selector.selectFromFlow(stateFlow).collectLatest(action)
        }
    }
    
    /**
     * 使用路径订阅ViewModel的状态
     */
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        path: StatePath<S, T>,
        action: suspend (T) -> Unit
    ) {
        observeState(path.getSelector(), action)
    }
    
    /**
     * 使用选择器函数订阅ViewModel的状态
     */
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        selector: (S) -> T,
        action: suspend (T) -> Unit
    ) {
        observeState(StateSelector(selector), action)
    }
    
    /**
     * 使用属性引用订阅ViewModel的状态
     */
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        prop: KProperty1<S, T>,
        action: suspend (T) -> Unit
    ) {
        observeState({ prop.get(it) }, action)
    }
    
    /**
     * 订阅ViewModel的事件
     */
    fun <S : MviState, I : MviIntent, E : MviEvent> ImprovedMviViewModel<S, I, E>.observeEvents(
        action: suspend (E) -> Unit
    ) {
        val scope = subscriptionScope ?: return
        scope.launch {
            eventFlow.collectLatest(action)
        }
    }
    
    /**
     * 订阅Flow
     */
    fun <T> Flow<T>.observe(action: suspend (T) -> Unit) {
        val scope = subscriptionScope ?: return
        scope.launch {
            this@observe.collectLatest(action)
        }
    }
} 