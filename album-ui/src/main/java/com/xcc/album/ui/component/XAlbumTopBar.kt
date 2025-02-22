package com.xcc.album.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.xcc.album.ui.R
import com.xcc.album.ui.theme.XAlbumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XAlbumTopBar(
    title: String,
    isDropdownExpanded: Boolean,
    onBackClick: () -> Unit,
    onTitleClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = XAlbumTheme.colors.topBarBackground,
            titleContentColor = XAlbumTheme.colors.topBarText,
            navigationIconContentColor = XAlbumTheme.colors.topBarIcon,
            actionIconContentColor = XAlbumTheme.colors.topBarIcon
        ),
        title = {
            TextButton(onClick = onTitleClick) {
                Text(
                    text = title,
                    color = XAlbumTheme.colors.topBarText
                )
                Icon(
                    imageVector = if (isDropdownExpanded) 
                        Icons.Default.KeyboardArrowUp 
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.xalbum_expand_album_list),
                    tint = XAlbumTheme.colors.topBarIcon
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.xalbum_back)
                )
            }
        }
    )
}