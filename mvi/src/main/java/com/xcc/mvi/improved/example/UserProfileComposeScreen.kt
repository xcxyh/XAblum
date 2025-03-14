package com.xcc.mvi.improved.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xcc.mvi.improved.collectAsState

/**
 * 示例Compose屏幕，展示如何在Compose中使用改进的MVI框架
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel) {
    // 收集状态
    val username by viewModel.collectAsState { it.userData.username }
    val email by viewModel.collectAsState { it.userData.email }
    val isLoading by viewModel.collectAsState { it.userData.isLoading }
    val posts by viewModel.collectAsState { it.postsState.posts }
    val userError by viewModel.collectAsState { it.userData.error }
    
    // 本地状态
    var newUsername by remember { mutableStateOf("") }
    
    // 处理事件
    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UserProfileEvent.ShowError -> {
                    // 显示Snackbar或Toast
                }
                is UserProfileEvent.NavigateTo -> {
                    // 处理导航
                }
            }
        }
    }
    
    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户资料") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 用户信息卡片
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "用户名: $username",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "邮箱: $email",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // 更新用户名
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("新用户名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.sendIntent(
                                UserProfileIntent.UserDataIntent(
                                    UserDataIntent.UpdateUsername("newname")
                                )
                            )
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("更新用户名")
                    }
                }
                
                // 帖子列表标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "帖子列表",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(
                        onClick = {
                            viewModel.sendIntent(
                                UserProfileIntent.PostsIntent(PostsIntent.RefreshPosts)
                            )
                        }
                    ) {
                        Text("刷新")
                    }
                }
                
                // 帖子列表
                LazyColumn {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = post.content,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // 加载指示器
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // 错误信息
            userError?.let {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(it)
                }
            }
        }
    }
}

// 帖子项组件
@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 