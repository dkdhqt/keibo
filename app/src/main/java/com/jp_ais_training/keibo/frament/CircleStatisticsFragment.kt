package com.jp_ais_training.keibo.frament

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.jp_ais_training.keibo.KeiboApplication
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.adapter.CircleStatisticsExpandableListAdapter
import com.jp_ais_training.keibo.databinding.FragmentCircleStatisticsBinding
import com.jp_ais_training.keibo.util.PreferenceUtil
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class CircleStatisticsFragment : Fragment() {
    private var mBinding: FragmentCircleStatisticsBinding? = null
    private val binding get() = mBinding!!

    //원그래프 선언
    private lateinit var pieChart: PieChart
    //원그래프 항목
    private var yValues = ArrayList<PieEntry>()
    //월별 이동 처리 기준
    private var dateStandard = 0
    //칼랜더 변수 선언
    val cal = Calendar.getInstance()
    //DB 선언
    private lateinit var app: KeiboApplication

    //DB 데이터 처리 코루틴
    private var job: Job = Job()

    //문자열 취득용 변수
    private var mainCategoryName = ""
    private var mainSumBundle = ""
    private var subCategoryName = ""
    private var subSumBundle = ""

    //확장리스트 데이터 클래스
    data class MenuTitle(var title: String, var price: String, var index: Int)
    data class MenuSpecific(var title: String, var detail: String?)

    //확장리스트 변수
    private var parentList = mutableListOf<MenuTitle>()
    private var childValueList = mutableListOf<MenuSpecific>()
    private var childList = mutableListOf<MutableList<MenuSpecific>>()

    //대분류 항목 리스트
    private val nameSetMainCategory = arrayOf(
        "公課金,","生活(固定),","その他(固定),",
        "食費,","生活(変動),","余暇,",
        "文化,","自己開発,","その他(変動)"
    )

    //원그래프 항목 색상 리스트
    private val colorOfCircleContents = listOf(
        R.color.circle1, R.color.circle2, R.color.circle3, R.color.circle4, R.color.circle5,
        R.color.circle6, R.color.circle7, R.color.circle8, R.color.circle9
    )

    //최초 true  -> 엔화
    //    false -> 원화
    var isJPY = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as KeiboApplication
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCircleStatisticsBinding.inflate(inflater, container, false)
        binding.noDataLayout.visibility = View.INVISIBLE

        //월별 이동 처리 기준
        dateStandard = 0

        //날짜 형식 지정
        cal.time = Date()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")

        //원그래프
        pieChart = binding.pieChart
        clearPieChart()

        //최초 기간 표시
        binding.circleKikanTv.setText(
            df.format(cal.time).substring(0,4) + "年" + df.format(cal.time).substring(5,7)+"月"
        )

        //다음달 통계 표시 버튼 최초 숨김
        binding.naviYokugetsuBtn.visibility = View.INVISIBLE
        binding.naviYokugetsuBtn.isEnabled = false

        getDataFromDB(df.format(cal.time).substring(0,7))

        //버튼 클릭 이벤트 -----------------------------------------------------------------------------------------
        //전달 통계 표시 버튼
        binding.naviSengetsuBtn.setOnClickListener(){
            //실행중인 코루틴 취소
            job.cancel()

            //DB 데이터 초기화
            clearPieChart()
            clearExpandableList()

            moveLastMonth()

            //전달 기간 표시
            cal.add(Calendar.MONTH, -1)
            binding.circleKikanTv.setText(
                df.format(cal.time).substring(0,4) + "年" + df.format(cal.time).substring(5,7) + "月"
            )
            getDataFromDB(df.format(cal.time).substring(0,7))
        }
        //다음달 통계 표시 버튼 -> dateStandard = 0이 될 경우 Invisible 처리
        binding.naviYokugetsuBtn.setOnClickListener(){
            //실행중인 코루틴 취소
            job.cancel()

            //DB 데이터 초기화
            clearPieChart()
            clearExpandableList()

            moveNextMonth()

            //다음달 기간 표시
            cal.add(Calendar.MONTH, 1)
            if(dateStandard == 0){
                binding.circleKikanTv.setText(
                    df.format(cal.time).substring(0,4) + "年" + df.format(cal.time).substring(5,7)+"月"
                )
                getDataFromDB(df.format(cal.time).substring(0,7))
            }else{
                binding.circleKikanTv.setText(
                    df.format(cal.time).substring(0,4) + "年" + df.format(cal.time).substring(5,7) + "月"
                )
                getDataFromDB(df.format(cal.time).substring(0,7))
            }
        }
        binding.tsukaKiriKaeBtn.setOnClickListener(){
            if(isJPY){
                //실행중인 코루틴 취소
                job.cancel()

                //DB 데이터 초기화
                clearPieChart()
                clearExpandableList()

                //원화 설정
                isJPY = false
                binding.tsukaKiriKaeBtn.text = "￦"

                getDataFromDB(df.format(cal.time).substring(0,7))

            }else{
                //실행중인 코루틴 취소
                job.cancel()

                //DB 데이터 초기화
                clearPieChart()
                clearExpandableList()

                //엔화설정
                isJPY = true
                binding.tsukaKiriKaeBtn.text = "円"

                getDataFromDB(df.format(cal.time).substring(0,7))
            }
        }

        return binding.root
    }

    //원그래프 옵션 설정
    private fun setPieChartOption(){
        binding.root.post{
            pieChart.setUsePercentValues(true)
            pieChart.description.isEnabled = false
            pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
            pieChart.dragDecelerationFrictionCoef = 0.95f
            pieChart.setEntryLabelTextSize(0f)
            pieChart.isDrawHoleEnabled = false
            pieChart.setHoleColor(Color.WHITE)
            pieChart.transparentCircleRadius = 61f
            pieChart.legend.isEnabled = true //하단 색항목 리스트
            pieChart.legend.textSize = 12f
            pieChart.animateXY(500, 500) //초기 애니메이션 설정
            pieChart.setNoDataText(" ")//최초 표시되는 no chart data available 텍스트
        }
    }

    //원그래프 항목 추가
    private fun setPieChartItem(setPieItem: String, mainCategoryName: String) {
        val arrPrice = setPieItem.split(",")
        var arrName = mainCategoryName.split(",")

        for (i in 0 until arrName.size-1){
            yValues.add(PieEntry(arrPrice[i].toFloat(), arrName[i]))

        }
    }

    //원그래프 데이터 세팅
    private fun setPieChartDataSet(){
        val dataSet = PieDataSet(yValues, "")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.setColors(colorOfCircleContents.toIntArray(), context)

        val data = PieData(dataSet)
        data.setValueTextSize(15f);
        data.setValueTextColor(Color.WHITE);

        pieChart.data = data
    }

    //원그래프 데이터 초기화
    private fun clearPieChart(){
        yValues.clear()
        pieChart.invalidate()
        pieChart.clear()
    }

    //전달 이동
    private fun moveLastMonth() {
        //초기화면 기준 0 전달 이동시 -1 다음달 이동 시 +1
        dateStandard += -1

        if(dateStandard < 0){
            binding.naviYokugetsuBtn.visibility = View.VISIBLE
            binding.naviYokugetsuBtn.isEnabled = true
        }
    }

    //다음달 이동
    private fun moveNextMonth() {
        if(dateStandard == -1){
            dateStandard += 1
            binding.naviYokugetsuBtn.visibility = View.INVISIBLE
            binding.naviYokugetsuBtn.isEnabled = false
        }else{
            dateStandard += 1
        }
    }

    //메인 카테고리 기준 DB 결과 값 가격 추출  <--  "yyyy-mm"형식 날짜 입력
    private fun getDataFromDB(setDate: String){
        //DB 데이터 가져오기
        job = CoroutineScope(Dispatchers.Main).launch{
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                //DB 데이터 취득
                var getSumMainCategory = app.db.loadMonthSumMainCategoryEI(setDate)
                var getSubCategory = app.db.loadMonthSumSubCategoryEI(setDate)

                //문자열 취득 변수 초기화
                mainCategoryName = ""
                mainSumBundle = ""
                subCategoryName = ""
                subSumBundle = ""

                //서브 카테고리 데이터 저장 배열
                var arrSub : List<String>

                //초기 string 변수 선언
                var str_data = getSumMainCategory.toString()
                var str_subData = getSubCategory.toString()

                //공백 제거
                str_data = str_data.replace(" ", "")
                str_subData = str_subData.replace(" ", "")

                //문자열 취득용
                val nameSetOfDBData = arrayOf(
                    "公課金,main_id=1,type=fix","生活,main_id=2,type=fix","その他,main_id=3,type=fix",
                    "食費,main_id=4,type=flex","生活,main_id=5,type=flex","余暇,main_id=6,type=flex",
                    "文化,main_id=7,type=flex","自己開発,main_id=8,type=flex","その他,main_id=9,type=flex"
                )

                //메인 카테고리명 취득
                for(i in 0..8){
                    if(str_data.contains(nameSetOfDBData[i])){
                        mainCategoryName = mainCategoryName.plus(nameSetMainCategory[i])
                    }
                }

                //메인 카테고리 문자열 변경
                str_data = str_data.replace("LoadSumMainCategoryEI(date=", "")
                str_data = str_data.replace("$setDate,price=", "")
                str_data = str_data.replace(",main_name=公課金,main_id=1,type=fix)", "")
                str_data = str_data.replace(",main_name=生活,main_id=2,type=fix)", "")
                str_data = str_data.replace(",main_name=その他,main_id=3,type=fix)", "")
                str_data = str_data.replace(",main_name=食費,main_id=4,type=flex)", "")
                str_data = str_data.replace(",main_name=生活,main_id=5,type=flex)", "")
                str_data = str_data.replace(",main_name=余暇,main_id=6,type=flex)", "")
                str_data = str_data.replace(",main_name=文化,main_id=7,type=flex)", "")
                str_data = str_data.replace(",main_name=自己開発,main_id=8,type=flex)", "")
                str_data = str_data.replace(",main_name=その他,main_id=9,type=flex)", "")
                str_data = str_data.replace("[", "")
                str_data = str_data.replace("]", "")
                mainSumBundle = str_data

                //서브 카테고리 문자열 변경
                str_subData = str_subData.replace(" ","")
                str_subData = str_subData.replace("[","")
                str_subData = str_subData.replace("]","")
                str_subData = str_subData.replace("LoadSumSubCategoryEI","")
                str_subData = str_subData.plus(",")
                str_subData = str_subData.replace("(","")

                arrSub = str_subData.split("),")

                //데이터 유무에 따라 화면 처리
                if(mainSumBundle.isNotEmpty()){
                    setExistDataCase()

                    //원그래프 항목, 옵션, 데이터 설정
                    setPieChartItem(mainSumBundle, mainCategoryName)
                    setPieChartOption()
                    setPieChartDataSet()

                    //확장리스트 데이터 설정
                    setExpandableList(mainCategoryName, mainSumBundle, arrSub)
                }else{
                    setBlankCase()
                }
            }
        }
    }

    //확장리스트 데이터 세팅
    private fun setExpandableList(
        getMainName:String,
        getMainPrice:String,
        setSub:List<String>
    )
    {
        //환율 적용 변수
        val rate = ((PreferenceUtil(requireContext()).getKawaseRate())*0.01).toFloat()

        val arrMainName = getMainName.split(",")
        val arrMainPrice = getMainPrice.split(",").toMutableList()

        //원화로 표시할 경우 환율 적용
        if(!isJPY && arrMainPrice.isNotEmpty()){
            for (i in 0 until arrMainPrice.size){
                arrMainPrice[i] = (arrMainPrice[i].toFloat()*rate).toInt().toString()
            }
        }

        //대분류 세팅
        for(i in arrMainPrice.indices){
            parentList.add(MenuTitle(arrMainName[i], arrMainPrice[i], i))
        }

        //소분류 항목명, 가격 설정
        for(i in 1..9){
            childValueList.clear()
            for(j in 0 until setSub.size-1){
                if(setSub[j].contains("main_id=$i")){
                    var bundleArr = setSub[j].split(",").toMutableList()

                    //원화로 표시할 경우 환율 적용
                    if(!isJPY && bundleArr.isNotEmpty()){
                        var bundleArrOfKRW =
                            "price=" + ((bundleArr[1].replace("price=","")).toFloat()*rate).toInt().toString()

                        bundleArr[1] = bundleArrOfKRW
                    }
                    childValueList.add(
                        MenuSpecific(
                            bundleArr[2].replace("sub_name=",""),
                            bundleArr[1].replace("price=","")
                        )
                    )

                }
            }
            //소분류 세팅
            if(childValueList.toMutableList().isNotEmpty()){
                childList.add(childValueList.toMutableList())
            }
        }
        //어댑터 세팅
        var circleStatisticsExpandableListAdapter = CircleStatisticsExpandableListAdapter(requireContext(), parentList.toMutableList(), childList.toMutableList())
        binding.root.post{
            binding.expandableListView.setAdapter(circleStatisticsExpandableListAdapter)
        }
    }

    //확장리스트 초기화
    private fun clearExpandableList(){
        binding.expandableListView.invalidate()
        parentList.clear()
        childList.clear()
        childValueList.clear()
    }

    //데이터 없는 경우 화면처리
    private fun setBlankCase(){
        binding.root.post{
            binding.noDataLayout.visibility = View.VISIBLE
            binding.percentTv.visibility = View.INVISIBLE
            binding.pieChart.visibility = View.INVISIBLE
            binding.tsukaKiriKaeBtn.visibility = View.INVISIBLE
            binding.expandableListView.visibility = View.INVISIBLE
        }
    }

    //데이터 있는 경우 화면처리
    private fun setExistDataCase(){
        binding.root.post{
            binding.noDataLayout.visibility = View.INVISIBLE
            binding.percentTv.visibility = View.VISIBLE
            binding.pieChart.visibility = View.VISIBLE
            binding.tsukaKiriKaeBtn.visibility = View.VISIBLE
            binding.expandableListView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        //원그래프 데이터 초기화
        clearPieChart()

        //확장리스트 초기화
        clearExpandableList()

        //월별 이동 처리 기준 초기화
        dateStandard = 0

        mBinding = null

        super.onDestroyView()
    }
}
