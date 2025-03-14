package com.xcc.mvi.improved

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * 状态选择器，用于类型安全地选择状态中的特定部分
 */
class StateSelector<S, T>(val select: (S) -> T) {
    /**
     * 组合两个选择器，创建一个新的选择器
     */
    fun <R> then(nextSelector: (T) -> R): StateSelector<S, R> {
        return StateSelector { nextSelector(select(it)) }
    }
    
    /**
     * 从Flow<S>中选择特定部分并返回Flow<T>
     */
    fun selectFromFlow(flow: Flow<S>): Flow<T> {
        return flow.map(select).distinctUntilChanged()
    }
}

/**
 * 状态路径，用于构建类型安全的状态访问路径
 */
class StatePath<S : MviState, T>(private val selector: StateSelector<S, T>) {
    /**
     * 获取选择器
     */
    fun getSelector(): StateSelector<S, T> = selector
    
    /**
     * 扩展路径
     */
    fun <R> then(nextSelector: (T) -> R): StatePath<S, R> {
        return StatePath(selector.then(nextSelector))
    }
}

/**
 * 创建状态选择器的扩展函数
 */
fun <S, T> ((S) -> T).asSelector(): StateSelector<S, T> {
    return StateSelector(this)
}

/**
 * 创建状态路径的扩展函数
 */
fun <S : MviState, T> ((S) -> T).asPath(): StatePath<S, T> {
    return StatePath(this.asSelector())
} 