package com.xcc.album.ui.feature

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.xcc.album.ui.theme.XAlbumTheme

data class AlbumItem(
    val id: Long,
    val imageUrl: String,
    val isVideo: Boolean = false,
    val duration: Long = 0L // 视频时长，单位毫秒
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XAlbumScreen(
    onBackClick: () -> Unit = {},
    onPreviewClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
) {
    var selectedCount by remember { mutableStateOf(0) }
    var currentAlbumTitle by remember { mutableStateOf("所有照片") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<Long>()) }

    // 模拟数据，实际应该从 ViewModel 获取
    val items = remember {
        List(20) { index ->
            AlbumItem(
                id = index.toLong(),
                imageUrl = "https://picsum.photos/200/200?random=$index"
            )
        }
    }

    XAlbumTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = XAlbumTheme.colors.mainPageBackground,
            topBar = {
                XAlbumTopBar(
                    title = currentAlbumTitle,
                    isDropdownExpanded = isDropdownExpanded,
                    onBackClick = onBackClick,
                    onTitleClick = { isDropdownExpanded = !isDropdownExpanded }
                )
            },
            bottomBar = {
                XAlbumBottomBar(
                    selectedCount = selectedCount,
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
                AlbumGrid(
                    items = items,
                    selectedItems = selectedItems,
                    onItemClick = { item ->
                        selectedItems = if (item.id in selectedItems) {
                            selectedItems - item.id
                        } else {
                            selectedItems + item.id
                        }
                        selectedCount = selectedItems.size
                    }
                )
            }
        }
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
                    imageVector = Icons.Default.KeyboardArrowDown,
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
    items: List<AlbumItem>,
    selectedItems: Set<Long>,
    onItemClick: (AlbumItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(items) { item ->
            AlbumGridItem(
                item = item,
                isSelected = item.id in selectedItems,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun AlbumGridItem(
    item: AlbumItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // 保持正方形比例
            .clickable(onClick = onClick)
    ) {
        // 图片
        Image(
            painter = rememberAsyncImagePainter(item.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(XAlbumTheme.colors.mediaItemBackground),
            contentScale = ContentScale.Crop
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
                text = formatDuration(item.duration),
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
