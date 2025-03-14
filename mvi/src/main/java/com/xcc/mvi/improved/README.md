# 改进的MVI架构框架
这是一个基于ViewModel的改进版MVI架构框架，解决了原有框架中状态过于集中和状态层级过深的问题。

## 主要特性
1. **模块化状态管理**：
   - 将大型状态对象分解为多个小型状态对象
   - 使用状态模块独立管理各个子状态
   - 主ViewModel协调各个状态模块

2. **类型安全的状态访问**：
   - 提供状态选择器和状态路径，简化深层状态访问
   - 减少反射使用，提高性能和类型安全
   - 为常用深层状态提供直接访问方法

3. **改进的事件处理**：
   - 使用SharedFlow处理事件，支持事件优先级
   - 提供更灵活的事件订阅机制

4. **Compose支持**：
   - 提供Jetpack Compose集成
   - 支持使用选择器和路径收集状态

## 核心组件

### 基础接口

- `MviState`：表示UI状态的基础接口
- `MviSubState`：表示子状态的接口，用于模块化状态管理
- `MviIntent`：表示用户意图的基础接口
- `MviSubIntent`：表示子模块意图的接口
- `MviEvent`：表示一次性事件的基础接口

### 状态选择器和路径

- `StateSelector`：类型安全的状态选择器，用于选择状态中的特定部分
- `StatePath`：状态路径，用于构建类型安全的状态访问路径

### 状态模块

- `StateModule`：状态模块接口，用于模块化状态管理
- `BaseStateModule`：状态模块基础实现

### ViewModel

- `ImprovedMviViewModel`：改进的MVI ViewModel基类，支持更灵活的状态访问
- `ModularMviViewModel`：支持模块化状态管理的ViewModel

### 视图扩展

- `MviView`：MVI View接口，提供状态订阅和事件处理的扩展函数
- `MviComposeExtensions`：Jetpack Compose扩展，用于在Compose中使用MVI架构

## 优势
1. **解耦的状态管理**：
   - 每个模块独立管理自己的状态
   - 减少状态对象的复杂度
   - 提高代码的可维护性和可测试性

2. **简化的状态访问**：
   - 使用选择器和路径简化深层状态访问
   - 类型安全的状态访问方式
   - 减少样板代码

3. **灵活的事件处理**：
   - 支持事件优先级
   - 更可靠的事件传递机制

4. **更好的Compose集成**：
   - 提供更多的Compose扩展函数
   - 简化Compose中的状态收集

## 架构图
```
┌─────────────────────────────────────────────────────────────────┐
│                           View Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   Fragment      │  │  Compose UI     │  │  Activity       │  │
│  │  implements     │  │                 │  │  implements     │  │
│  │   MviView       │  │                 │  │   MviView       │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
└───────────┼─────────────────────┼─────────────────────┼─────────┘
            │                     │                     │
            │    observeState     │   collectAsState    │    observeState
            │    observeEvents    │                     │    observeEvents
            ▼                     ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                        ViewModel Layer                          │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              ModularMviViewModel<S,I,E>                 │    │
│  │                                                         │    │
│  │  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐    │    │
│  │  │ StateModule │   │ StateModule │   │ StateModule │    │    │
│  │  │    <S,MS1,I1>   │    <S,MS2,I2>   │    <S,MS3,I3>   │    │
│  │  └─────────────┘   └─────────────┘   └─────────────┘    │    │
│  │         │                │                 │            │    │
│  │         └────────────────┼─────────────────┘            │    │
│  │                          │                              │    │
│  │                          ▼                              │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │              Shared State (S)                   │    │    │
│  │  │  ┌───────────┐  ┌───────────┐  ┌───────────┐    │    │    │
│  │  │  │    MS1    │  │    MS2    │  │    MS3    │    │    │    │
│  │  │  └───────────┘  └───────────┘  └───────────┘    │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
            ▲                     ▲                     ▲
            │                     │                     │
            │     sendIntent      │     sendIntent      │     sendIntent
            │                     │                     │
┌───────────┼─────────────────────┼─────────────────────┼─────────┐
│           │                     │                     │         │
│  ┌────────┴────────┐  ┌─────────┴───────┐  ┌─────────┴───────┐  │
│  │  User Actions   │  │  User Actions   │  │  User Actions   │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
│                       User Interactions                         │
└─────────────────────────────────────────────────────────────────┘
```

## 类图
```
┌───────────────────┐     ┌───────────────────┐     ┌───────────────────┐
│    <<interface>>  │     │    <<interface>>  │     │    <<interface>>  │
│     MviState      │     │     MviIntent     │     │     MviEvent      │
└───────────────────┘     └───────────────────┘     └───────────────────┘
         ▲                         ▲                         
         │                         │                         
┌───────────────────┐     ┌───────────────────┐     ┌───────────────────┐
│    <<interface>>  │     │    <<interface>>  │     │  EventPriority    │
│    MviSubState    │     │    MviSubIntent   │     │    (enum)         │
└───────────────────┘     └───────────────────┘     └───────────────────┘
                                                              │
                                                              │
                                                    ┌───────────────────┐
                                                    │   EventWrapper<E> │
                                                    └───────────────────┘

┌───────────────────────────────────────┐
│           ViewModel                   │
│                                       │
└───────────────┬───────────────────────┘
                │
                │
┌───────────────▼───────────────────────┐
│     ImprovedMviViewModel<S,I,E>       │
│                                       │
│ - _stateFlow: MutableStateFlow<S>     │
│ - stateFlow: StateFlow<S>             │
│ - eventFlow: SharedFlow<E>            │
│ + sendIntent(intent: I)               │
│ # updateState(reducer: S.() -> S)     │
│ # sendEvent(event: E)                 │
│ # select(selector: (S) -> T)          │
│ # observe(selector: (S) -> T): Flow<T>│
└───────────────┬───────────────────────┘
                │
                │
┌───────────────▼───────────────────────┐
│     ModularMviViewModel<S,I,E>        │
│                                       │
│ - modules: List<Any>                  │
│ + registerModule(...)                 │
│ # dispatchToModule(...)               │
│ # selectFromModule(...)               │
└───────────────────────────────────────┘

┌───────────────────────────────────────┐
│    <<interface>>                      │
│    StateModule<S,MS,I>                │
│                                       │
│ + mainStateFlow: StateFlow<S>         │
│ + stateSelector: (S) -> MS            │
│ + subStateFlow: StateFlow<MS>         │
│ + handleIntent(intent: I)             │
└───────────────┬───────────────────────┘
                │
                │
┌───────────────▼───────────────────────┐
│    BaseStateModule<S,MS,I>            │
│                                       │
│ - mainStateFlow: MutableStateFlow<S>  │
│ - stateSelector: (S) -> MS            │
│ - stateUpdater: (S,MS) -> S           │
│ # currentSubState: MS                 │
│ # updateState(reducer: MS.() -> MS)   │
│ # select(selector: (MS) -> T)         │
└───────────────────────────────────────┘

┌───────────────────────────────────────┐     ┌───────────────────────────────────────┐
│    StateSelector<S,T>                 │     │    StatePath<S,T>                     │
│                                       │     │                                       │
│ + select: (S) -> T                    │     │ - selector: StateSelector<S,T>        │
│ + then(nextSelector: (T) -> R)        │     │ + getSelector(): StateSelector<S,T>   │
│ + selectFromFlow(flow: Flow<S>)       │     │ + then(nextSelector: (T) -> R)        │
└───────────────────────────────────────┘     └───────────────────────────────────────┘

┌───────────────────────────────────────┐
│    <<interface>>                      │
│    MviView                            │
│                                       │
│ + subscriptionScope: CoroutineScope?  │
│ + ImprovedMviViewModel.observeState() │
│ + ImprovedMviViewModel.observeEvents()│
└───────────────────────────────────────┘
```

## 状态流图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│  ┌─────────┐        ┌─────────┐        ┌─────────┐        ┌─────────┐   │
│  │ User    │        │ View    │        │ViewModel│        │ State   │   │
│  │ Action  │        │ Layer   │        │ Layer   │        │ Modules │   │
│  └────┬────┘        └────┬────┘        └────┬────┘        └────┬────┘   │
│       │                  │                  │                  │        │
│       │                  │                  │                  │        │
│       │  User Intent     │                  │                  │        │
│       │─────────────────>│                  │                  │        │
│       │                  │                  │                  │        │
│       │                  │  sendIntent()    │                  │        │
│       │                  │─────────────────>│                  │        │
│       │                  │                  │                  │        │
│       │                  │                  │  handleIntent()  │        │
│       │                  │                  │─────────────────>│        │
│       │                  │                  │                  │        │
│       │                  │                  │                  │        │
│       │                  │                  │                  │        │
│       │                  │                  │  updateState()   │        │
│       │                  │<─────────────────│        │
│       │                  │                  │                  │        │
│       │                  │  State Update    │                  │        │
│       │                  │<─────────────────│                  │        │
│       │                  │                  │                  │        │
│       │  UI Update       │                  │                  │        │
│       │<─────────────────│                  │                  │        │
│       │                  │                  │                  │        │
│       │                  │                  │  sendEvent()     │        │
│       │                  │                  │<─────────────────│        │
│       │                  │                  │                  │        │
│       │                  │  Event           │                  │        │
│       │                  │<─────────────────│                  │        │
│       │                  │                  │                  │        │
│       │  Show Event      │                  │                  │        │
│       │<─────────────────│                  │                  │        │
│       │                  │                  │                  │        │
└─────────────────────────────────────────────────────────────────────────┘ 
```

## 共享状态设计图

```
┌───────────────────────────────────────────────────────────────────────┐
│                                                                       │
│                       ModularMviViewModel                             │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐    │
│  │                                                               │    │
│  │                    MutableStateFlow<S>                        │    │
│  │                                                               │    │
│  │  ┌───────────────┬───────────────────┬───────────────────┐    │    │
│  │  │               │                   │                   │    │    │
│  │  │  UserInfo     │  Settings         │  Content          │    │    │
│  │  │               │                   │                   │    │    │
│  │  └───────┬───────┴─────────┬─────────┴─────────┬─────────┘    │    │
│  │          │                 │                   │              │    │
│  └──────────┼─────────────────┼───────────────────┼──────────────┘    │
│             │                 │                   │                   │
│  ┌──────────▼──────┐  ┌───────▼───────┐  ┌────────▼──────┐            │
│  │                 │  │               │  │                │            │
│  │  UserModule     │  │ SettingsModule│  │ ContentModule  │            │
│  │                 │  │               │  │                │            │
│  │ stateSelector   │  │ stateSelector │  │ stateSelector  │            │
│  │ stateUpdater    │  │ stateUpdater  │  │ stateUpdater   │            │
│  │                 │  │               │  │                │            │
│  └─────────────────┘  └───────────────┘  └────────────────┘            │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

## 组件详细说明

### 核心接口

#### MviState
表示UI状态的基础接口，所有状态类都应实现此接口。

```kotlin
interface MviState
```

#### MviIntent
表示用户意图的基础接口，所有意图类都应实现此接口。

```kotlin
interface MviIntent
```

#### MviSubIntent
表示子模块意图的接口，所有子模块意图类都应实现此接口。

```kotlin
interface MviSubIntent : MviIntent
```

#### MviEvent
表示一次性事件的基础接口，所有事件类都应实现此接口。

```kotlin
interface MviEvent
```

### 状态选择器和路径

#### StateSelector
类型安全的状态选择器，用于选择状态中的特定部分。

```kotlin
class StateSelector<S, T>(val select: (S) -> T) {
    fun <R> then(nextSelector: (T) -> R): StateSelector<S, R>
    fun selectFromFlow(flow: Flow<S>): Flow<T>
}
```

#### StatePath
状态路径，用于构建类型安全的状态访问路径。

```kotlin
class StatePath<S : MviState, T>(private val selector: StateSelector<S, T>) {
    fun getSelector(): StateSelector<S, T>
    fun <R> then(nextSelector: (T) -> R): StatePath<S, R>
}
```

### 状态模块

#### StateModule
状态模块接口，用于模块化状态管理。

```kotlin
interface StateModule<S : MviState, MS, I : MviSubIntent> {
    val mainStateFlow: StateFlow<S>
    val stateSelector: (S) -> MS
    val subStateFlow: StateFlow<MS>
    fun handleIntent(intent: I)
}
```

#### BaseStateModule
状态模块基础实现。

```kotlin
abstract class BaseStateModule<S : MviState, MS, I : MviSubIntent>(
    private val mainStateFlow: MutableStateFlow<S>,
    val stateSelector: (S) -> MS,
    private val stateUpdater: (S, MS) -> S,
    protected val coroutineScope: CoroutineScope
) : StateModule<S, MS, I> {
    protected val currentSubState: MS
    protected fun updateState(reducer: MS.() -> MS)
    protected fun <T> select(selector: (MS) -> T): StateSelector<S, T>
}
```

### ViewModel

#### ImprovedMviViewModel
改进的MVI ViewModel基类，支持更灵活的状态访问。

```kotlin
abstract class ImprovedMviViewModel<S : MviState, I : MviIntent, E : MviEvent>(
    initialState: S
) : ViewModel() {
    protected val _stateFlow: MutableStateFlow<S>
    val stateFlow: StateFlow<S>
    val currentState: S
    val eventFlow: SharedFlow<E>
    
    protected abstract fun handleIntent(intent: I)
    fun sendIntent(intent: I)
    protected fun updateState(reducer: S.() -> S)
    protected fun sendEvent(event: E, priority: EventPriority = EventPriority.NORMAL)
    protected fun <T> select(selector: (S) -> T): StateSelector<S, T>
    protected fun <T> observe(selector: StateSelector<S, T>): Flow<T>
    protected fun <T> observe(path: StatePath<S, T>): Flow<T>
    protected fun <T> observe(selector: (S) -> T): Flow<T>
}
```

#### ModularMviViewModel
支持模块化状态管理的ViewModel。

```kotlin
abstract class ModularMviViewModel<S : MviState, I : MviIntent, E : MviEvent>(
    initialState: S
) : ImprovedMviViewModel<S, I, E>(initialState) {
    protected fun <MS, MI : MviSubIntent> registerModule(
        stateSelector: (S) -> MS,
        stateUpdater: (S, MS) -> S,
        moduleFactory: (MutableStateFlow<S>, (S) -> MS, (S, MS) -> S, CoroutineScope) -> StateModule<S, MS, MI>
    ): StateModule<S, MS, MI>
    
    protected fun <M : StateModule<*, *, *>> getModule(moduleClass: Class<M>): M?
    protected fun <MI : MviSubIntent> dispatchToModule(intent: MI, module: StateModule<*, *, MI>)
    protected fun <MS, T> selectFromModule(moduleSelector: (S) -> MS, valueSelector: (MS) -> T): StateSelector<S, T>
}
```

### 视图扩展

#### MviView
MVI View接口，提供状态订阅和事件处理的扩展函数。

```kotlin
interface MviView {
    val subscriptionScope: CoroutineScope?
    
    fun <S : MviState, I : MviIntent, E : MviEvent> ImprovedMviViewModel<S, I, E>.observeState(
        action: suspend (S) -> Unit
    )
    
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        selector: StateSelector<S, T>,
        action: suspend (T) -> Unit
    )
    
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        path: StatePath<S, T>,
        action: suspend (T) -> Unit
    )
    
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        selector: (S) -> T,
        action: suspend (T) -> Unit
    )
    
    fun <S : MviState, I : MviIntent, E : MviEvent, T> ImprovedMviViewModel<S, I, E>.observeState(
        prop: KProperty1<S, T>,
        action: suspend (T) -> Unit
    )
    
    fun <S : MviState, I : MviIntent, E : MviEvent> ImprovedMviViewModel<S, I, E>.observeEvents(
        action: suspend (E) -> Unit
    )
    
    fun <T> Flow<T>.observe(action: suspend (T) -> Unit)
}
```

## 使用方法

### 1. 定义状态、意图和事件

```kotlin
// 子状态
data class UserDataState(
    val username: String = "",
    val email: String = "",
    val isLoading: Boolean = false
) : MviSubState

// 子意图
sealed class UserDataIntent : MviSubIntent {
    data class UpdateUsername(val username: String) : UserDataIntent()
    data class UpdateEmail(val email: String) : UserDataIntent()
}

// 主状态
data class MainState(
    val userData: UserDataState = UserDataState(),
    val otherState: OtherState = OtherState()
) : MviState

// 主意图
sealed class MainIntent : MviIntent {
    data class UserDataIntent(val intent: UserDataIntent) : MainIntent()
    data class OtherIntent(val intent: OtherIntent) : MainIntent()
}

// 事件
sealed class MainEvent : MviEvent {
    data class ShowError(val message: String) : MainEvent()
}
```

### 2. 创建状态模块

```kotlin
class UserDataModule(
    initialState: UserDataState,
    coroutineScope: CoroutineScope
) : BaseStateModule<UserDataState, UserDataIntent>(initialState, coroutineScope) {
    
    override fun handleIntent(intent: UserDataIntent) {
        when (intent) {
            is UserDataIntent.UpdateUsername -> updateUsername(intent.username)
            is UserDataIntent.UpdateEmail -> updateEmail(intent.email)
        }
    }
    
    private fun updateUsername(username: String) {
        updateState { copy(username = username) }
    }
    
    private fun updateEmail(email: String) {
        updateState { copy(email = email) }
    }
}
```

### 3. 创建ViewModel

```kotlin
class MainViewModel : ModularMviViewModel<MainState, MainIntent, MainEvent>(MainState()) {
    
    private val userDataModule = UserDataModule(UserDataState(), viewModelScope)
    private val otherModule = OtherModule(OtherState(), viewModelScope)
    
    init {
        // 注册模块
        registerModule(userDataModule) { state, moduleState ->
            state.copy(userData = moduleState)
        }
        
        registerModule(otherModule) { state, moduleState ->
            state.copy(otherState = moduleState)
        }
    }
    
    override fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UserDataIntent -> userDataModule.handleIntent(intent.intent)
            is MainIntent.OtherIntent -> otherModule.handleIntent(intent.intent)
        }
    }
}
```

### 4. 在View中使用

```kotlin
class MainFragment : Fragment(), MviView {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 使用状态选择器观察特定状态
        viewModel.observeState({ it.userData.username }) { username ->
            usernameTextView.text = username
        }
        
        // 使用状态路径观察深层状态
        val loadingPath = { state: MainState -> state.userData.isLoading }.asPath()
        viewModel.observeState(loadingPath) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // 观察事件
        viewModel.observeEvents { event ->
            when (event) {
                is MainEvent.ShowError -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // 发送意图
        updateButton.setOnClickListener {
            viewModel.sendIntent(
                MainIntent.UserDataIntent(
                    UserDataIntent.UpdateUsername("新用户名")
                )
            )
        }
    }
}
```

### 5. 在Compose中使用

```kotlin
@Composable
fun MainScreen(viewModel: MainViewModel) {
    // 收集状态
    val username by viewModel.collectAsState { it.userData.username }
    val isLoading by viewModel.collectAsState { it.userData.isLoading }
    
    // 处理事件
    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MainEvent.ShowError -> {
                    // 显示错误
                }
            }
        }
    }
    
    // UI
    Column {
        Text("用户名: $username")
        
        Button(
            onClick = {
                viewModel.sendIntent(
                    MainIntent.UserDataIntent(
                        UserDataIntent.UpdateUsername("新用户名")
                    )
                )
            }
        ) {
            Text("更新用户名")
        }
        
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}
```