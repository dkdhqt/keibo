package com.jp_ais_training.keibo.util

import android.app.PendingIntent
import android.os.Build

class TargetSDKUtil {

    companion object {
        fun getFlags() : Int{
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> Const.NOTI_RECEIVER_PENDING_INTENT_FLAGS or PendingIntent.FLAG_IMMUTABLE
                else -> Const.NOTI_RECEIVER_PENDING_INTENT_FLAGS
            }
        }
    }
}