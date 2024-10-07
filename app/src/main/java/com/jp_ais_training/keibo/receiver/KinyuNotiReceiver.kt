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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class KinyuNotiReceiver: BroadcastReceiver() {

    private val TAG = this::class.java.simpleName.toString()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { it ->
            val calendar = Calendar.getInstance()

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

            val today = "$year-$month-$day"

            val DB = AppDatabase.getInstance(context!!)!!

            CoroutineScope(Dispatchers.IO).launch {
                // $year-$month-$day : YYYY-MM-DD
                val fixEI = DB.dao().loadFixEI(today)
                val flexEI = DB.dao().loadFlexEI(today)
                val fixII = DB.dao().loadFixII(today)
                val flexII = DB.dao().loadFlexII(today)

                // 고정 지출, 변동 지출, 고정 수입, 변동 수입 모두 비어있다면,
                if (fixEI.isEmpty() && flexEI.isEmpty() && fixII.isEmpty() && flexII.isEmpty()) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra(Const.NOTI_INTENT_TYPE_KEY_WHAT_TO_DO, Const.NOTI_INTENT_TYPE_VALUE_GO_TO_SYOUSAI_PAGE)
                    intent.putExtra(Const.KINYU_MAIN_ACTIVITY_EXTRA_YEAR, year)
                    intent.putExtra(Const.KINYU_MAIN_ACTIVITY_EXTRA_MONTH, month)
                    intent.putExtra(Const.KINYU_MAIN_ACTIVITY_EXTRA_DAY, day)

                    val pendingIntent = PendingIntent.getActivity(context, Const.PENDING_INTENT_REQUEST_CODE, intent, Const.PENDING_INTENT_FLAGS)

                    val contentTitle = Const.KINYU_NOTI_CONTENT_TITLE
                    val contentText = "$today\n${Const.KINYU_NOTI_CONTENT_TEXT}"

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
                    notificationManagerCompat.notify(Const.KINYU_NOTIFICATION_ID, builder.build())
                }
            }
        }
    }
}