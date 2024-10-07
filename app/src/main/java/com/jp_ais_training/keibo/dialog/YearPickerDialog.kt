package com.jp_ais_training.keibo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.jp_ais_training.keibo.databinding.DialogYearPickerBinding
import java.util.*

class YearPickerDialog(context: Context): Dialog(context) {

    private lateinit var binding: DialogYearPickerBinding
    private lateinit var calendar: Calendar
    // 현재 연도로부터 과거 몇년까지 선택가능한가
    private val yearCountLimit = 5

    private var mCurrentYear = -1
    private var mCurrentMonth = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogYearPickerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        initNumberPicker()
        setClickEvent()
        setNumberPickerValueChangeEvent()
    }

    // NumberPicker 초기화
    private fun initNumberPicker() {
        calendar = Calendar.getInstance()                   // 현재 날짜 객체 생성
        val currentYear = calendar.get(Calendar.YEAR)       // 현재 연
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // 현재 월, 0~11 = 1월~12월이기에 +1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)// 현재 일

        binding.npYear.maxValue = currentYear               // 미래 데이터를 검색할 수 없기에 현재 년도를 최대값으로 설정
        binding.npYear.minValue = currentYear - yearCountLimit  // yearCountLimit을 이용해 (현재년도-N)년 ~ 현재년도
        binding.npYear.value = currentYear                      // default는 현재년도
    }

    // 클릭 이벤트 설정
    private fun setClickEvent() {
        binding.btnCancel.setOnClickListener {
            // cancel
        }
        binding.btnConfirm.setOnClickListener {
            // confirm
        }
    }

    // NumberPicker의 ValueChange 이벤트 설정
    private fun setNumberPickerValueChangeEvent() {
        // 연도 NumberPicker ValueChange 이벤트 설정
        binding.npYear.setOnValueChangedListener { picker, oldVal, newVal ->
            mCurrentYear = newVal   // mCurrentYear 변수는 npMonth ValueChange Event에서 활용됨
        }

    }

    // 특정 연월에 해당하는 주간 데이터 추출
    private fun getWeeksOfMonth(year: Int, month: Int) : java.util.ArrayList<String> {
        val calendar = Calendar.getInstance()   // Calendar 객체 생성
        calendar.set(Calendar.YEAR, year)       // 입력받은 연도값 set
        calendar.set(Calendar.MONTH, month - 1) // 입력받은 월값 set, Calendar 객체는 "N월 -> Int"의 경우 N월 -> N-1으로 대입해야 함, 1월~12월 == 0~11
        calendar.set(Calendar.WEEK_OF_MONTH, 1)             // 특정 달의 첫째주를 설정
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // 특정 주의 월요일을 설정

        // 1월의 첫째주 월요일을 찾는 경우, 첫번째 월요일이 이전달에 들어가 있다면 이전연도로 넘어가버리는 오류가 발생
        if (year != calendar.get(Calendar.YEAR)) {   // 첫째주 월요일을 설정하는 과정에서 연도가 바뀐 경우
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.WEEK_OF_MONTH, 2)        // 1+1인 2를 대입하면 이전연도가 포함되지 않은 우리가 구하고자하는 특정월의"첫째주"를 추출하는 것이 가능
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        // 위의 조건문과 비슷한 이유로, 첫째주 월요일이 이전달에 들어가 있는 경우 이전월로 넘어가버리는 오류가 발생
        if (month != calendar.get(Calendar.MONTH) + 1) {    // 첫째주 월요일을 설정하는 과정에서 월이 바뀐 경우
            // N월 - N-1(Int)으로 대칭되기에 이전월로 넘어가지 않고 정상적인 경우라면
            // N : month(parameter), N-1 : calendar.get(Calendar.MONTH) as Int 이기에
            // N != N-1+1 -> N != N 인지를 묻는 조건문이다.
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.WEEK_OF_MONTH, 2)     // 1+1인 2를 대입하면 이전월이 포함되지 않은 우리가 구하고자하는 특정월의 "첫째주"를 추출하는 것이 가능
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        // 이를 통해 추출한 특정 연월의 첫째주 월요일에 대한 날짜 데이터
        val year = calendar.get(Calendar.YEAR)          // 연
        val month = calendar.get(Calendar.MONTH)        // 월: Int
        val day = calendar.get(Calendar.DAY_OF_MONTH)   // 일
        val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)  // 원래라면 해당 변수의 값이 "1"일 경우, 첫째주를 의미하지만, 이전연도, 이전월로 넘어가는 경우에 따라 "2"가 첫째주를 의미하기도 함

        return makeWeeksOfMonthByString(calendar, year, month, day, weekOfMonth)
    }

    // 특정 연월의 첫째주 월요일 데이터를 이용해
    // 실제 npWeek의 displayedValues로 활용될 "주에 대한 기간" ArrayList<String> 추출
    private fun makeWeeksOfMonthByString(calendar: Calendar, year: Int, month: Int, day: Int, weekOfMonth: Int) : java.util.ArrayList<String> {
        val list = java.util.ArrayList<String>()
        list.add("${day}~${day+6}")     // 일주일은 N일~N+6일
        // 첫째주 기간 추가
        var newWeekOfMonth = weekOfMonth    //(variable) val to var weekOfMonth(parameter)

        while (true) {
            newWeekOfMonth++
            calendar.set(Calendar.WEEK_OF_MONTH, newWeekOfMonth)    // 둘째주,셋째주... 설정
            if (month != calendar.get(Calendar.MONTH)) {            // 다음주로 변경했더니 다음달로 넘어가는 경우 (parameter가 Int이기에 "-1"은 불필요)
                // 29~35, 31~37 등 month(parameter)를 기준으로 한 day 숫자 값이 추출됨
                // 이를 해결하기 위한 string 변환

                val dayOfnextMonday = calendar.get(Calendar.DAY_OF_MONTH)

                // 다음달로 넘어갔으나 월요일이 1일인 경우
                // "${N}~${N+6}"형식의 데이터 추가에서 ${N+6}에 특정 달의 말일이 정상적으로 추가 됐기에
                // 별도 작업 불필요 -> break
                if (dayOfnextMonday == 1) break

                var dayOfSundayOfLastWeek = (dayOfnextMonday - 1)   // 다음달의 월요일-1은 이번달의 주 기간의 마지막 일(일요일) ex)29~4, 26~2, 31~6

                val waveIndex = list[list.size - 1].indexOf("~")    // "${N}~${N+6}"의 형식에서 "~"앞의 "${N}"부분을 추출하기 위한 index 변수
                val frontDayOfWave = list[list.size - 1].substring(0, waveIndex) // "${N}~${N+6}"의 형식에 "${N}"
                list[list.size - 1] = "$frontDayOfWave~$dayOfSundayOfLastWeek"  // 이미 입력된 리스트의 마지막 데이터에 적절한 기간 문자열 입력

                break   // 달의 마지막 기간까지 추가했기에 while문 종료
            }

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            list.add("${day}~${day+6}")
        }
        return list
    }

}