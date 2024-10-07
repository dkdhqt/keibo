package com.jp_ais_training.keibo.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.util.Const
import com.jp_ais_training.keibo.activity.MainActivity
import com.jp_ais_training.keibo.db.AppDatabase
import com.jp_ais_training.keibo.model.response.LoadSumEI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ComparisonExpenseNotiReceiver: BroadcastReceiver() {

    private val TAG = this::class.java.simpleName.toString()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { it ->
            val calendar = Calendar.getInstance()

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = (calendar.get(Calendar.MONTH) + 1).let {
                if (it < 10) {
                    "0$it"
                } else {
                    it.toString()
                }
            }

            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)

            val prevYear = calendar.get(Calendar.YEAR)
            val prevMonth = (calendar.get(Calendar.MONTH) + 1).let {
                if (it < 10) {
                    "0$it"
                } else {
                    it.toString()
                }
            }

            val current = "$currentYear-$currentMonth"
            val prev = "$prevYear-$prevMonth"

            val DB = AppDatabase.getInstance(context!!)!!

            CoroutineScope(Dispatchers.IO).launch {

                val currentMonthExpenseSum: LoadSumEI =  DB.dao().loadMonthSumEI(current)[0]
                val prevMonthExpenseSum: LoadSumEI =  DB.dao().loadMonthSumEI(prev)[0]

                // 데이터가 있을 경우
                if (currentMonthExpenseSum.price != null && prevMonthExpenseSum.price != null) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra(Const.NOTI_INTENT_TYPE_KEY_WHAT_TO_DO, Const.NOTI_INTENT_TYPE_VALUE_GO_TO_BAR_STATISTIC)
                    intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    val pendingIntent = PendingIntent.getActivity(context, Const.PENDING_INTENT_REQUEST_CODE, intent, Const.PENDING_INTENT_FLAGS)

                    val contentTitle = Const.COMPARISON_NOTI_CONTENT_TITLE
                    val contentText =
                        "${Const.COMPARISON_NOTI_CONTENT_TEXT_1}${prevMonthExpenseSum.price}円\n" +
                                "${Const.COMPARISON_NOTI_CONTENT_TEXT_2}${currentMonthExpenseSum.price}円\n" +
                                "${Const.COMPARISON_NOTI_CONTENT_TEXT_3}${currentMonthExpenseSum.price - prevMonthExpenseSum.price}${Const.COMPARISON_NOTI_CONTENT_TEXT_4}"

                    val builder = NotificationCompat.Builder(context!!, Const.COMPARISON_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setAutoCancel(true)
                        .setStyle(NotificationCompat.BigTextStyle())
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)

                    val notificationManagerCompat = NotificationManagerCompat.from(context!!)
                    notificationManagerCompat.notify(Const.COMPARISON_NOTIFICATION_ID, builder.build())
                }

            }
        }
    }
}