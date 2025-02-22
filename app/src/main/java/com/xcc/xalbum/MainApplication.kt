package com.xcc.xalbum

import android.app.Application
import android.content.Context
import android.os.Build
import coil3.request.allowHardware
import coil3.request.crossfade
import com.xcc.album.ui.di.xAlbumModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化 Koin
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(xAlbumModule)
        }
        initCoil(this)
        initCoil3()
    }

    private fun initCoil(context: Context) {
        val imageLoader = coil.ImageLoader.Builder(context = context)
            .crossfade(enable = false)
            .allowHardware(enable = true)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(coil.decode.ImageDecoderDecoder.Factory())
                } else {
                    add(coil.decode.GifDecoder.Factory())
                }
                add(coil.decode.VideoFrameDecoder.Factory())
            }
            .build()
        coil.Coil.setImageLoader(imageLoader)
    }

    private fun initCoil3() {
        coil3.SingletonImageLoader.setSafe(factory = { context ->
            coil3.ImageLoader
                .Builder(context = context)
                .crossfade(enable = false)
                .allowHardware(enable = true)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(coil3.gif.AnimatedImageDecoder.Factory())
                    } else {
                        add(coil3.gif.GifDecoder.Factory())
                    }
                    add(coil3.video.VideoFrameDecoder.Factory())
                }
                .build()
        })
    }
}