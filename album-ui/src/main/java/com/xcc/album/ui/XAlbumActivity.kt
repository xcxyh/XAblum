package com.xcc.album.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xcc.album.ui.feature.XAlbumScreen
import com.xcc.album.ui.viewmodel.XAlbumIntent
import com.xcc.album.ui.viewmodel.XAlbumViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class XAlbumActivity : ComponentActivity() {

    private val viewModel by viewModel<XAlbumViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XAlbumScreen(
                viewModel = viewModel,
                onBackClick = { finish() }
            )
        }
        viewModel.setIntent(XAlbumIntent.LoadMedia(this))
    }
}