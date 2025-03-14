package com.xcc.mvi.improved

/**
 * 表示UI状态的基础接口
 */
interface MviState

/**
 * 表示子状态的接口，用于模块化状态管理
 */
interface MviSubState : MviState

/**
 * 表示用户意图的基础接口
 */
interface MviIntent

/**
 * 表示子模块意图的接口
 */
interface MviSubIntent : MviIntent

/**
 * 表示一次性事件的基础接口
 */
interface MviEvent

/**
 * 事件优先级枚举
 */
enum class EventPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * 事件包装类，添加优先级信息
 */
data class EventWrapper<E : MviEvent>(
    val event: E,
    val priority: EventPriority = EventPriority.NORMAL,
    val timestamp: Long = System.currentTimeMillis()
) 