package com.jp_ais_training.keibo.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.util.Const
import com.jp_ais_training.keibo.activity.MainActivity
import com.jp_ais_training.keibo.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FixExpenseReceiver : BroadcastReceiver() {

    private val TAG = this::class.java.simpleName.toString()

    override fun onReceive(context: Context?, intent: Intent?) {

        // 해당 리시버가 동작했다는 것은 내일(리시버가 동작한 다음날) 고정 지출이 있고,
        // 이에 대해 알림이 필요하다는 것을 의미
        val calendar = Calendar.getInstance()
        Log.d(TAG, "FixExpenseReceiver ${calendar.timeInMillis}")

        // 날짜 데이터를 내일,하루뒤로 변경
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)

        val year = calendar.get(Calendar.YEAR)
        val month = (calendar.get(Calendar.MONTH) + 1).let {
            if (it < 10) {
                "0$it"
            } else {
                it.toString()
            }
        }
        val day = calendar.get(Calendar.DAY_OF_MONTH).let {
            if (it < 10) {
                "0$it"
            } else {
                it.toString()
            }
        }

        val tomorrow = "$year-$month-$day"
//        val tomorrow = "2022-05-11"   // for test

        val DB = AppDatabase.getInstance(context!!)!!

        CoroutineScope(Dispatchers.IO).launch {
            // $year-$month-$day : YYYY-MM-DD
            val fixExpenseList = DB.dao().loadFixEI(tomorrow)

            Log.d(TAG, "fixExpenseList: $fixExpenseList")

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            val pendingIntent = PendingIntent.getActivity(
                context,
                Const.PENDING_INTENT_REQUEST_CODE,
                intent,
                Const.PENDING_INTENT_FLAGS
            )

            val contentTitle = Const.FIX_EXPENSE_NOTI_CONTENT_TITLE
            // 明日(XXXX-XX-XX)
            var contentText = "${Const.FIX_EXPENSE_NOTI_CONTENT_TEXT_1} ($tomorrow)\n"
            var sum = 0
            for (item in fixExpenseList) {
                contentText += "${item.name} ${item.price}円\n"
                item.price?.let {
                    sum+= it
                }
            }
            contentText += "合計(${sum}円) ${Const.FIX_EXPENSE_NOTI_CONTENT_TEXT_2}"

            val builder = NotificationCompat.Builder(context!!, Const.KINYU_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            val notificationManagerCompat = NotificationManagerCompat.from(context!!)
            notificationManagerCompat.notify(Const.FIX_EXPENSE_NOTIFICATION_ID, builder.build())    // 한번에 묶어서
        }
    }
}