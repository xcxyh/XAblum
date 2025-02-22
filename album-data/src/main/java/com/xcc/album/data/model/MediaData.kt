package com.xcc.album.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaData(
    val mediaId: Long,
    val bucketId: String,
    val bucketName: String,
    val name: String,
    val modifiedTime: Long, // in seconds
    var mimeType: String,
    val uri: Uri,
    var path: String,
    var fileSize: Long,
    var durationMs: Int,
    var width: Int,
    var height: Int
): Parcelable {
    val isImage: Boolean
        get() = mimeType.startsWith(prefix = ImageMimeTypePrefix)

    val isVideo: Boolean
        get() = mimeType.startsWith(prefix = VideoMimeTypePrefix)
}