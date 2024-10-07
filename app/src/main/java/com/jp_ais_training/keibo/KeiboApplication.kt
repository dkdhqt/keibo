package com.jp_ais_training.keibo

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import com.jp_ais_training.keibo.db.AppDatabase
import com.jp_ais_training.keibo.db.DAO
import com.jp_ais_training.keibo.receiver.AutoAddFixExpenseReceiver
import com.jp_ais_training.keibo.receiver.ComparisonExpenseNotiReceiver
import com.jp_ais_training.keibo.util.*
import kotlinx.coroutines.*
import java.util.*


open class KeiboApplication: Application() {

    private val TAG = this::class.java.simpleName.toString()
    lateinit var db: DAO
    lateinit var prefs: PreferenceUtil

    override fun onCreate() {
        super.onCreate()

        db = AppDatabase.getInstance(this)?.dao()!!
        prefs = PreferenceUtil(applicationContext)
        if (!prefs.getTestData()) {
            testSet()
            prefs.setTestData()
        } else {
            Log.d("testSet", "true")
        }

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            val notificationUtil = NotificationUtil(this)

            // Notification Channel 생성
            notificationUtil.createChannels()

            // 정기 고정 지출 알림 생성
            notificationUtil.setFixExpenseNotification()
            prefs.setIsRunningFixExpenseNoti(true)
            // 가계부 기입 요청 알림 생성
            notificationUtil.setKinyuNotification()
            prefs.setIsRunningKinyuNoti(true)
            // 월말 지출 비교 알림 생성
            notificationUtil.setComparisonExpenseByMonthly()
            prefs.setIsRunningComparisonExpenseNoti(true)
        }

        // format:YYYY-MM
        val autoAddFixExpenseDate = prefs.getAutoAddFixExpenseDate()
        val currentMonth = getCurrentMonth()

        Log.d(TAG, "autoAddFixExpenseDate: $autoAddFixExpenseDate, currentMonth: $currentMonth")
        if (autoAddFixExpenseDate.isNullOrEmpty() || autoAddFixExpenseDate != currentMonth) {
            // 알람설정이 한번도 실행된적 없거나, 이번달에 고정지출 자동추가를 진행한적이 없는 경우
            // 매월 1일 고정지출 자동 추가 기능
            AlarmUtil(this).setAutoAddFixExpense()
            prefs.setAutoAddFixExpenseDate(currentMonth) // 이번달 고정지출 자동추가 설정 완료이기에 다시 실행되지 않도록 함
        }

        refreshKawaseRate()
    }

    private fun getCurrentMonth(): String {
        val currentCalendar = Calendar.getInstance()
        val year = currentCalendar.get(Calendar.YEAR)
        val month = (currentCalendar.get(Calendar.MONTH) + 1).let {
            if (it < 10) {
                "0$it"
            } else {
                it.toString()
            }
        }

        return "$year-$month"
    }

    private fun refreshKawaseRate() {
        var currentKawaseRate: Float? = null
        runBlocking {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                currentKawaseRate = KawaseUtil().getCurrentKawaseRate()
            }
            currentKawaseRate?.let {
                Log.d(TAG, "currentKawaseRate: $currentKawaseRate")
                prefs.setKawaseRate(it)
            }
        }
    }

    fun testSet() {
        CoroutineScope(Dispatchers.IO).async {

            val random = Random()

            db.insertMainCategory()

            for (i in 1 until 20) {
                val main = random.nextInt(9) + 1
                db.insertSubCategory(main, "sub" + i.toString())
            }
            for (i in 1 until 500) {
                val month = random.nextInt(9) + 1
                val dayF = random.nextInt(3)
                val dayN = random.nextInt(7) + 1
                val sub = random.nextInt(15) + 1
                val typeR = random.nextInt(2)
                var type = ""
                if (typeR == 1) {
                    type = "flex"
                } else {
                    type = "fix"
                }
                db.insertII(
                    type, "test" + i.toString(), 50,
                    "2022-0" + month.toString() + "-" + dayF.toString() + dayN.toString()
                )

                db.insertEI(
                    sub, "test" + i.toString(), 100,
                    "2022-0" + month.toString() + "-" + dayF.toString() + dayN.toString()
                )
            }
        }
    }
}