package  com.jp_ais_training.keibo.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context)
{
    private val prefs: SharedPreferences = context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE )

    fun setTestData()
    {
        prefs.edit().putBoolean(Const.PREF_TESTDATA_KEY, true).apply()
    }

    fun getTestData(): Boolean
    {
        return prefs.getBoolean(Const.PREF_TESTDATA_KEY, false)
    }

    //----------------------------------------------------------------


    // 이번달 고정지출 자동 추가 확인 여부용 플래그 저장
    fun setAutoAddFixExpenseDate(date: String) {
        prefs.edit().putString(Const.PREF_AUTOADDFIXEXPENSEDATE_KEY, date).apply()
    }

    // 이번달 고정지출 자동 추가 확인 여부용 플래그 출력
    fun getAutoAddFixExpenseDate(): String? {
        return prefs.getString(Const.PREF_AUTOADDFIXEXPENSEDATE_KEY, "")
    }

    fun setIsRunningFixExpenseNoti(isChecked: Boolean) {
        prefs.edit().putBoolean(Const.PREF_FIX_EXPENSE_NOTI_KEY, isChecked).apply()
    }

    fun getIsRunningFixExpenseNoti() : Boolean {
        return prefs.getBoolean(Const.PREF_FIX_EXPENSE_NOTI_KEY, false)
    }

    fun setIsRunningKinyuNoti(isChecked: Boolean) {
        prefs.edit().putBoolean(Const.PREF_KINYU_NOTI_KEY, isChecked).apply()
    }

    fun getIsRunningKinyuNoti() : Boolean {
        return prefs.getBoolean(Const.PREF_KINYU_NOTI_KEY, false)
    }

    fun setIsRunningComparisonExpenseNoti(isChecked: Boolean) {
        prefs.edit().putBoolean(Const.PREF_COMPARISON_EXPENSE_NOTI_KEY, isChecked).apply()
    }

    fun getIsRunningComparisonExpenseNoti() : Boolean {
        return prefs.getBoolean(Const.PREF_COMPARISON_EXPENSE_NOTI_KEY, false)
    }

    // 환율 데이터 저장
    fun setKawaseRate(currentKawaseRate: Float) {
        prefs.edit().putFloat(Const.PREF_KAWASERATE_KEY, currentKawaseRate).apply()
    }
    // 환율 데이터 출력
    fun getKawaseRate() : Float {
        return prefs.getFloat(Const.PREF_KAWASERATE_KEY, 1000.0F)
    }

}