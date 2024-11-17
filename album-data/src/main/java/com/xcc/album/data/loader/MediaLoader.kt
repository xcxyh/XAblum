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
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object MediaLoader {

    private const val TAG: String = "MediaLoader"

    private const val NEW_PICTURE_THRESHOLD = 10
    private const val QUERY_BATCH_SIZE = 50

    private const val IMAGES_AND_VIDEOS_FOLDER_KEY = "images_and_videos_folder_key"
    private const val IMAGES_FOLDER_KEY = "images_and_videos_folder_key"
    private const val VIDEOS_FOLDER_KEY = "images_and_videos_folder_key"

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

    suspend fun loadMedia(
        context: Context,
        mediaType: MediaType,
    ): List<Folder> {
        val idSelection = getSelectionStr(mediaType)
        if (mediaCache.isEmpty()) {
            query(context, idSelection)
        } else {
            refresh(context, idSelection)
        }
        val result = mutableListOf<Folder>()
        // 全部
        val updatedTime = mediaCache.values.firstOrNull()?.modifiedTime ?: 0L
        val allFolder = Folder(IMAGES_AND_VIDEOS_FOLDER_KEY, IMAGES_AND_VIDEOS_FOLDER_NAME, updatedTime, mediaCache.values.toList())
        // 图片
        val images = mediaCache.values.filter { it.isImage }
        val imagesUpdatedTime = images.firstOrNull()?.modifiedTime ?: 0L
        val imageFolder = Folder(IMAGES_FOLDER_KEY, IMAGES_FOLDER_NAME, imagesUpdatedTime, images)
        // 视频
        val videos = mediaCache.values.filter { it.isVideo }
        val videosUpdatedTime = videos.firstOrNull()?.modifiedTime ?: 0L
        val videoFolder = Folder(VIDEOS_FOLDER_KEY, VIDEOS_FOLDER_NAME, videosUpdatedTime, videos)
        // buckets
        val buckets = mediaCache.values.groupBy { it.bucketId }.mapValues { (bucketId, medias) ->
            val sortedMedias = medias.sortedByDescending { it.modifiedTime }
            val bucketName = sortedMedias.firstOrNull()?.bucketName ?: ""
            val updateTime = sortedMedias.firstOrNull()?.modifiedTime ?: 0L
            Folder(bucketId, bucketName, updateTime, sortedMedias)
        }
        result.add(allFolder)
        result.add(imageFolder)
        result.add(videoFolder)
        result.addAll(buckets.values)
        return result
    }

    suspend fun loadMedia(
        context: Context,
        uri: Uri,
    ): MediaData? {
        return withContext(context = Dispatchers.Default) {
            val id = ContentUris.parseId(uri)
            val selection = MediaStore.MediaColumns._ID + " = " + id
            val resources = query(
                context = context,
                idSelection = selection,
            )
            if (resources.isNullOrEmpty() || resources.size != 1) {
                return@withContext null
            }
            return@withContext resources[0]
        }
    }

    private suspend fun refresh(
        context: Context,
        idSelection: String?
    ): Boolean {
        val cursor: Cursor? = context.contentResolver.query(
            CONTENT_URI,
            PROJECTIONS,
            idSelection ?: getSelectionStr(),
            null,
            null,
        )
        if (cursor == null) {
            return false
        }
        val ids = mutableListOf<Long>()
        kotlin.runCatching {
            cursor.use {
                while (cursor.moveToNext()) {
                    val defaultId = Long.MAX_VALUE
                    val id = cursor.getLong(ID_COLUMN, defaultId)
                    val data = cursor.getString(DATA_COLUMN, "")
                    val size = cursor.getLong(SIZE_COLUMN, 0)
                    if (id == defaultId || data.isBlank() || size <= 0) {
                        continue
                    }
                    ids.add(id)
                }
            }
        }.getOrElse {
            Log.e(TAG, "refresh: error ", it)
        }

        val cacheSet = mediaCache.keys
        val newIdSet = ids.toSet()
        if (cacheSet == newIdSet) {
            return false
        }
        cacheSet.retainAll(newIdSet)
        ids.removeAll(cacheSet)
        if (ids.isNotEmpty()) {
            // query the increment
            val builder = StringBuilder()
            if (ids.size < QUERY_BATCH_SIZE) {
                queryByIds(context, ids, builder)
            } else {
                val idList = mutableListOf<Long>()
                for (id in ids) {
                    idList.add(id)
                    if (idList.size == QUERY_BATCH_SIZE) {
                        queryByIds(context, idList, builder)
                        idList.clear()
                    }
                }
                if (idList.isNotEmpty()) {
                    queryByIds(context, idList, builder)
                }
            }
        }
        return true
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