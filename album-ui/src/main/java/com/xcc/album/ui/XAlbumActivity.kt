package com.xcc.album.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xcc.album.ui.feature.XAlbumScreen
import com.xcc.album.ui.viewmodel.XAlbumIntent
import com.xcc.album.ui.viewmodel.XAlbumViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class XAlbumActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SELECTED_MEDIA = "extra_selected_media"
        
        fun launch(activity: Activity, requestCode: Int) {
            activity.startActivityForResult(
                Intent(activity, XAlbumActivity::class.java),
                requestCode
            )
        }
    }

    private val viewModel by viewModel<XAlbumViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XAlbumScreen(
                onBackClick = { finish() },
                onConfirmClick = {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putParcelableArrayListExtra(
                            EXTRA_SELECTED_MEDIA,
                            ArrayList(viewModel.state.selectedItems)
                        )
                    })
                    finish()
                }
            )
        }
        viewModel.setIntent(XAlbumIntent.LoadMedia(this))
    }

    override fun onBackPressed() {
        if (viewModel.state.isDropdownExpanded) {
            viewModel.setIntent(XAlbumIntent.ToggleDropdown(false))
            return
        }
        super.onBackPressed()
    }
}