package com.jp_ais_training.keibo.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.jp_ais_training.keibo.KeiboApplication
import com.jp_ais_training.keibo.R
import kotlinx.coroutines.*
import java.lang.Runnable

//NORMAL 정상, DUPLICATE 중복 , NULL 널
enum class Checker {
    NORMAL, DUPLICATE, NULL
}

class CategoryDialog(private val activity: Activity) {

    val app = activity.application as KeiboApplication

    interface ButtonClickListener {
        fun onClicked(id: Int, name: String, type: String)
    }

    private lateinit var onClickListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickListener = listener
    }

    private fun displayAddCategoryDialog(
        context: Context,
        mainCgId: Int,
        mainCgName: String,
    ) {
        ContextCompat.getMainExecutor(context).execute {

            var inflater = LayoutInflater.from(context)

            val inflatedLayout = inflater.inflate(R.layout.custom_alert_dialog_add_category, null)
            val dialog = AlertDialog.Builder(context).create()
            dialog.setCancelable(false)
            dialog.setView(inflatedLayout)
            inflatedLayout.findViewById<TextView>(R.id.mainCg_name).text = mainCgName
            val edtSubCg: EditText = inflatedLayout.findViewById(R.id.edittext_sub_category)
            val btnBack: Button = inflatedLayout.findViewById(R.id.btn_back)
            val btnAdd: Button = inflatedLayout.findViewById(R.id.btn_add)
            val txtMsg: TextView = inflatedLayout.findViewById(R.id.msg)

            btnBack.setOnClickListener {
                dialog.dismiss()
                callSubCategory(mainCgId, mainCgName)
            }
            btnAdd.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    var checker = Checker.NORMAL

                    if (edtSubCg.text.isNullOrBlank())
                        checker = Checker.NULL
                    else {
                        withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                            // 기존의 삭제 되지 않은 서브카테고리 중 동일한 이름의 카테고리가 있는지
                            val listInt1 = app.db.checkSubCategory(
                                mainCgId,
                                edtSubCg.text.toString().lowercase()
                            )
                            // 기존의 삭제된 서브 서브카테고리 중 동일한 이름의 카테고리가 있는지
                            val listInt2 = app.db.checkSubCategoryWithDeleted(
                                mainCgId,
                                edtSubCg.text.toString().lowercase()
                            )

                            if (listInt1.isNotEmpty())
                                checker = Checker.DUPLICATE

                            if (checker == Checker.NORMAL) {
                                // 논리삭제된 동일명의 서브카테고리가 있다면 업데이트
                                // 없다면 새로 인설트
                                if (listInt2.isEmpty()) {
                                    app.db.insertSubCategory(
                                        mainCgId,
                                        edtSubCg.text.toString().lowercase()
                                    )
                                } else {
                                    app.db.updateDeletedSubCategory(listInt2[0])
                                }
                            }
                        }
                    }

                    when (checker) {
                        Checker.DUPLICATE -> {
                            txtMsg.text = "同じカテゴリが既に存在します。";
                            txtMsg.visibility = View.VISIBLE
                        }
                        Checker.NULL -> {
                            txtMsg.text = "カテゴリ名を入力してくだい。";
                            txtMsg.visibility = View.VISIBLE
                        }
                        else -> {
                            dialog.dismiss()
                            callSubCategory(mainCgId, mainCgName)
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun displayDeleteItemDialog(
        context: Context,
        title: String,
        body: String,
        id: Int,
        type: Int,
        position: Int
    ) {
        var inflater = LayoutInflater.from(context)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val inflatedLayout = inflater.inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setCancelable(false)
        dialog.setView(inflatedLayout)
        inflatedLayout.findViewById<TextView>(R.id.title).text = title
        inflatedLayout.findViewById<TextView>(R.id.body).text = body
        val buttonContainer = inflatedLayout.findViewById<LinearLayout>(R.id.buttonContainer)
        var buttonRow: LinearLayout =
            inflater.inflate(R.layout.custom_alert_dialog_button, null) as LinearLayout
        buttonRow.gravity = Gravity.CENTER
        var buttons = buttonRow.children as Sequence<Button>
        buttons.elementAt(1).text = "Cancle"
        buttons.elementAt(2).text = "OK"
        buttons.elementAt(1).visibility = View.VISIBLE
        buttons.elementAt(2).visibility = View.VISIBLE
        buttons.elementAt(1).setOnClickListener {
            dialog.dismiss()
        }
        buttons.elementAt(2).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                    if (type == 0 || type == 1)
                        app.db.deleteII(id)
                    else
                        app.db.deleteEI(id)

                    onClickListener.onClicked(
                        position,
                        "",
                        "delete"
                    )
                    dialog.dismiss()
                }
            }
        }

        buttonContainer.addView(buttonRow, params)
        dialog.show()
    }


    private fun displayDeleteCategoryDialog(
        context: Context,
        title: String,
        body: String,
        subCategoryId: Int,
        main_category_id: Int,
        main_category_name: String
    ) {
        var inflater = LayoutInflater.from(context)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val inflatedLayout = inflater.inflate(R.layout.custom_alert_dialog, null)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setCancelable(false)
        dialog.setView(inflatedLayout)
        inflatedLayout.findViewById<TextView>(R.id.title).text = title
        inflatedLayout.findViewById<TextView>(R.id.body).text = body
        val buttonContainer = inflatedLayout.findViewById<LinearLayout>(R.id.buttonContainer)
        var buttonRow: LinearLayout =
            inflater.inflate(R.layout.custom_alert_dialog_button, null) as LinearLayout
        buttonRow.gravity = Gravity.CENTER
        var buttons = buttonRow.children as Sequence<Button>
        buttons.elementAt(1).text = "Cancle"
        buttons.elementAt(2).text = "OK"
        buttons.elementAt(1).visibility = View.VISIBLE
        buttons.elementAt(2).visibility = View.VISIBLE
        buttons.elementAt(1).setOnClickListener {
            dialog.dismiss()
        }
        buttons.elementAt(2).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                runBlocking {
                    CoroutineScope(Dispatchers.IO).launch {
                        println("!!!!!!!!!!$subCategoryId")
                        val subCategoryList = app.db.deleteSubCategory(subCategoryId)
                    }.join()
                    dialog.dismiss()
                    callSubCategory(main_category_id, main_category_name)
                }
            }
        }

        buttonContainer.addView(buttonRow, params)
        dialog.show()
    }

    private fun displayMainCategoryDialog(
        context: Context,
        title: String,
        body: String,
        dialogActions: MutableList<DialogAction>
    ) {
        ContextCompat.getMainExecutor(context).execute {

            var inflater = LayoutInflater.from(context)

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val inflatedLayout = inflater.inflate(R.layout.custom_alert_dialog, null)
            val dialog = AlertDialog.Builder(context).create()
            dialog.setCancelable(false)
            dialog.setView(inflatedLayout)
            inflatedLayout.findViewById<TextView>(R.id.title).text = title
            inflatedLayout.findViewById<TextView>(R.id.body).text = body
            val buttonContainer = inflatedLayout.findViewById<LinearLayout>(R.id.buttonContainer)

            for (i in 0 until dialogActions.size / 3) {
                val dialogActionListLimit3 = ArrayList<DialogAction>()
                for (j in 0..2) {
                    if (i * 3 + j < dialogActions.size)
                        dialogActionListLimit3.add(dialogActions.elementAt(i * 3 + j))
                    else
                        break
                }

                var buttonRow: LinearLayout =
                    inflater.inflate(R.layout.custom_alert_dialog_button, null) as LinearLayout
                buttonRow.gravity = Gravity.CENTER
                var buttons = buttonRow.children as Sequence<Button>

                //button.elementAt(i)
                for (i in 0 until dialogActionListLimit3.size) {
                    buttons.elementAt(i).visibility = View.VISIBLE
                    buttons.elementAt(i).text = dialogActionListLimit3.elementAt(i).text
                    buttons.elementAt(i).setOnClickListener(View.OnClickListener {
                        dialogActionListLimit3.elementAt(i).runnable.run()
                        onClickListener.onClicked(
                            dialogActionListLimit3.elementAt(i).id,
                            dialogActionListLimit3.elementAt(i).text,
                            "main"
                        )
                        callSubCategory(
                            dialogActionListLimit3.elementAt(i).id,
                            dialogActionListLimit3.elementAt(i).text
                        )
                        dialog.dismiss()
                    })
                }
                buttonContainer.addView(buttonRow, params)
            }
            dialog.show()

        }
    }

    private fun displaySubCategoryDialog(
        context: Context,
        title: String,
        body: String,
        main_category_id: Int,
        main_category_name: String,
        dialogActions: MutableList<DialogAction>
    ) {
        ContextCompat.getMainExecutor(context).execute {

            var inflater = LayoutInflater.from(context)

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val inflatedLayout = inflater.inflate(R.layout.custom_alert_dialog, null)
            val dialog = AlertDialog.Builder(context).create()
            dialog.setCancelable(false)
            dialog.setView(inflatedLayout)
            inflatedLayout.findViewById<TextView>(R.id.title).text = title
            inflatedLayout.findViewById<TextView>(R.id.body).text = body
            val buttonContainer = inflatedLayout.findViewById<LinearLayout>(R.id.buttonContainer)

            for (i in 0..dialogActions.size / 3) {
                val dialogActionListLimit3 = ArrayList<DialogAction>()
                for (j in 0..2) {
                    if (i * 3 + j < dialogActions.size)
                        dialogActionListLimit3.add(dialogActions.elementAt(i * 3 + j))
                    else
                        break
                }

                var buttonRow: LinearLayout =
                    inflater.inflate(R.layout.custom_alert_dialog_button, null) as LinearLayout
                buttonRow.gravity = Gravity.CENTER
                var buttons = buttonRow.children as Sequence<Button>
                //button.elementAt(i)
                for (i in 0 until dialogActionListLimit3.size) {
                    buttons.elementAt(i).visibility = View.VISIBLE
                    buttons.elementAt(i).text = dialogActionListLimit3.elementAt(i).text
                    buttons.elementAt(i).setOnClickListener(View.OnClickListener {
                        dialogActionListLimit3.elementAt(i).runnable.run()
                        onClickListener.onClicked(
                            dialogActionListLimit3.elementAt(i).id,
                            dialogActionListLimit3.elementAt(i).text,
                            "sub"
                        )
                        dialog.dismiss()
                    })
                    buttons.elementAt(i).setOnLongClickListener {
                        displayDeleteCategoryDialog(
                            context,
                            "注意",
                            buttons.elementAt(i).text.toString() + "カテゴリを削除しますか？",
                            dialogActionListLimit3.elementAt(i).id,
                            main_category_id,
                            main_category_name
                        )
                        dialog.dismiss()
                        return@setOnLongClickListener true
                    }
                }
                if (i == dialogActions.size / 3) {
                    val onClickListener = View.OnClickListener {
                        displayAddCategoryDialog(context, main_category_id, main_category_name)
                        dialog.dismiss()
                    }

                    if (buttons.elementAt(2).visibility == View.VISIBLE) {
                        buttonContainer.addView(buttonRow, params)
                        buttons.elementAt(0).text = "+"
                        buttons.elementAt(0).setOnClickListener(onClickListener)
                        buttons.elementAt(1).visibility = View.INVISIBLE
                        buttons.elementAt(2).visibility = View.INVISIBLE
                        buttonContainer.addView(buttonRow, params)
                    } else {
                        if (buttons.elementAt(0).visibility == View.INVISIBLE) {
                            buttons.elementAt(0).text = "+"
                            buttons.elementAt(0).setOnClickListener(onClickListener)
                            buttons.elementAt(0).visibility = View.VISIBLE
                            buttonContainer.addView(buttonRow, params)
                        } else if (buttons.elementAt(1).visibility == View.INVISIBLE) {
                            buttons.elementAt(1).text = "+"
                            buttons.elementAt(1).setOnClickListener(onClickListener)
                            buttons.elementAt(1).visibility = View.VISIBLE
                            buttonContainer.addView(buttonRow, params)
                        } else if (buttons.elementAt(2).visibility == View.INVISIBLE) {
                            buttons.elementAt(2).text = "+"
                            buttons.elementAt(2).setOnClickListener(onClickListener)
                            buttons.elementAt(2).visibility = View.VISIBLE
                            buttonContainer.addView(buttonRow, params)
                        }
                    }
                } else
                    buttonContainer.addView(buttonRow, params)
            }
            dialog.show()

        }
    }


    fun callSubCategory(main_category_id: Int, main_category_name: String) {
        val dialogActions = mutableListOf<DialogAction>()
        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    val subCategoryList = app.db.loadSubCategory(main_category_id)
                    subCategoryList.forEach { subCategory ->
                        dialogActions.add(
                            DialogAction(
                                subCategory.sub_category_id,
                                subCategory.sub_category_name,
                                Runnable {
                                    //액티비티에 네임 보내기 필요에 따라서 id도
                                    // 콜 서브 카테고리 다이얼 로크
                                })
                        )
                    }
                }.join()
                displaySubCategoryDialog(
                    activity,
                    "サブカテゴリ",
                    "カテゴリの追加はプラスボタンを押してください",
                    main_category_id,
                    main_category_name,
                    dialogActions
                )
            }
        }
    }


    fun callMainCategory(iType: Int) {
        val dialogActions = mutableListOf<DialogAction>()
        var type = ""
        type = if (iType == 2)
            "fix"
        else
            "flex"

        CoroutineScope(Dispatchers.IO).launch {
            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    val mainCategoryList = app.db.loadMainCategory(type)
                    mainCategoryList.forEach { mainCategory ->
                        dialogActions.add(
                            DialogAction(
                                mainCategory.main_category_id,
                                mainCategory.main_category_name,
                                Runnable {
                                })
                        )
                    }
                }.join()
                displayMainCategoryDialog(
                    activity,
                    "メインカテゴリ",
                    "メインカテゴリを選択してください。",
                    dialogActions
                )
            }
        }
    }

    fun callDeleteItemDialog(id: Int, type: Int, position: Int) {
        displayDeleteItemDialog(activity, "注意", "削除しますか？", id, type, position)
    }

}

class DialogAction(var id: Int, var text: String, var runnable: Runnable)