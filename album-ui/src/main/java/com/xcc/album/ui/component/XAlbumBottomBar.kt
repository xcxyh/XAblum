package com.xcc.album.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xcc.album.ui.R
import com.xcc.album.ui.theme.XAlbumTheme

@Composable
fun XAlbumBottomBar(
    selectedCount: Int,
    onPreviewClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextButton(
            onClick = onPreviewClick,
            enabled = selectedCount > 0,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = stringResource(R.string.xalbum_preview),
                color = if (selectedCount > 0)
                    XAlbumTheme.colors.previewText
                else XAlbumTheme.colors.previewTextDisabled
            )
        }

        TextButton(
            onClick = onConfirmClick,
            enabled = selectedCount > 0,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = if (selectedCount > 0)
                    stringResource(R.string.xalbum_confirm_with_count, selectedCount)
                else stringResource(R.string.xalbum_confirm),
                color = if (selectedCount > 0)
                    XAlbumTheme.colors.confirmText
                else XAlbumTheme.colors.confirmTextDisabled,
                textAlign = TextAlign.Center
            )
        }
    }
}