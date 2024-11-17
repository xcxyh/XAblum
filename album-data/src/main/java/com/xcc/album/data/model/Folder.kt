package com.xcc.album.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder(
    val folderId: String,
    val folderName: String,
    val updatedTime: Long,
    val medias: List<MediaData> = emptyList()
): Parcelable