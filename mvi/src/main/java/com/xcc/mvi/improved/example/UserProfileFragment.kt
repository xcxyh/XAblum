package com.xcc.mvi.improved.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.xcc.mvi.improved.MviView
import com.xcc.mvi.improved.asPath

/**
 * 示例Fragment，展示如何在View中使用改进的MVI框架
 */
class UserProfileFragment : Fragment(), MviView {
    
    // 使用依赖注入获取ViewModel
    private val viewModel: UserProfileViewModel by viewModels()
    
    // UI组件
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var refreshButton: Button
    private lateinit var updateUsernameButton: Button
    private lateinit var usernameEditText: EditText
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 实际项目中应该使用视图绑定或数据绑定
        return inflater.inflate(/* R.layout.fragment_user_profile */ 0, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化视图
        // usernameTextView = view.findViewById(R.id.username_text_view)
        // emailTextView = view.findViewById(R.id.email_text_view)
        // postsRecyclerView = view.findViewById(R.id.posts_recycler_view)
        // loadingProgressBar = view.findViewById(R.id.loading_progress_bar)
        // refreshButton = view.findViewById(R.id.refresh_button)
        // updateUsernameButton = view.findViewById(R.id.update_username_button)
        // usernameEditText = view.findViewById(R.id.username_edit_text)
        
        // 设置点击监听器
        refreshButton.setOnClickListener {
            viewModel.sendIntent(UserProfileIntent.PostsIntent(PostsIntent.RefreshPosts))
        }
        
        updateUsernameButton.setOnClickListener {
            val newUsername = usernameEditText.text.toString()
            viewModel.sendIntent(
                UserProfileIntent.UserDataIntent(
                    UserDataIntent.UpdateUsername(newUsername)
                )
            )
        }
        
        // 订阅状态
        setupStateObservation()
        
        // 加载用户资料
        viewModel.loadUserProfile("user123")
    }
    
    private fun setupStateObservation() {
        // 使用状态选择器观察特定状态
        viewModel.observeState({ it.userData.username }) { username ->
            usernameTextView.text = username
        }
        
        viewModel.observeState({ it.userData.email }) { email ->
            emailTextView.text = email
        }
        
        // 使用状态路径观察深层状态
        val loadingPath = { state: UserProfileState -> state.userData.isLoading }.asPath()
        viewModel.observeState(loadingPath) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // 观察帖子列表
        viewModel.observeState({ it.postsState.posts }) { posts ->
            // 更新RecyclerView适配器
            // postsAdapter.submitList(posts)
        }
        
        // 观察事件
        viewModel.observeEvents { event ->
            when (event) {
                is UserProfileEvent.ShowError -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
                is UserProfileEvent.NavigateTo -> {
                    // 处理导航事件
                }
            }
        }
    }
} 