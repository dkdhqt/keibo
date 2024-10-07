package com.jp_ais_training.keibo.frament

import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jp_ais_training.keibo.KeiboApplication
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.activity.MainActivity
import com.jp_ais_training.keibo.databinding.DialogTestBinding
import com.jp_ais_training.keibo.databinding.FragmentSettingsBinding
import com.jp_ais_training.keibo.db.AppDatabase
import com.jp_ais_training.keibo.model.response.LoadSumEI
import com.jp_ais_training.keibo.util.Const
import com.jp_ais_training.keibo.util.NotificationUtil
import com.jp_ais_training.keibo.util.PreferenceUtil
import com.jp_ais_training.keibo.util.TargetSDKUtil
import kotlinx.coroutines.*
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class SettingsFragment : Fragment() {

    private val TAG = this::class.java.simpleName.toString()
    private lateinit var binding:FragmentSettingsBinding

    private lateinit var preferenceUtil: PreferenceUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)

        initSwitchValue()
        initLicenseTextView()
        setClickEvent()

        setTestEvent()

        return binding.root
    }

    // 스위치 상태 초기화
    private fun initSwitchValue() {
        preferenceUtil = if (activity != null) {
            (activity?.application as KeiboApplication).prefs
        } else {
            PreferenceUtil(requireContext())
        }

        // 이전 스위치 상태 확인 및 대입
        val isRunningFixExpenseNoti = preferenceUtil.getIsRunningFixExpenseNoti()
        val isRunningKinyuNoti = preferenceUtil.getIsRunningKinyuNoti()
        val isRunningComparisonExpenseNoti = preferenceUtil.getIsRunningComparisonExpenseNoti()

        binding.swiAll.isChecked= isRunningFixExpenseNoti && isRunningKinyuNoti && isRunningComparisonExpenseNoti
        binding.swiFixExpenseNoti.isChecked= isRunningFixExpenseNoti
        binding.swiKinyuNoti.isChecked= isRunningKinyuNoti
        binding.swiComparisonNoti.isChecked= isRunningComparisonExpenseNoti
    }

    private fun initLicenseTextView() {
        val content = SpannableString(Const.LICENSE_TEXTVIEW_TEXT)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        content.setSpan(ForegroundColorSpan(Color.BLUE), 0, content.length, 0)
        binding.tvLicense.text = content
    }

    // 화면 클릭 이벤트 설정
    private fun setClickEvent() {
        binding.swiAll.setOnClickListener{
            val isChecked = binding.swiAll.isChecked
            binding.swiFixExpenseNoti.isChecked= isChecked
            binding.swiKinyuNoti.isChecked= isChecked
            binding.swiComparisonNoti.isChecked= isChecked

            preferenceUtil.setIsRunningFixExpenseNoti(isChecked)
            preferenceUtil.setIsRunningKinyuNoti(isChecked)
            preferenceUtil.setIsRunningComparisonExpenseNoti(isChecked)

            updateNotification(Const.PREF_FIX_EXPENSE_NOTI_KEY)
            updateNotification(Const.PREF_KINYU_NOTI_KEY)
            updateNotification(Const.PREF_COMPARISON_EXPENSE_NOTI_KEY)
        }

        binding.swiFixExpenseNoti.setOnClickListener{
            val isChecked = binding.swiFixExpenseNoti.isChecked
            preferenceUtil.setIsRunningFixExpenseNoti(isChecked)

            // 각각의 스위치 상태 변화시 switchAll 상태 변화
            binding.swiAll.isChecked= isChecked && binding.swiKinyuNoti.isChecked&& binding.swiComparisonNoti.isChecked

            updateNotification(Const.PREF_FIX_EXPENSE_NOTI_KEY)
        }

        binding.swiKinyuNoti.setOnClickListener{
            val isChecked = binding.swiKinyuNoti.isChecked
            preferenceUtil.setIsRunningKinyuNoti(isChecked)

            // 각각의 스위치 상태 변화시 switchAll 상태 변화
            binding.swiAll.isChecked= binding.swiFixExpenseNoti.isChecked&& isChecked  && binding.swiComparisonNoti.isChecked
            updateNotification(Const.PREF_KINYU_NOTI_KEY)
        }

        binding.swiComparisonNoti.setOnClickListener{
            val isChecked = binding.swiComparisonNoti.isChecked
            preferenceUtil.setIsRunningComparisonExpenseNoti(isChecked)

            // 각각의 스위치 상태 변화시 switchAll 상태 변화
            binding.swiAll.isChecked= binding.swiFixExpenseNoti.isChecked&& binding.swiKinyuNoti.isChecked&& isChecked

            updateNotification(Const.PREF_COMPARISON_EXPENSE_NOTI_KEY)
        }

        // Licenseについて 클릭
        binding.tvLicense.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())

            val inputStream: InputStream = this.resources.openRawResource(R.raw.license)
            val buffer = ByteArray(inputStream.available())
            while (inputStream.read(buffer) !== -1){ }
            val licenseText = String(buffer)
            builder.setMessage(licenseText)
            builder.setNegativeButton("閉じる", object: DialogInterface.OnClickListener {
                override fun onClick(view: DialogInterface?, which: Int) {
                    view?.dismiss()
                }
            })
            val dialog = builder.create()
            dialog.show()


        }

    }

    // 스위치의 현재 상태를 확인해
    private fun updateNotification(flag: String) {

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            val notificationUtil = NotificationUtil(requireContext())
            when(flag) {
                Const.PREF_FIX_EXPENSE_NOTI_KEY -> {
                    if (binding.swiFixExpenseNoti.isChecked) {
                        notificationUtil.setFixExpenseNotification()
                    } else {
                        notificationUtil.cancelFixExpenseNotification()
                    }
                }
                Const.PREF_KINYU_NOTI_KEY -> {
                    if (binding.swiKinyuNoti.isChecked) {
                        notificationUtil.setKinyuNotification()
                    } else {
                        notificationUtil.cancelKinyuNotification()
                    }
                }
                Const.PREF_COMPARISON_EXPENSE_NOTI_KEY -> {
                    if (binding.swiComparisonNoti.isChecked) {
                        notificationUtil.setComparisonExpenseByMonthly()
                    } else {
                        notificationUtil.cancelComparisonExpenseByMonthly()
                    }
                }
            }
        }
    }


    private fun setTestEvent() {
        // 매월 1일 자동 추가
        binding.btnAutoAdd.setOnClickListener {

            val prevMonthAsYYYYMM = getPrevMonthAsYYYYMM()
            val db = AppDatabase.getInstance(requireContext())!!

                runBlocking {
                    val currentList = ArrayList<String>()
                    var prevList = ArrayList<String>()
                    CoroutineScope(Dispatchers.IO).async {
                        val prevMonthFixExpenseList = db.dao().loadEI(prevMonthAsYYYYMM).filter { item ->
                            item.type == "fix"
                        }
                        prevMonthFixExpenseList.forEach {
                            it.datetime?.let { it1 -> prevList.add(it1) }
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
                            currentList.add(newDatetime)
                        }
                    }.await()

                    val dialogBinding: DialogTestBinding = DataBindingUtil
                        .inflate(LayoutInflater.from(context), R.layout.dialog_test, null, false)
                    val dialog = Dialog(requireContext())

                    dialogBinding.prev.text = prevList.toString()
                    dialogBinding.current.text = currentList.toString()

                    dialog.setContentView(dialogBinding.root)
                    dialog.show()
                }


        }
        // 매일 기입요청 알람
        binding.btnKinyuNoti.setOnClickListener {
            setKinyuNoti()
        }
        // 이전달, 이번달 지출 비교 알람
        binding.btnComparisonNoti.setOnClickListener {
            setComparisonNoti()
        }
        // 다음날 고정 지출 알람
        val tomorrow = "2022-05-11"   // for test
        binding.btnFixExpenseNoti.text = "固定支出($tomorrow)"
        binding.btnFixExpenseNoti.setOnClickListener {
            setNotiFixExpense()
        }

        binding.btnKawase.setOnClickListener {
            val currentKawaseRate = PreferenceUtil(requireContext()).getKawaseRate()
            binding.btnKawase.text = "為替レート確認($currentKawaseRate)"
        }
    }

    private fun setKinyuNoti() {
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

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(Const.NOTI_INTENT_TYPE_KEY_WHAT_TO_DO, Const.NOTI_INTENT_TYPE_VALUE_GO_TO_SYOUSAI_PAGE)
        intent.putExtra(Const.KINYU_MAIN_ACTIVITY_EXTRA_YEAR, year)
        intent.putExtra(Const.KINYU_MAIN_ACTIVITY_EXTRA_MONTH, month)
        intent.putExtra(Const.KINYU_MAIN_ACTIVITY_EXTRA_DAY, day)

        val pendingIntent = PendingIntent.getActivity(context, Const.PENDING_INTENT_REQUEST_CODE, intent, TargetSDKUtil.getFlags())

        val contentTitle = Const.KINYU_NOTI_CONTENT_TITLE
        val contentText = "$today\n${Const.KINYU_NOTI_CONTENT_TEXT}"

        val builder = NotificationCompat.Builder(requireContext(), Const.KINYU_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManagerCompat = NotificationManagerCompat.from(requireContext())
        notificationManagerCompat.notify(Const.KINYU_NOTIFICATION_ID, builder.build())

    }
    private fun setComparisonNoti() {
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

        val DB = AppDatabase.getInstance(requireContext())!!

        CoroutineScope(Dispatchers.IO).launch {

            val currentMonthExpenseSum: LoadSumEI =  DB.dao().loadMonthSumEI(current)[0]
            val prevMonthExpenseSum: LoadSumEI =  DB.dao().loadMonthSumEI(prev)[0]
            // 데이터가 있을 경우
            if (currentMonthExpenseSum.price != null && prevMonthExpenseSum.price != null) {
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra(Const.NOTI_INTENT_TYPE_KEY_WHAT_TO_DO, Const.NOTI_INTENT_TYPE_VALUE_GO_TO_BAR_STATISTIC)
                intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                val pendingIntent = PendingIntent.getActivity(context, Const.PENDING_INTENT_REQUEST_CODE, intent, TargetSDKUtil.getFlags())

                val contentTitle = Const.COMPARISON_NOTI_CONTENT_TITLE
                val contentText =
                    "${Const.COMPARISON_NOTI_CONTENT_TEXT_1}${prevMonthExpenseSum.price}円\n" +
                            "${Const.COMPARISON_NOTI_CONTENT_TEXT_2}${currentMonthExpenseSum.price}円\n" +
                            "${Const.COMPARISON_NOTI_CONTENT_TEXT_3}${currentMonthExpenseSum.price - prevMonthExpenseSum.price}${Const.COMPARISON_NOTI_CONTENT_TEXT_4}"

                val builder = NotificationCompat.Builder(requireContext(), Const.COMPARISON_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle())
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)

                val notificationManagerCompat = NotificationManagerCompat.from(requireContext())
                notificationManagerCompat.notify(Const.COMPARISON_NOTIFICATION_ID, builder.build())
            }

        }
    }
    private fun setNotiFixExpense() {
// 해당 리시버가 동작했다는 것은 내일(리시버가 동작한 다음날) 고정 지출이 있고,
        // 이에 대해 알림이 필요하다는 것을 의미
        val tomorrow = "2022-05-11"   // for test

        val DB = AppDatabase.getInstance(requireContext())!!

        CoroutineScope(Dispatchers.IO).launch {
            // $year-$month-$day : YYYY-MM-DD
            val fixExpenseList = DB.dao().loadFixEI(tomorrow)

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            val pendingIntent = PendingIntent.getActivity(
                context,
                Const.PENDING_INTENT_REQUEST_CODE,
                intent,
                TargetSDKUtil.getFlags()
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

            val builder = NotificationCompat.Builder(requireContext(), Const.KINYU_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            val notificationManagerCompat = NotificationManagerCompat.from(requireContext())
            notificationManagerCompat.notify(Const.FIX_EXPENSE_NOTIFICATION_ID, builder.build())    // 한번에 묶어서
//                notificationManagerCompat.notify(index, builder.build())  // 따로따로
        }
    }

    private fun convertStringToCalendar(itemDatetime: String?): Calendar {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.parse(itemDatetime)
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
        prevCalendar.set(Calendar.DAY_OF_MONTH, 1)  // 30, 31일 경우, MONTH(달)을 바꾸더라도 이전달로 넘어가지 않는 문제 발생

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
