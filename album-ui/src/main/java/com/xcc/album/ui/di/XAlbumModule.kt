package com.xcc.album.ui.di

import com.xcc.album.ui.viewmodel.XAlbumViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val xAlbumModule = module {
    viewModelOf(::XAlbumViewModel)
}