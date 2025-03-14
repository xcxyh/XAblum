package com.xcc.xalbum

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import com.xcc.album.data.model.MediaData
import com.xcc.album.ui.XAlbumActivity
import com.xcc.mvi.improved.example.UserProfileScreen
import com.xcc.mvi.improved.example.UserProfileViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_CODE_ALBUM = 1001
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startXAlbum()
        }
    }

    private val viewModel by viewModel<UserProfileViewModel>()

    private val selectedMedia = mutableStateListOf<MediaData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            MainScreen(
//                selectedMedia = selectedMedia,
//                onOpenAlbumClick = { requestPermissions() }
//            )
            UserProfileScreen(viewModel)
        }
        viewModel.loadUserProfile("xxccc")
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(getRequiredPermissions())
    }

    private fun startXAlbum() {
        XAlbumActivity.launch(this, REQUEST_CODE_ALBUM)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ALBUM && resultCode == Activity.RESULT_OK) {
            val medias = data?.getParcelableArrayListExtra<MediaData>(XAlbumActivity.EXTRA_SELECTED_MEDIA)
            if (!medias.isNullOrEmpty()) {
                selectedMedia.clear()
                selectedMedia.addAll(medias)
            }
        }
    }
}