package com.xcc.mvi.improved.example.di

import com.xcc.mvi.improved.example.UserProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val userModule = module {
    viewModelOf(::UserProfileViewModel)
}