package com.xcc.album.ui.feature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xcc.album.ui.component.AlbumGrid
import com.xcc.album.ui.component.FolderDropdownMenuContainer
import com.xcc.album.ui.component.XAlbumBottomBar
import com.xcc.album.ui.component.XAlbumTopBar
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
