package com.xcc.album.ui.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xcc.album.data.model.MediaData
import com.xcc.album.ui.theme.XAlbumTheme
import com.xcc.album.ui.viewmodel.XAlbumIntent
import com.xcc.album.ui.viewmodel.XAlbumViewModel
import com.xcc.mvi.collectAsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun XAlbumScreen(
    onBackClick: () -> Unit = {},
    onPreviewClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
) {
    val viewModel = koinViewModel<XAlbumViewModel>()
    val state by viewModel.collectAsState()

    XAlbumTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = XAlbumTheme.colors.mainPageBackground,
            topBar = {
                XAlbumTopBar(
                    title = state.currentFolder?.folderName ?: "",
                    isDropdownExpanded = state.isDropdownExpanded,
                    onBackClick = onBackClick,
                    onTitleClick = { viewModel.setIntent(XAlbumIntent.ToggleDropdown(!state.isDropdownExpanded)) }
                )
            },
            bottomBar = {
                XAlbumBottomBar(
                    selectedCount = state.selectedItems.size,
                    onPreviewClick = onPreviewClick,
                    onConfirmClick = onConfirmClick
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = XAlbumTheme.colors.circularLoading
                    )
                } else {
                    AlbumGrid(
                        medias = state.currentFolder?.medias ?: emptyList(),
                        selectedItems = state.selectedItems.map { it.mediaId }.toSet(),
                        onItemClick = { media -> 
                            viewModel.setIntent(XAlbumIntent.SelectMedia(media))
                        }
                    )
                }
            }
        }

        // 下拉菜单
        FolderDropdownMenuContainer(
            isExpanded = state.isDropdownExpanded,
            folders = state.folders,
            onFolderSelected = { folder ->
                viewModel.setIntent(XAlbumIntent.SwitchFolder(folder))
                viewModel.setIntent(XAlbumIntent.ToggleDropdown(false))
            },
            onDismiss = { viewModel.setIntent(XAlbumIntent.ToggleDropdown(false)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun XAlbumTopBar(
    title: String,
    isDropdownExpanded: Boolean,
    onBackClick: () -> Unit,
    onTitleClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = XAlbumTheme.colors.topBarBackground,
            titleContentColor = XAlbumTheme.colors.topBarText,
            navigationIconContentColor = XAlbumTheme.colors.topBarIcon,
            actionIconContentColor = XAlbumTheme.colors.topBarIcon
        ),
        title = {
            TextButton(
                onClick = onTitleClick,
            ) {
                Text(
                    text = title,
                    color = XAlbumTheme.colors.topBarText
                )
                Icon(
                    imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "展开相册列表",
                    tint = XAlbumTheme.colors.topBarIcon
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
        }
    )
}

@Composable
private fun XAlbumBottomBar(
    selectedCount: Int,
    onPreviewClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextButton(
            onClick = onPreviewClick,
            enabled = selectedCount > 0,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = "预览",
                color = if (selectedCount > 0) 
                    XAlbumTheme.colors.previewText 
                else XAlbumTheme.colors.previewTextDisabled
            )
        }

        TextButton(
            onClick = onConfirmClick,
            enabled = selectedCount > 0,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = if (selectedCount > 0) "确定($selectedCount)" else "确定",
                color = if (selectedCount > 0) 
                    XAlbumTheme.colors.confirmText 
                else XAlbumTheme.colors.confirmTextDisabled,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AlbumGrid(
    medias: List<MediaData>,
    selectedItems: Set<Long>,
    onItemClick: (MediaData) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(medias) { item ->
            AlbumGridItem(
                item = item,
                isSelected = item.mediaId in selectedItems,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun AlbumGridItem(
    item: MediaData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // 保持正方形比例
            .clickable(onClick = onClick)
    ) {
        // 图片
        coil3.compose.AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = item.uri,
            contentScale = ContentScale.Crop,
            contentDescription = item.name
        )

        // 选择指示器
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp)
                .align(Alignment.TopEnd)
        ) {
            // 未选中状态的圆圈背景
            Surface(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                color = XAlbumTheme.colors.checkBoxCircle.copy(alpha = 0.5f)
            ) {}

            // 选择图标
            Icon(
                imageVector = if (isSelected) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = if (isSelected) "已选择" else "未选择",
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) {
                    XAlbumTheme.colors.checkBoxCircleFill
                } else {
                    XAlbumTheme.colors.checkBoxCircle
                }
            )
        }

        // 如果是视频，显示视频时长
        if (item.isVideo) {
            Text(
                text = formatDuration(item.durationMs.toLong()),
                color = XAlbumTheme.colors.videoIcon,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        color = XAlbumTheme.colors.mediaItemBackground,
                        shape = CircleShape
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

// 辅助函数：格式化视频时长
private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
