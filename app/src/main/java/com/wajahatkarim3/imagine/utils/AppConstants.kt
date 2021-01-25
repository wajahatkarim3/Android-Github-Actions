package com.wajahatkarim3.imagine.utils

import com.wajahatkarim3.imagine.BuildConfig

object AppConstants {
    object API {
        val PHOTOS_PER_PAGE = 30
        val API_KEY = "Client-ID " + BuildConfig.UNSPLASH_API_KEY
    }
}