package com.xcc.mvi.improved.example

import androidx.lifecycle.viewModelScope
import com.xcc.mvi.improved.BaseStateModule
import com.xcc.mvi.improved.ModularMviViewModel
import com.xcc.mvi.improved.MviEvent
import com.xcc.mvi.improved.MviIntent
import com.xcc.mvi.improved.MviState
import com.xcc.mvi.improved.MviSubIntent
import com.xcc.mvi.improved.MviSubState
import com.xcc.mvi.improved.asPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 示例：用户资料模块
 */

// 用户数据子状态
data class UserDataState(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) : MviSubState

// 用户数据意图
sealed class UserDataIntent : MviSubIntent {
    data class LoadUser(val userId: String) : UserDataIntent()
    data class UpdateUsername(val username: String) : UserDataIntent()
    data class UpdateEmail(val email: String) : UserDataIntent()
}

// 用户数据模块
class UserDataModule(
    initialState: UserDataState,
    override val coroutineScope: CoroutineScope,
    private val userRepository: UserRepository
) : BaseStateModule<UserDataState, UserDataIntent>(initialState, coroutineScope) {
    
    override fun handleIntent(intent: UserDataIntent) {
        when (intent) {
            is UserDataIntent.LoadUser -> loadUser(intent.userId)
            is UserDataIntent.UpdateUsername -> updateUsername(intent.username)
            is UserDataIntent.UpdateEmail -> updateEmail(intent.email)
        }
    }
    
    private fun loadUser(userId: String) {
        coroutineScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val user = userRepository.getUser(userId)
                updateState { 
                    copy(
                        userId = user.id,
                        username = user.username,
                        email = user.email,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    private fun updateUsername(username: String) {
        updateState { copy(username = username) }
    }
    
    private fun updateEmail(email: String) {
        updateState { copy(email = email) }
    }
}

// 帖子子状态
data class PostsState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : MviSubState

// 帖子意图
sealed class PostsIntent : MviSubIntent {
    data class LoadPosts(val userId: String) : PostsIntent()
    data class AddPost(val content: String) : PostsIntent()
    data object RefreshPosts : PostsIntent()
}

// 帖子模块
class PostsModule(
    initialState: PostsState,
    override val coroutineScope: CoroutineScope,
    private val postsRepository: PostsRepository
) : BaseStateModule<PostsState, PostsIntent>(initialState, coroutineScope) {
    
    override fun handleIntent(intent: PostsIntent) {
        when (intent) {
            is PostsIntent.LoadPosts -> loadPosts(intent.userId)
            is PostsIntent.AddPost -> addPost(intent.content)
            is PostsIntent.RefreshPosts -> refreshPosts()
        }
    }
    
    private fun loadPosts(userId: String) {
        coroutineScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val posts = postsRepository.getUserPosts(userId)
                updateState { copy(posts = posts, isLoading = false) }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    private fun addPost(content: String) {
        // 实现添加帖子逻辑
    }
    
    private fun refreshPosts() {
        // 实现刷新帖子逻辑
    }
}

// 主状态
data class UserProfileState(
    val userData: UserDataState = UserDataState(),
    val postsState: PostsState = PostsState()
) : MviState

// 主意图
sealed class UserProfileIntent : MviIntent {
    data class UserDataIntent(val intent: com.xcc.mvi.improved.example.UserDataIntent) : UserProfileIntent()
    data class PostsIntent(val intent: com.xcc.mvi.improved.example.PostsIntent) : UserProfileIntent()
}

// 事件
sealed class UserProfileEvent : MviEvent {
    data class ShowError(val message: String) : UserProfileEvent()
    data class NavigateTo(val screen: String) : UserProfileEvent()
}

// 主ViewModel
class UserProfileViewModel : ModularMviViewModel<UserProfileState, UserProfileIntent, UserProfileEvent>(UserProfileState()) {

    private val userRepository: UserRepository = object : UserRepository {
        override suspend fun getUser(userId: String): User {
            return User(userId,"xcc", "136@gmail.com")
        }
    }
    private val postsRepository: PostsRepository = object : PostsRepository {
        override suspend fun getUserPosts(userId: String): List<Post> {
            return listOf(Post(userId, "xcc", "xcc"))
        }
    }

    // 初始化模块
    private val userDataModule: UserDataModule = UserDataModule(
        UserDataState(),
        viewModelScope,
        userRepository
    )
    private val postsModule: PostsModule = PostsModule(
        PostsState(),
        viewModelScope,
        postsRepository
    )

    // 状态选择器
    private val usernameSelector = select { it.userData.username }
    private val emailSelector = select { it.userData.email }
    private val postsSelector = select { it.postsState.posts }
    
    // 状态路径
    private val userLoadingPath = { state: UserProfileState -> state.userData.isLoading }.asPath()
    private val userErrorPath = { state: UserProfileState -> state.userData.error }.asPath()
    
    init {

        // 注册模块
        registerModule(userDataModule) { state, moduleState ->
            state.copy(userData = moduleState)
        }
        
        registerModule(postsModule) { state, moduleState ->
            state.copy(postsState = moduleState)
        }
        
        // 监听错误
        viewModelScope.launch {
            observe(userErrorPath).collect { error ->
                error?.let { sendEvent(UserProfileEvent.ShowError(it)) }
            }
        }
    }
    
    override fun handleIntent(intent: UserProfileIntent) {
        when (intent) {
            is UserProfileIntent.UserDataIntent -> userDataModule.handleIntent(intent.intent)
            is UserProfileIntent.PostsIntent -> postsModule.handleIntent(intent.intent)
        }
    }
    
    // 便捷方法
    fun loadUserProfile(userId: String) {
        userDataModule.handleIntent(UserDataIntent.LoadUser(userId))
        postsModule.handleIntent(PostsIntent.LoadPosts(userId))
    }
}

// 模拟数据类和仓库接口
data class User(val id: String, val username: String, val email: String)
data class Post(val id: String, val userId: String, val content: String)

interface UserRepository {
    suspend fun getUser(userId: String): User
}

interface PostsRepository {
    suspend fun getUserPosts(userId: String): List<Post>
} 