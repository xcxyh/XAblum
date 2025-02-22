package com.xcc.album.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val LocalXAlbumColorScheme = staticCompositionLocalOf { LightXAlbumColorScheme }

@Composable
fun XAlbumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val xAlbumColorScheme = if (darkTheme) {
        DarkXAlbumColorScheme
    } else {
        LightXAlbumColorScheme
    }

    CompositionLocalProvider(
        LocalXAlbumColorScheme provides xAlbumColorScheme,
        LocalDensity provides Density(LocalDensity.current.density, fontScale = 1f),
    ) {
        content()
    }
}

// 在其他 Composable 中使用这些颜色的方式
object XAlbumTheme {
    val colors: XAlbumColorScheme
        @Composable
        get() = LocalXAlbumColorScheme.current
}