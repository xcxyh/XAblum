package com.xcc.album.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.xcc.album.data.model.Folder
import com.xcc.album.ui.R
import com.xcc.album.ui.theme.XAlbumTheme

@Composable
fun FolderDropdownMenuContainer(
    isExpanded: Boolean,
    folders: List<Folder>,
    onFolderSelected: (Folder) -> Unit,
    onDismiss: () -> Unit
) {
    if (isExpanded) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 半透明背景遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(onClick = onDismiss)
            )

            // 下拉菜单
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp), // TopAppBar 的高度
                    color = XAlbumTheme.colors.dropdownMenuBackground,
                    shadowElevation = 8.dp
                ) {
                    FolderDropdownMenu(
                        folders = folders,
                        onFolderSelected = onFolderSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderDropdownMenu(
    folders: List<Folder>,
    onFolderSelected: (Folder) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(vertical = 8.dp)
    ) {
        items(folders) { folder ->
            FolderItem(
                folder = folder,
                onClick = { onFolderSelected(folder) }
            )
        }
    }
}

@Composable
private fun FolderItem(
    folder: Folder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 文件夹预览图
        if (folder.medias.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = XAlbumTheme.colors.mediaItemBackground,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Image(
                    painter = rememberAsyncImagePainter(folder.medias.first().uri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 文件夹信息
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = folder.folderName,
                color = XAlbumTheme.colors.dropdownMenuText,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.xalbum_media_count, folder.medias.size),
                color = XAlbumTheme.colors.dropdownMenuText.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 