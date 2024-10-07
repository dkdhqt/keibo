package com.jp_ais_training.keibo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jp_ais_training.keibo.db.AppDatabase
import com.jp_ais_training.keibo.model.response.ExpenseItemType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AutoAddFixExpenseReceiver: BroadcastReceiver() {

    private val TAG = this::class.java.simpleName.toString()

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "AutoAddFixExpenseReceiver")

        val prevMonthAsYYYYMM = getPrevMonthAsYYYYMM()
        val db = AppDatabase.getInstance(context!!)!!

        CoroutineScope(Dispatchers.IO).launch {
            val prevMonthFixExpenseList = db.dao().loadEI(prevMonthAsYYYYMM).filter { item ->
                item.type == "fix"
            }
            // 이번달 Calendar 객체
            val currentCalendar = Calendar.getInstance()
            val currentMonthAsInt = currentCalendar.get(Calendar.MONTH) // 이번달
            val lastDayOfCurrentMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) // 이번달의 마지막날

            for (item in prevMonthFixExpenseList) {
                // datetime(String)를 Calendar 객체로 변환
                val itemDatetime = item.datetime
                val cal = convertStringToCalendar(itemDatetime)
                // 이번달로 변경
                cal.set(Calendar.MONTH, currentMonthAsInt)

                val itemDay = cal.get(Calendar.DAY_OF_MONTH)
                if (itemDay > lastDayOfCurrentMonth) {
                    // 아이템의 datetime의 날짜가 현재달의 마지막날보다 큰 경우
                    // 1월 31일 데이터를 2월(28일까지밖에없음)에 추가해야하는 경우
                    // "이번달의 마지막날로 추가하자!"
                    cal.set(Calendar.DAY_OF_MONTH, lastDayOfCurrentMonth)
                } else {
                    cal.set(Calendar.DAY_OF_MONTH, itemDay)
                }

                val newDatetime = convertCalendarToString(cal)

//                val newExpenseItemType = ExpenseItemType(item.expense_item_id, item.sub_category_id, item.name, item.price, newDatetime, item.type)
//                Log.d(TAG, "item.datetime: ${item.datetime}  ${item.sub_category_id}, ${item.name}, ${item.price!!}, $newDatetime")

                // 저번달 고정 지출을 이번달에 추가
                db.dao().insertEI(item.sub_category_id!!, item.name!!, item.price!!, newDatetime)
            }
        }
    }

    private fun convertStringToCalendar(itemDatetime: String?): Calendar {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date: Date = sdf.parse(itemDatetime)
        val cal = Calendar.getInstance()
        cal.time = date

        return cal
    }

    private fun convertCalendarToString(cal: Calendar): String {
        val year = cal.get(Calendar.YEAR)
        val month = (cal.get(Calendar.MONTH) + 1).let {
            if (it < 10) {
                "0$it"
            } else {
                it.toString()
            }
        }
        val day = cal.get(Calendar.DAY_OF_MONTH).let {
            if (it < 10) {
                "0$it"
            } else {
                it.toString()
            }
        }

        return "$year-$month-$day"
    }

    private fun getPrevMonthAsYYYYMM(): String {
        // 이전달 Calendar 객체
        val prevCalendar = Calendar.getInstance()
        prevCalendar.set(Calendar.MONTH, prevCalendar.get(Calendar.MONTH) - 1)

        val prevYear = prevCalendar.get(Calendar.YEAR)
        val prevMonth = (prevCalendar.get(Calendar.MONTH) + 1).let {
            if (it < 10) {
                "0$it"
            } else {
                it.toString()
            }
        }

        return "$prevYear-$prevMonth"
    }
}