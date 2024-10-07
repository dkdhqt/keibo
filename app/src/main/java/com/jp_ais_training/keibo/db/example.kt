package com.jp_ais_training.keibo.db

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*


class ExActivity : AppCompatActivity() {

    private lateinit var DB: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DB = AppDatabase.getInstance(this)!!

        //사용시 CoroutineScope 필요
        CoroutineScope(Dispatchers.IO).async {
            var data = DB.dao().loadWeekSumEI("2022")
        }

        //testSet : 한번만 사용할 것 ,  1~9월/1일~27일 까지 랜덤으로 Insert
        testSet()

    }
    private fun testSet(){
        CoroutineScope(Dispatchers.IO).async {

            val random = Random()

            DB.dao().insertMainCategory()

            for (i in 1 until 20) {
                val main = random.nextInt(9)+1
                DB.dao().insertSubCategory(main, "sub"+i.toString())
            }
            for (i in 1 until 500){
                val month = random.nextInt(9)+1
                val dayF = random.nextInt(3)
                val dayN = random.nextInt(7)+1
                val sub = random.nextInt(15)+1
                val typeR = random.nextInt(2)
                var type: String = if(typeR == 1){
                    "flex"
                }else{
                    "fix"
                }
                DB.dao().insertII(type,"test"+i.toString(),100,
                    "2022-0"+month.toString()+"-"+dayF.toString()+dayN.toString())

                DB.dao().insertEI(sub,"test"+i.toString(),100,
                    "2022-0"+month.toString()+"-"+dayF.toString()+dayN.toString())
            }
        }
    }
}
