package com.jp_ais_training.keibo.frament

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.jp_ais_training.keibo.KeiboApplication
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.databinding.FragmentBarStatisticsBinding
import com.jp_ais_training.keibo.dialog.YearPickerDialog
import com.jp_ais_training.keibo.model.response.LoadSumEI
import com.jp_ais_training.keibo.model.response.LoadSumII
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BarStatisticsFragment : Fragment() {
    private var data_ei: List<LoadSumEI>? = null
    private var data_ii: List<LoadSumII>? = null
    private var _binding: FragmentBarStatisticsBinding? = null
    private val binding get() = _binding!!
    lateinit var app: KeiboApplication
    private var flag:Boolean= true
    private var year = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBarStatisticsBinding.inflate(inflater, container, false)
        app = requireActivity().application as KeiboApplication

        //캘린더 변수 선언
        val cal = Calendar.getInstance()
        //날짜 형식 지정
        cal.time = Date()
        year = cal.get(Calendar.YEAR)

        //최초 기간 표시
        binding.dateYearBtn.text = "${year}年"

        dateButton()
        dbDataSet()
        eiButton()
        iiButton()
        return binding.root
    }

    private fun dateButton() {
        binding.dateLeftBtn.setOnClickListener() {
            year--
            binding.dateYearBtn.text = "${year}年"
            dbDataSet()
        }

        binding.dateRightBtn.setOnClickListener() {
            year++
            binding.dateYearBtn.text = "${year}年"
            dbDataSet()
        }

        binding.dateYearBtn.setOnClickListener() {
            showYearPickerDialog()
        }

    }

    private fun showYearPickerDialog() {
        val numberSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
            val month = month + 1
//            val date = makeDateString(dayOfMonth, month, year)
//            binding.btnDatePicker.text = date
        }

        val style = AlertDialog.THEME_HOLO_LIGHT

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val numberPickerDialog = YearPickerDialog(requireContext())
        numberPickerDialog.show()
    }

    private fun eiButton() {
        binding.eiButton.setOnClickListener() {
            flag = true
            barChart()
        }
    }

    private fun iiButton() {
        binding.iiButton.setOnClickListener() {
            flag = false
            barChart()
        }
    }

    private fun dbDataSet() {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch() {
                data_ei = app.db.loadMonthSumEI(year.toString())
                data_ii = app.db.loadMonthSumII(year.toString())
            }.join()
            barChart()
        }
    }

    private fun barChart() {
        val barchart: BarChart = binding.barchart// barChart 생성
        val entries = ArrayList<BarEntry>()
        var max = 0f

        if(flag==true){
            for (i in 0..11) {
                var month : Float
                var price : Float

                if(i<data_ei!!.size){
                    month = data_ei!!.get(i).date!!.substring(5,7).toFloat() // 2022-01
                    price = data_ei!!.get(i).price!!.toFloat()
                    entries.add(BarEntry(month ,price))
                }else{
                    month = i.toFloat()+1f
                    price = 0f
                    entries.add(BarEntry(month ,price))
                }

                if (max < price!!){
                    max = price+1000f
                }
            }
        }else{
            for (i in 0..11) {
                var month : Float
                var price : Float

                if(i<data_ii!!.size){
                    month = data_ii!!.get(i).date!!.substring(5,7).toFloat() // 2022-01
                    price = data_ii!!.get(i).price!!.toFloat()
                    entries.add(BarEntry(month ,price))
                }else{
                    month = i.toFloat()+1f
                    price = 0f
                    entries.add(BarEntry(month ,price))
                }

                if (max < price!!){
                    max = price+1000f
                }
            }
        }
        barchart.run {
            description.isEnabled = false // 차트 옆에 별도로 표기되는 description을 안보이게 설정 (false)
            setMaxVisibleValueCount(12) // 최대 보이는 그래프 개수를 12개로 지정
            setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
            setDrawBarShadow(false) //그래프의 그림자
            setDrawGridBackground(false)//격자구조 넣을건지,

            axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                axisMaximum = max //지출금액 위에 선을 그리기 위해 맥시멈값 설정 max = price+1000f
                axisMinimum = 0f // 최소값 0
                granularity = 1000f// 1000 단위마다 선을 그리려고 설정.
                setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                setDrawGridLines(true) //격자 라인 활용
                setDrawAxisLine(false) // 축 그리기 설정
                axisLineColor = ContextCompat.getColor(context, R.color.teal_200) // 축 색깔 설정
                gridColor = ContextCompat.getColor(context, R.color.purple_200) // 축 아닌 격자 색깔 설정
                textColor = ContextCompat.getColor(context, R.color.black) // 라벨 텍스트 컬러 설정
                textSize = 10f //라벨 텍스트 크기
                setBackgroundColor(Color.WHITE)
            }
            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X축을 아래에다가 둔다.
                granularity = 0.5f // 1 단위만큼 간격 두기
                setDrawAxisLine(true) // 축 그림
                setDrawGridLines(false) // 격자
                textColor = ContextCompat.getColor(context, R.color.black) //라벨 색상
                textSize = 10f // 텍스트 크기
                valueFormatter = MyXAxisFormatter() // X축 라벨값(밑에 표시되는 글자) 바꿔주기 위해 설정
                setLabelCount(12, false) //x축 라벨 갯수 지정
            }
            axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 해줌.
            setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
            animateY(700) // 밑에서부터 올라오는 애니매이션 적용
            legend.isEnabled = false //차트 범례 설정
        }
        var set = BarDataSet(entries, "DataSet") // 데이터셋 초기화
        set.color = ContextCompat.getColor(requireContext(), R.color.blue) // 바 그래프 색 설정

        val dataSet: ArrayList<IBarDataSet> = ArrayList()
        dataSet.add(set)
        val data = BarData(dataSet)
        data.barWidth = 0.5f //막대 너비 설정
        barchart.run {
            this.data = data //차트의 데이터를 data로 설정해줌.
            setFitBars(true)
            invalidate()
        }
    }
    inner class MyXAxisFormatter : ValueFormatter() {
        private val days =
            arrayOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt() - 1) ?: value.toString()
        }
    }
}

