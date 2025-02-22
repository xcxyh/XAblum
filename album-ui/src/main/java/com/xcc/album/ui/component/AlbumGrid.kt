package com.xcc.album.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xcc.album.data.model.MediaData
import com.xcc.album.ui.R
import com.xcc.album.ui.theme.XAlbumTheme

@Composable
fun AlbumGrid(
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
                contentDescription = if (isSelected) 
                    stringResource(R.string.xalbum_selected)
                else stringResource(R.string.xalbum_unselected),
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