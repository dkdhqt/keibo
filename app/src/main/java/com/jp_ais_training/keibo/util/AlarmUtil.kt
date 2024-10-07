package com.jp_ais_training.keibo.util

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.jp_ais_training.keibo.receiver.AutoAddFixExpenseReceiver
import java.util.*

class AlarmUtil(val context: Context) {

    private val TAG = this::class.java.simpleName.toString()

    fun setAutoAddFixExpense() {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            // 다음달 1일 00시 00분 00초로 broadcast 추가
            val targetCalendar = Calendar.getInstance()

            val currentYear = targetCalendar.get(Calendar.YEAR)
            val currentMonth = targetCalendar.get(Calendar.MONTH)
            val currentDay = targetCalendar.get(Calendar.DAY_OF_MONTH)

            Log.d(TAG, "$currentYear-$currentMonth-$currentDay")

            // 다음달 1일 00시 00분 00초
            targetCalendar.set(Calendar.MONTH, currentMonth + 1)
            targetCalendar.set(Calendar.DAY_OF_MONTH, Const.ALARM_DAY_OF_MONTH_1)
            targetCalendar.set(Calendar.HOUR_OF_DAY, Const.ALARM_HOUR_OF_DAY_ZERO)
            targetCalendar.set(Calendar.MINUTE, Const.NOTI_MINUTE_ZERO)
            targetCalendar.set(Calendar.SECOND, Const.NOTI_SECOND_ZERO)
            targetCalendar.set(Calendar.MILLISECOND, Const.NOTI_MILLISECOND_ZERO)


            // test용
//            targetCalendar.set(Calendar.MINUTE, targetCalendar.get(Calendar.MINUTE) + 1)
//            targetCalendar.set(Calendar.SECOND, Const.NOTI_SECOND_ZERO)
//            targetCalendar.set(Calendar.MILLISECOND, Const.NOTI_MILLISECOND_ZERO)

            // Log용
//            val targetYear = targetCalendar.get(Calendar.YEAR)
//            val targetMonth = targetCalendar.get(Calendar.MONTH)
//            val targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH)
//            val targetHour = targetCalendar.get(Calendar.HOUR_OF_DAY)
//            val targetMinute = targetCalendar.get(Calendar.MINUTE)
//
//            Log.d(TAG, "$targetYear-$targetMonth-$targetDay $targetHour:$targetMinute")

            // AlarmManagert 생성 (특정 시간을 인식하는 BroadCast 생성 클래스)
            val alarmManager = context.getSystemService(Application.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AutoAddFixExpenseReceiver::class.java)

            // 매월 1일00시00분00초에 실행되는 intent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                Const.NOTI_RECEIVER_PENDING_INTENT_REQUEST_CODE,
                intent,
                TargetSDKUtil.getFlags()
            )

            // 매월 1일00시00분00초에 실행
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                targetCalendar.timeInMillis,
                pendingIntent
            )
        }
    }
}