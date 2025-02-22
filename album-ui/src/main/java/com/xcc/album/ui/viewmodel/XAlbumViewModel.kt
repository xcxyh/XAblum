package com.xcc.album.ui.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.xcc.album.data.loader.MediaLoader
import com.xcc.album.data.model.Folder
import com.xcc.album.data.model.MediaData
import com.xcc.album.data.model.MediaType
import com.xcc.mvi.BaseMviViewModel
import com.xcc.mvi.MviEvent
import com.xcc.mvi.MviUiIntent
import com.xcc.mvi.MviUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class XAlbumState(
    val folders: List<Folder> = emptyList(),
    val currentFolder: Folder? = null,
    val selectedItems: Set<MediaData> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
): MviUiState

sealed class XAlbumEvent : MviEvent {
    data class ShowError(val message: String) : XAlbumEvent()
    data class ShowToast(val message: String) : XAlbumEvent()
}

sealed class XAlbumIntent: MviUiIntent {
    data class LoadMedia(val context: Context) : XAlbumIntent()
    data class SelectMedia(val media: MediaData) : XAlbumIntent()
    data class SwitchFolder(val folder: Folder) : XAlbumIntent()
}

class XAlbumViewModel: BaseMviViewModel<XAlbumState, XAlbumIntent, XAlbumEvent>(XAlbumState()) {

    override fun handleIntent(uiIntent: XAlbumIntent) {
        when (uiIntent) {
            is XAlbumIntent.LoadMedia -> load(uiIntent.context)
            is XAlbumIntent.SelectMedia -> selectMedia(uiIntent.media)
            is XAlbumIntent.SwitchFolder -> switchFolder(uiIntent.folder)
        }
    }

    private fun load(context: Context) {
        this.viewModelScope.launch(Dispatchers.IO) {
            setState { copy(isLoading = true) }
            try {
                MediaLoader.loadMediaFlow(context, MediaType.ImageAndVideo)
                    .collect { folders ->
                        setState {
                            copy(
                                folders = folders,
                                currentFolder = if (currentFolder == null) {
                                    folders.firstOrNull()
                                } else {
                                    folders.find { it.folderId == currentFolder.folderId }
                                },
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Throwable) {
                setState { copy(isLoading = false) }
                sendEvent(XAlbumEvent.ShowError(e.message ?: "加载媒体失败"))
            }
        }
    }


    private fun selectMedia(media: MediaData) {
        setState {
            copy(
                selectedItems = if (media in selectedItems) {
                    selectedItems - media
                } else {
                    selectedItems + media
                }
            )
        }
    }

    private fun switchFolder(folder: Folder) {
        setState { copy(currentFolder = folder) }
    }

}