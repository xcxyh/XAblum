package com.xcc.mvi.improved

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.reflect.KProperty1

/**
 * 将ViewModel的状态流转换为Compose State
 */
@Composable
fun <VM : ImprovedMviViewModel<S, I, E>, S : MviState, I : MviIntent, E : MviEvent> VM.collectAsState(): State<S> {
    return stateFlow.collectAsState(initial = currentState)
}

/**
 * 使用选择器将ViewModel的状态流转换为Compose State
 */
@Composable
fun <VM : ImprovedMviViewModel<S, I, E>, S : MviState, I : MviIntent, E : MviEvent, T> VM.collectAsState(
    selector: StateSelector<S, T>
): State<T> {
    val initialValue = selector.select(currentState)
    val flow = remember(selector) { selector.selectFromFlow(stateFlow) }
    return flow.collectAsState(initial = initialValue)
}

/**
 * 使用路径将ViewModel的状态流转换为Compose State
 */
@Composable
fun <VM : ImprovedMviViewModel<S, I, E>, S : MviState, I : MviIntent, E : MviEvent, T> VM.collectAsState(
    path: StatePath<S, T>
): State<T> {
    return collectAsState(path.getSelector())
}

/**
 * 使用选择器函数将ViewModel的状态流转换为Compose State
 */
@Composable
fun <VM : ImprovedMviViewModel<S, I, E>, S : MviState, I : MviIntent, E : MviEvent, T> VM.collectAsState(
    selector: (S) -> T
): State<T> {
    return collectAsState(StateSelector(selector))
}

/**
 * 使用属性引用将ViewModel的状态流转换为Compose State
 */
@Composable
fun <VM : ImprovedMviViewModel<S, I, E>, S : MviState, I : MviIntent, E : MviEvent, T> VM.collectAsState(
    prop: KProperty1<S, T>
): State<T> {
    return collectAsState { prop.get(it) }
}

/**
 * 将Flow转换为Compose State
 */
@Composable
fun <T> Flow<T>.collectAsState(initial: T): State<T> {
    return distinctUntilChanged().collectAsState(initial = initial)
} 