package com.xcc.album.ui.theme

import androidx.compose.ui.graphics.Color

data class XAlbumColorScheme(
    // Status and Navigation bars
    val statusBarColor: Color,
    val navigationBarColor: Color,

    // Main UI colors
    val mainPageBackground: Color,
    val mediaItemBackground: Color,
    val mediaItemSelectedScrim: Color,
    val captureItemBackground: Color,
    val captureItemIcon: Color,

    // Top bar colors
    val topBarBackground: Color,
    val topBarIcon: Color,
    val topBarText: Color,

    // Dropdown menu colors
    val dropdownMenuBackground: Color,
    val dropdownMenuText: Color,

    // Bottom navigation colors
    val bottomNavBackground: Color,

    // Preview related colors
    val previewText: Color,
    val previewTextDisabled: Color,
    val confirmText: Color,
    val confirmTextDisabled: Color,
    val previewPageBackground: Color,
    val previewBottomNavBackground: Color,
    val previewBackText: Color,
    val previewConfirmText: Color,
    val previewConfirmTextDisabled: Color,

    // Checkbox colors
    val checkBoxCircle: Color,
    val checkBoxCircleDisabled: Color,
    val checkBoxCircleFill: Color,
    val checkBoxText: Color,

    // Other UI elements
    val circularLoading: Color,
    val videoIcon: Color,
    val videoViewPageBackground: Color,
)

val LightXAlbumColorScheme = XAlbumColorScheme(
    statusBarColor = Color(0xFF03A9F4),
    navigationBarColor = Color(0xFFFFFFFF),
    mainPageBackground = Color(0xFFFFFFFF),
    mediaItemBackground = Color(0x66CCCCCC),
    mediaItemSelectedScrim = Color(0x80000000),
    captureItemBackground = Color(0x66CCCCCC),
    captureItemIcon = Color(0xFFFFFFFF),
    topBarBackground = Color(0xFF03A9F4),
    topBarIcon = Color(0xFFFFFFFF),
    topBarText = Color(0xFFFFFFFF),
    dropdownMenuBackground = Color(0xFFFFFFFF),
    dropdownMenuText = Color(0xFF000000),
    bottomNavBackground = Color(0xFFFFFFFF),
    previewText = Color(0xFF000000),
    previewTextDisabled = Color(0xFFC6CCD2),
    confirmText = Color(0xFF03A9F4),
    confirmTextDisabled = Color(0x6003A9F4),
    previewPageBackground = Color(0xFF22202A),
    previewBottomNavBackground = Color(0xFF2B2A34),
    previewBackText = Color(0xFFFFFFFF),
    previewConfirmText = Color(0xFF03A9F4),
    previewConfirmTextDisabled = Color(0x6003A9F4),
    checkBoxCircle = Color(0xFFFFFFFF),
    checkBoxCircleDisabled = Color(0x60FFFFFF),
    checkBoxCircleFill = Color(0xFF03A9F4),
    checkBoxText = Color(0xFFFFFFFF),
    circularLoading = Color(0xFF03A9F4),
    videoIcon = Color(0xFFFFFFFF),
    videoViewPageBackground = Color(0xFF22202A)
)

val DarkXAlbumColorScheme = XAlbumColorScheme(
    statusBarColor = Color(0xFF2B2A34),
    navigationBarColor = Color(0xFF2B2A34),
    mainPageBackground = Color(0xFF22202A),
    mediaItemBackground = Color(0xCCFFFFFF),
    mediaItemSelectedScrim = Color(0x80000000),
    captureItemBackground = Color(0xCCFFFFFF),
    captureItemIcon = Color(0xFFFFFFFF),
    topBarBackground = Color(0xFF2B2A34),
    topBarIcon = Color(0xFFFFFFFF),
    topBarText = Color(0xFFFFFFFF),
    dropdownMenuBackground = Color(0xFF2B2A34),
    dropdownMenuText = Color(0xFFFFFFFF),
    bottomNavBackground = Color(0xFF2B2A34),
    previewText = Color(0xFFFFFFFF),
    previewTextDisabled = Color(0x99FFFFFF),
    confirmText = Color(0xFFFFFFFF),
    confirmTextDisabled = Color(0x99FFFFFF),
    previewPageBackground = Color(0xFF22202A),
    previewBottomNavBackground = Color(0xFF2B2A34),
    previewBackText = Color(0xFFFFFFFF),
    previewConfirmText = Color(0xFFFFFFFF),
    previewConfirmTextDisabled = Color(0x99FFFFFF),
    checkBoxCircle = Color(0xFFFFFFFF),
    checkBoxCircleDisabled = Color(0x80FFFFFF),
    checkBoxCircleFill = Color(0xFF009688),
    checkBoxText = Color(0xFFFFFFFF),
    circularLoading = Color(0xFF009688),
    videoIcon = Color(0xFFFFFFFF),
    videoViewPageBackground = Color(0xFF22202A)
)