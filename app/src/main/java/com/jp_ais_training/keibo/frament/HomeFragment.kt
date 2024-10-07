package com.jp_ais_training.keibo.frament

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jp_ais_training.keibo.KeiboApplication
import com.jp_ais_training.keibo.databinding.FragmentHomeBinding
import com.jp_ais_training.keibo.util.Const
import com.jp_ais_training.keibo.adapter.CalendarAdapter
import com.jp_ais_training.keibo.adapter.CalendarItem
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val TAG = this::class.java.simpleName.toString()
    private lateinit var app: KeiboApplication
    private val currentCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        app = requireActivity().application as KeiboApplication

        // 캘린더 레이아웃 작성
        val numberOfWeek = 7
        binding.homeCalendar.calendar.layoutManager = GridLayoutManager(context, numberOfWeek)
        setButtonListener(currentCalendar)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setCalendarLayout(currentCalendar)
    }

    private fun setCalendarLayout(calendar: Calendar) {
        runBlocking {
            val itemDataList = // 해달 날짜 캘린더
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                    setCalendarData(calendar) // 해달 날짜 캘린더
                }

            val totalAmount = // 해달 날짜 캘린더
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                    loadTotalAmount(calendar) // 해달 날짜 캘린더
                }
            setMonth(calendar)
            setCalendar(itemDataList)
            setTotalAmount(totalAmount)
        }
    }

    private fun loadTotalAmount(calendar: Calendar): TotalAmount {
        val yearMonth = SimpleDateFormat("yyyy-MM").format(calendar.time)
        return TotalAmount(
            app.db.loadMonthSumHEI(yearMonth),
            app.db.loadMonthSumHII(yearMonth)
        )
    }

    private fun setCalendarData(paramCalendar: Calendar): ArrayList<CalendarItem> {
        Log.d(TAG, "setCalendarData: start")
        // 기존 데이터 삭제
        val dataSet = ArrayList<CalendarItem>()
        val calendar = paramCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")     // 년월일 날짜 포멧
        val dateNum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)    // 당월의 마지막 날
        val datePadding = calendar.get(Calendar.DAY_OF_WEEK)        // 당월 1일의 요일
        val yearMonthFormat = SimpleDateFormat("yyyy-MM")    // 년월 날짜 포멧
        val yearMonth = yearMonthFormat.format(calendar.time)

        val expenseItemList = app.db.loadDaySumEI(yearMonth)
        val incomeItemList = app.db.loadDaySumII(yearMonth)

        // 1일이 해당하는 요일까지 패딩 (1일이 수요일이라면 일, 월, 화요일이 패딩)
        for (i in 1 until datePadding) {
            dataSet.add(CalendarItem(0, Const.NULL, 0, 0))
        }
        // 실제 캘린더에 들어갈 데이터 작성
        for (i in 1..dateNum) {
            calendar.set(Calendar.DAY_OF_MONTH, i)
            val date = dateFormat.format(calendar.time)
            val incomeItem = incomeItemList.firstOrNull { it.date == date }
            val income = if (incomeItem != null) incomeItem.price!! else 0
            val expenseItem = expenseItemList.firstOrNull { it.date == date }
            val expense = if (expenseItem != null) expenseItem.price!! else 0
            dataSet.add(
                CalendarItem(
                    i,
                    date,
                    income,
                    expense
                )
            )
        }
        return dataSet
    }

    private fun setMonth(calendar: Calendar) {
        binding.homeCalendar.calendarMonth.text =
            (calendar.get(Calendar.MONTH) + 1).toString() + "月"
    }

    private fun setCalendar(itemDataList: ArrayList<CalendarItem>) {
        binding.homeCalendar.calendar.adapter = CalendarAdapter(itemDataList, context)
    }

    private fun setTotalAmount(totalAmount: TotalAmount) {
        binding.homeTotalIncome.text = totalAmount.totalIncome.toString()
        binding.homeTotalExpense.text = totalAmount.totalExpense.toString()
    }

    data class TotalAmount(
        var totalExpense: Int,
        var totalIncome: Int
    )

    private fun setButtonListener(calendar: Calendar) {
        binding.homeCalendar.homePreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            Log.d(TAG, "setButtonListener: " + SimpleDateFormat("yyyy-MM").format(calendar.time))
            setCalendarLayout(calendar)
        }

        binding.homeCalendar.homeNextMonth.setOnClickListener {
            val currentCalendar = Calendar.getInstance()
            if ((calendar.get(Calendar.YEAR) < currentCalendar.get(Calendar.YEAR))
                || (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                        && calendar.get(Calendar.MONTH) < currentCalendar.get(Calendar.MONTH))
            ) {
                calendar.add(Calendar.MONTH, 1)
                setCalendarLayout(calendar)
            }
        }
    }
}