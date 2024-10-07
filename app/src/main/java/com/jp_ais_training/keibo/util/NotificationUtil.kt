package com.jp_ais_training.keibo.util

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.jp_ais_training.keibo.db.AppDatabase
import com.jp_ais_training.keibo.receiver.ComparisonExpenseNotiReceiver
import com.jp_ais_training.keibo.receiver.FixExpenseReceiver
import com.jp_ais_training.keibo.receiver.KinyuNotiReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class NotificationUtil(context: Context) : ContextWrapper(context) {

    private val TAG = this::class.java.simpleName.toString()
    lateinit var manager: NotificationManager

    private val mContext = this

    fun createChannels() {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 정기 고정 지출 알림 채널 생성
        val fixExpenseChannel = NotificationChannel(
            Const.FIX_EXPENSE_CHANNEL_ID,
            Const.FIX_EXPENSE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        // 이 채널에 게시된 알림이 알림 표시등(notification light)을 표시해야 하는지 여부를 설정합니다.
        fixExpenseChannel.enableLights(true)
        // 이 채널에 게시된 알림이 진동해야 하는지 여부를 설정합니다.
        fixExpenseChannel.enableVibration(true)
        // 이 채널에 게시된 알림에 대한 알림 라이트 색을 설정합니다.
        fixExpenseChannel.lightColor = Color.GREEN
// 이 채널에 게시된 알림이 잠금 화면에 표시되는지 여부를 설정합니다.
        fixExpenseChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(fixExpenseChannel)

        // 가계부 기입 요청 알림 채널 생성
        val kinyuChannel = NotificationChannel(
            Const.KINYU_CHANNEL_ID,
            Const.KINYU_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        // 이 채널에 게시된 알림이 알림 표시등(notification light)을 표시해야 하는지 여부를 설정합니다.
        kinyuChannel.enableLights(true)
        // 이 채널에 게시된 알림이 진동해야 하는지 여부를 설정합니다.
        kinyuChannel.enableVibration(true)
        // 이 채널에 게시된 알림에 대한 알림 라이트 색을 설정합니다.
        kinyuChannel.lightColor = Color.GREEN
// 이 채널에 게시된 알림이 잠금 화면에 표시되는지 여부를 설정합니다.
        kinyuChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(kinyuChannel)

        // 월말 지출 비교 알림
        val comparisonChannel = NotificationChannel(
            Const.COMPARISON_CHANNEL_ID,
            Const.COMPARISON_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        // 이 채널에 게시된 알림이 알림 표시등(notification light)을 표시해야 하는지 여부를 설정합니다.
        kinyuChannel.enableLights(true)
        // 이 채널에 게시된 알림이 진동해야 하는지 여부를 설정합니다.
        kinyuChannel.enableVibration(true)
        // 이 채널에 게시된 알림에 대한 알림 라이트 색을 설정합니다.
        kinyuChannel.lightColor = Color.GREEN
// 이 채널에 게시된 알림이 잠금 화면에 표시되는지 여부를 설정합니다.
        kinyuChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(comparisonChannel)
    }

    // 정기 고정 지출 알림 설정
    // 1. 이번달 DB 데이터 확인 (고정 지출)
    // 2. 오늘 이전날은 제외하고, 오늘부터 이번달내에 있는 고정 지출 알람 (하루전 21시)
    // 3. 한달에 한번씩 반복되도록 함
    fun setFixExpenseNotification() {
        // 알람 매니저 생성
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val DB = AppDatabase.getInstance(this)!!

        CoroutineScope(Dispatchers.IO).launch {
            val list = DB.dao().loadEI("2022-05")

            for (item in list) {
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val date = sdf.parse(item.datetime)
                val itemCalendar = Calendar.getInstance()
                // item.datetime을 담고 있는 calendar 객체
                itemCalendar.time = date

                // 정확하게는 하루 전 날짜 데이터를 담아야한다.
                // 더 정확하게는 하루 전 21시 날짜 데이터를 담아야 한다.
                // 알림이 발생하는 건 고정 지출 하루전 21시이기 때문이다.
                // 비교가 필요한 이유는 이미 과거에 데이터이면, 알림을 발생시킬 필요가 없고
                // 과거 시간에 알람을 발생시키면 무시하는게 아니라, 코드가 동작하는 순간에 알람을 주는 에러가 발생한다.
                itemCalendar.set(
                    Calendar.DAY_OF_MONTH,
                    itemCalendar.get(Calendar.DAY_OF_MONTH) - 1
                )   // 고정지출 하루 전
                itemCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    Const.NOTI_HOUR_OF_DAY_21
                )                       // 21시
                itemCalendar.set(
                    Calendar.MINUTE,
                    Const.NOTI_MINUTE_ZERO
                )                               // 00분
                itemCalendar.set(
                    Calendar.SECOND,
                    Const.NOTI_SECOND_ZERO
                )                               // 00초
                itemCalendar.set(
                    Calendar.MILLISECOND,
                    Const.NOTI_MILLISECOND_ZERO
                )                     // 00초

                // 오늘 날짜 데이터를 담는 캘린더 객체
                val currentCalendar = Calendar.getInstance()
                val alarmCalendar = Calendar.getInstance()

                // 테스트 하려면 조건문 주석
                if (currentCalendar.timeInMillis >= itemCalendar.timeInMillis) {    // 현재 시각 >= 알림 발생 시각 -> 과거와 현재를 의미
                    // 다음달 알람을 설정 + 한달마다 반복
                    alarmCalendar.set(
                        Calendar.MONTH,
                        currentCalendar.get(Calendar.MONTH) + 1
                    ) // 다음달
                    alarmCalendar.set(
                        Calendar.DAY_OF_MONTH,
                        itemCalendar.get(Calendar.DAY_OF_MONTH)
                    )   // 고정지출 하루전(-1은 위에서 이미 실시함)
                    alarmCalendar.set(Calendar.HOUR_OF_DAY, Const.NOTI_HOUR_OF_DAY_21)  // 21시
                    alarmCalendar.set(Calendar.MINUTE, Const.NOTI_MINUTE_ZERO)  // 00분
                    alarmCalendar.set(Calendar.SECOND, Const.NOTI_SECOND_ZERO)  // 00초
                    alarmCalendar.set(Calendar.MILLISECOND, Const.NOTI_MILLISECOND_ZERO)  // 00초

                } else { // 현재 시각 < 아이템 시각 -> 미래를 의미
                    // 이번달 알람을 설정 + 한달마다 반복
                    val alarmCalendar = Calendar.getInstance()
                    alarmCalendar.set(
                        Calendar.MONTH,
                        currentCalendar.get(Calendar.MONTH)
                    ) // 이번달
                    alarmCalendar.set(
                        Calendar.DAY_OF_MONTH,
                        itemCalendar.get(Calendar.DAY_OF_MONTH)
                    )   // 고정지출 하루전(-1은 위에서 이미 실시함)
                    alarmCalendar.set(Calendar.HOUR_OF_DAY, Const.NOTI_HOUR_OF_DAY_21)  // 21시
                    alarmCalendar.set(Calendar.MINUTE, Const.NOTI_MINUTE_ZERO)  // 00분
                    alarmCalendar.set(Calendar.SECOND, Const.NOTI_SECOND_ZERO)  // 00초
                    alarmCalendar.set(Calendar.MILLISECOND, Const.NOTI_MILLISECOND_ZERO)  // 00초
                }

                val year = alarmCalendar.get(Calendar.YEAR)
                val month = alarmCalendar.get(Calendar.MONTH) + 1
                val day = alarmCalendar.get(Calendar.DAY_OF_MONTH)
                val hour = alarmCalendar.get(Calendar.HOUR_OF_DAY)
                val minute = alarmCalendar.get(Calendar.MINUTE)
                val second = alarmCalendar.get(Calendar.SECOND)


                val intent = Intent(mContext, FixExpenseReceiver::class.java)
                // 고정 지출 이전일 21시00분00초에 실행되는 intent
                val pendingIntent = PendingIntent.getBroadcast(
                    mContext,
                    Const.NOTI_RECEIVER_PENDING_INTENT_REQUEST_CODE,
                    intent,
                    TargetSDKUtil.getFlags()
                )
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmCalendar.timeInMillis,
                    pendingIntent
                )
            }
        }

    }

    // 가계부 기입 요청 알림 설정
    // 매일 21시 DB 확인 및 필요시 알림
    fun setKinyuNotification() {

        // AlarmManagert 생성 (특정 시간을 인식하는 BroadCast 생성 클래스)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, KinyuNotiReceiver::class.java)
        val calendar = Calendar.getInstance()

        // 21시00분00초
        calendar.set(Calendar.HOUR_OF_DAY, Const.NOTI_HOUR_OF_DAY_21)  // 21시
        calendar.set(Calendar.MINUTE, Const.NOTI_MINUTE_ZERO)        // 00분
        calendar.set(Calendar.SECOND, Const.NOTI_SECOND_ZERO)        // 00초
        calendar.set(Calendar.MILLISECOND, Const.NOTI_MILLISECOND_ZERO)

        // 21시00분00초에 실행되는 intent
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            Const.NOTI_RECEIVER_PENDING_INTENT_REQUEST_CODE,
            intent,
            TargetSDKUtil.getFlags()
        )
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        // setInexactRepeating 성능 이슈로 setExact로 변경
//        alarmManager.setInexactRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            pendingIntent
//        )
    }

    // 월말 지출 비교 알림 설정
    // 매월 25일 21시 DB 확인 및 알림
    fun setComparisonExpenseByMonthly() {

        // AlarmManagert 생성 (특정 시간을 인식하는 BroadCast 생성 클래스)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ComparisonExpenseNotiReceiver::class.java)
        val calendar = Calendar.getInstance()

        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // 오늘이 25일이 넘으면, 다음달 25일로 넘김
        if (currentDay > Const.NOTI_DAY_OF_MONTH_25) {
            // 매월 25일 21시00분00초
            calendar.set(Calendar.MONTH, currentMonth + 1)  // 다음달
            calendar.set(Calendar.DAY_OF_MONTH, Const.NOTI_DAY_OF_MONTH_25) // 25일
            calendar.set(Calendar.HOUR_OF_DAY, Const.NOTI_HOUR_OF_DAY_21)  // 21시
            calendar.set(Calendar.MINUTE, Const.NOTI_MINUTE_ZERO)        // 00분
            calendar.set(Calendar.SECOND, Const.NOTI_SECOND_ZERO)        // 00초
            calendar.set(Calendar.MILLISECOND, Const.NOTI_MILLISECOND_ZERO)
        }
        // 오늘이 25일인데, 21시가 넘었을 경우
        else if (currentDay == Const.NOTI_DAY_OF_MONTH_25 && currentHour == Const.NOTI_HOUR_OF_DAY_21) {
            // 21시이후 ~24시까지는 알람을 수행하지 않음
            return
        } else {  // 오늘이 25일을 넘지않으면, 이번달 25일 21시로 설정
            // 매월 25일 21시00분00초
            calendar.set(Calendar.DAY_OF_MONTH, Const.NOTI_DAY_OF_MONTH_25) // 25일
            calendar.set(Calendar.HOUR_OF_DAY, Const.NOTI_HOUR_OF_DAY_21)  // 21시
            calendar.set(Calendar.MINUTE, Const.NOTI_MINUTE_ZERO)        // 00분
            calendar.set(Calendar.SECOND, Const.NOTI_SECOND_ZERO)        // 00초
            calendar.set(Calendar.MILLISECOND, Const.NOTI_MILLISECOND_ZERO)
        }

        // 21시00분00초에 실행되는 intent
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            Const.NOTI_RECEIVER_PENDING_INTENT_REQUEST_CODE,
            intent,
            TargetSDKUtil.getFlags()
        )

        // calendar.getActualMaximum(Calendar.DAY_OF_MONTH)는 매달 달라져야함, 정확히 25일이 아닐 수 있음. 추후 수정 필요
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        // setInexactRepeating 성능 이슈로 setExact로 변경
//        alarmManager.setInexactRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY* calendar.getActualMaximum(
//                Calendar.DAY_OF_MONTH
//            ),
//            pendingIntent
//        )

    }

    // 정기 고정 지출 알림 취소
    fun cancelFixExpenseNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.deleteNotificationChannel(Const.FIX_EXPENSE_CHANNEL_ID)
    }

    // 월말 지출 비교 알림 취소
    fun cancelKinyuNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.deleteNotificationChannel(Const.KINYU_CHANNEL_ID)
    }

    // 월말 지출 비교 알림 취소
    fun cancelComparisonExpenseByMonthly() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.deleteNotificationChannel(Const.COMPARISON_CHANNEL_ID)
    }

}