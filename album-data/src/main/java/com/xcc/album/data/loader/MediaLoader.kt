package com.xcc.album.data.loader

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.xcc.album.data.model.Folder
import com.xcc.album.data.model.MediaData
import com.xcc.album.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object MediaLoader {

    private const val TAG: String = "MediaLoader"

    private const val NEW_PICTURE_THRESHOLD = 10
    private const val INITIAL_BATCH_SIZE = 200
    private const val MAX_BATCH_SIZE = 800

    private const val IMAGES_AND_VIDEOS_FOLDER_KEY = "images_and_videos_folder_key"
    private const val IMAGES_FOLDER_KEY = "images_folder_key"
    private const val VIDEOS_FOLDER_KEY = "videos_folder_key"

    private const val IMAGES_AND_VIDEOS_FOLDER_NAME = "图片和视频"
    private const val IMAGES_FOLDER_NAME = "图片"
    private const val VIDEOS_FOLDER_NAME = "视频"

    private val CONTENT_URI: Uri = MediaStore.Files.getContentUri("external")

    private const val ID_COLUMN = MediaStore.Files.FileColumns._ID
    private const val DATA_COLUMN = MediaStore.MediaColumns.DATA
    private const val MEDIA_TYPE_COLUMN = MediaStore.Files.FileColumns.MEDIA_TYPE
    private const val DATE_MODIFIED_COLUMN = MediaStore.MediaColumns.DATE_MODIFIED
    private const val MIME_TYPE_COLUMN = MediaStore.MediaColumns.MIME_TYPE
    private const val DURATION_COLUMN = MediaStore.Video.Media.DURATION
    private const val SIZE_COLUMN = MediaStore.MediaColumns.SIZE
    private const val WIDTH_COLUMN = MediaStore.MediaColumns.WIDTH
    private const val HEIGHT_COLUMN = MediaStore.MediaColumns.HEIGHT
    private const val DISPLAY_NAME_COLUMN = MediaStore.MediaColumns.DISPLAY_NAME
    private const val BUCKET_ID_COLUMN = MediaStore.MediaColumns.BUCKET_ID
    private const val BUCKET_DISPLAY_NAME_COLUMN = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME


    private val PROJECTIONS: Array<String> = arrayOf(
        ID_COLUMN,
        DATA_COLUMN,
        MEDIA_TYPE_COLUMN,
        DATE_MODIFIED_COLUMN,
        DISPLAY_NAME_COLUMN,
        MIME_TYPE_COLUMN,
        DURATION_COLUMN,
        SIZE_COLUMN,
        WIDTH_COLUMN,
        HEIGHT_COLUMN,
        BUCKET_ID_COLUMN,
        BUCKET_DISPLAY_NAME_COLUMN
    )

    private val mediaCache: ConcurrentHashMap<Long, MediaData> = ConcurrentHashMap()

    fun loadMediaFlow(
        context: Context,
        mediaType: MediaType = MediaType.ImageAndVideo
    ): Flow<List<Folder>> = flow {
        val selection = getSelectionStr(mediaType)
        val sortOrder = "$DATE_MODIFIED_COLUMN DESC"
        
        // 先获取总数
        var cursor = context.contentResolver.query(
            CONTENT_URI,
            PROJECTIONS,
            selection,
            null,
            sortOrder
        )
        
        val totalCount = cursor?.count ?: 0
        cursor?.close()
        
        var processed = 0
        var batchSize = INITIAL_BATCH_SIZE

        while (processed < totalCount) {
            cursor = context.contentResolver.query(
                CONTENT_URI,
                PROJECTIONS,
                selection,
                null,
                sortOrder
            )

            cursor?.use { cursor ->
                // 跳过已处理的项
                var skipped = 0
                while (skipped < processed && cursor.moveToNext()) {
                    skipped++
                }
                
                // 处理当前批次
                var count = 0
                val mediaDataList = mutableListOf<MediaData>()
                
                while (count < batchSize && cursor.moveToNext()) {
                    processMediaItem(cursor)?.let { 
                        mediaDataList.add(it)
                        count++
                    }
                }

                if (mediaDataList.isNotEmpty()) {
                    cacheResult(mediaDataList)
                    emit(createFolders())
                    
                    processed += count
                    batchSize = (batchSize * 2).coerceAtMost(MAX_BATCH_SIZE)
                }
            }
        }
    }

    private fun processMediaItem(cursor: Cursor): MediaData? {
        val defaultId = Long.MAX_VALUE
        val id = cursor.getLong(ID_COLUMN, defaultId)
        val data = cursor.getString(DATA_COLUMN, "")
        val size = cursor.getLong(SIZE_COLUMN, 0)
        
        if (id == defaultId || data.isBlank() || size <= 0) {
            return null
        }
        
        val file = File(data)
        if (!file.isFile || !file.exists()) {
            return null
        }

        val name = cursor.getString(DISPLAY_NAME_COLUMN, "")
        val mimeType = cursor.getString(MIME_TYPE_COLUMN, "")
        val bucketId = cursor.getString(BUCKET_ID_COLUMN, "")
        val bucketName = cursor.getString(BUCKET_DISPLAY_NAME_COLUMN, "")
        val width = cursor.getInt(WIDTH_COLUMN, 0)
        val height = cursor.getInt(HEIGHT_COLUMN, 0)
        val uri = ContentUris.withAppendedId(CONTENT_URI, id)
        val type = cursor.getInt(MEDIA_TYPE_COLUMN, 0)
        val isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        val durationMs = if (isVideo) cursor.getInt(DURATION_COLUMN, 0) else 0
        val modifiedTime = cursor.getLong(DATE_MODIFIED_COLUMN, 0)

        return MediaData(
            mediaId = id,
            path = data,
            uri = uri,
            width = width,
            height = height,
            name = name,
            modifiedTime = modifiedTime,
            mimeType = mimeType,
            bucketId = bucketId,
            bucketName = bucketName,
            fileSize = size,
            durationMs = durationMs,
        )
    }

    private fun createFolders(): List<Folder> {
        val result = mutableListOf<Folder>()
        
        // 全部媒体
        val allMedia = mediaCache.values.sortedByDescending { it.modifiedTime }
        val updatedTime = allMedia.firstOrNull()?.modifiedTime ?: 0L
        result.add(Folder(IMAGES_AND_VIDEOS_FOLDER_KEY, IMAGES_AND_VIDEOS_FOLDER_NAME, updatedTime, allMedia))
        
        // 图片
        val images = allMedia.filter { it.isImage }.sortedByDescending { it.modifiedTime }
        val imagesUpdatedTime = images.firstOrNull()?.modifiedTime ?: 0L
        result.add(Folder(IMAGES_FOLDER_KEY, IMAGES_FOLDER_NAME, imagesUpdatedTime, images))
        
        // 视频
        val videos = allMedia.filter { it.isVideo }.sortedByDescending { it.modifiedTime }
        val videosUpdatedTime = videos.firstOrNull()?.modifiedTime ?: 0L
        result.add(Folder(VIDEOS_FOLDER_KEY, VIDEOS_FOLDER_NAME, videosUpdatedTime, videos))
        
        // 按文件夹分组
        val buckets = allMedia.groupBy { it.bucketId }.mapValues { (bucketId, medias) ->
            val sortedMedias = medias.sortedByDescending { it.modifiedTime }
            val bucketName = sortedMedias.firstOrNull()?.bucketName ?: ""
            val updateTime = sortedMedias.firstOrNull()?.modifiedTime ?: 0L
            Folder(bucketId, bucketName, updateTime, sortedMedias)
        }
        result.addAll(buckets.values)
        
        return result
    }

    private suspend fun queryByIds(
        context: Context,
        idList: List<Long>,
        builder: StringBuilder,
    ) {
        builder.setLength(0)
        if (idList.size == 1) {
            builder.append(MediaStore.MediaColumns._ID).append('=').append(idList[0])
        } else {
            builder.append(MediaStore.MediaColumns._ID).append(" in (")
            for (id in idList) {
                builder.append(id).append(',')
            }
            builder.setCharAt(builder.length - 1, ')')
        }
        query(context, builder.toString())
    }

    private suspend fun query(
        context: Context,
        idSelection: String?,
    ): List<MediaData>? {
        return withContext(Dispatchers.Default) {
            val selection = idSelection ?: getSelectionStr()
            val sortOrder = "$DATE_MODIFIED_COLUMN DESC"
            val list = kotlin.runCatching {
                val cursor = context.contentResolver.query(
                    CONTENT_URI,
                    PROJECTIONS,
                    selection,
                    null,
                    sortOrder,
                )
                if (cursor == null) {
                    return@runCatching null
                }
                val mediaDataList = mutableListOf<MediaData>()
                cursor.use { cursor ->
                    while (cursor.moveToNext()) {
                        val defaultId = Long.MAX_VALUE
                        val id = cursor.getLong(ID_COLUMN, defaultId)
                        val data = cursor.getString(DATA_COLUMN, "")
                        val size = cursor.getLong(SIZE_COLUMN, 0)
                        if (id == defaultId || data.isBlank() || size <= 0) {
                            continue
                        }
                        val file = File(data)
                        if (!file.isFile || !file.exists()) {
                            continue
                        }

                        val name = cursor.getString(DISPLAY_NAME_COLUMN, "")
                        val mimeType = cursor.getString(MIME_TYPE_COLUMN, "")
                        val bucketId = cursor.getString(BUCKET_ID_COLUMN, "")
                        val bucketName = cursor.getString(BUCKET_DISPLAY_NAME_COLUMN, "")
                        val width = cursor.getInt(WIDTH_COLUMN, 0)
                        val height = cursor.getInt(HEIGHT_COLUMN, 0)
                        val uri = ContentUris.withAppendedId(CONTENT_URI, id)
                        val type = cursor.getInt(MEDIA_TYPE_COLUMN, 0)
                        val isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        val durationMs = if (isVideo) cursor.getInt(DURATION_COLUMN, 0) else 0
                        val modifiedTime = cursor.getLong(DATE_MODIFIED_COLUMN, 0)

                        val mediaData = MediaData(
                            mediaId = id,
                            path = data,
                            uri = uri,
                            width = width,
                            height = height,
                            name = name,
                            modifiedTime = modifiedTime,
                            mimeType = mimeType,
                            bucketId = bucketId,
                            bucketName = bucketName,
                            fileSize = size,
                            durationMs = durationMs,
                        )
                        mediaDataList.add(element = mediaData)
                    }
                }
                return@runCatching mediaDataList
            }.getOrElse {
                Log.e(TAG, "loadMedia error: ", it)
                null
            }
            cacheResult(list)
            list
        }
    }

    private fun cacheResult(list: List<MediaData>?) {
        if (!list.isNullOrEmpty()) {
            val excludeList: MutableList<MediaData> = ArrayList()
            val currentTimeSeconds = System.currentTimeMillis() / 1000
            for (item in list) {
                // If the picture is just taking by camera,
                // it might be processing while picture info had already been saved to media database.
                // So we need to check if the picture file is exists when the picture is new.
                if (currentTimeSeconds - item.modifiedTime < NEW_PICTURE_THRESHOLD) {
                    if (File(item.path).exists()) {
                        mediaCache[item.mediaId] = item
                    } else {
                        excludeList.add(item)
                    }
                } else {
                    mediaCache[item.mediaId] = item
                }
            }
        } else {
            Log.e(TAG, "cache list is null or empty")
        }
    }

    private fun getSelectionStr(mediaType: MediaType = MediaType.ImageAndVideo) : String {
        val mediaTypeColumn = MediaStore.Files.FileColumns.MEDIA_TYPE
        val mediaTypeImageColumn = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        val mediaTypeVideoColumn = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        val mimeTypeColumn = MediaStore.Files.FileColumns.MIME_TYPE
        val queryImageSelection =
            "$mediaTypeColumn = $mediaTypeImageColumn and $mimeTypeColumn like 'image/%'"
        val queryVideoSelection =
            "$mediaTypeColumn = $mediaTypeVideoColumn and $mimeTypeColumn like 'video/%'"
        return when (mediaType) {
            MediaType.ImageAndVideo -> {
                buildString {
                    append(queryImageSelection)
                    append(" or ")
                    append(queryVideoSelection)
                }
            }
            MediaType.Image -> queryImageSelection
            MediaType.Video -> queryVideoSelection
        }
    }

    private fun Cursor.getInt(columnName: String, default: Int): Int {
        return try {
            val columnIndex = getColumnIndexOrThrow(columnName)
            getInt(columnIndex)
        } catch (throwable: IllegalArgumentException) {
            throwable.printStackTrace()
            default
        }
    }

    private fun Cursor.getLong(columnName: String, default: Long): Long {
        return try {
            val columnIndex = getColumnIndexOrThrow(columnName)
            getLong(columnIndex)
        } catch (throwable: IllegalArgumentException) {
            throwable.printStackTrace()
            default
        }
    }

    private fun Cursor.getString(columnName: String, default: String): String {
        return try {
            val columnIndex = getColumnIndexOrThrow(columnName)
            getString(columnIndex) ?: default
        } catch (throwable: IllegalArgumentException) {
            throwable.printStackTrace()
            default
        }
    }

}