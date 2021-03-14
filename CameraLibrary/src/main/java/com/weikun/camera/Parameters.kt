package com.weikun.camera

import android.util.Size

data class Parameters(
        val previewSize:Size,
        val type:Int
){
    companion object{
        const val TYPE_PREVIEW = 0
        const val TYPE_ZXING_DECODING = 1
        const val TYPE_ZXING_DECODING_WITH_SCANNER = 2
    }
}