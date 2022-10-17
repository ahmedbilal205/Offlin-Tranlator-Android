package com.anb.offlinetranslator.utils

import com.anb.offlinetranslator.BuildConfig

class AppConfig {
    companion object{
        const val RECENTLY_USED_FIRST ="recently_used_first"
        const val RECENTLY_USED_SECOND = "recently_used_second"
        const val DOWNLOADED_LANGS = "downloaded_langs"
        const val TEXT_HISTORY = "text_history"
        const val PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    }
}