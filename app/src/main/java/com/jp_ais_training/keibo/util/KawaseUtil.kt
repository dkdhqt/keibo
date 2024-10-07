package com.jp_ais_training.keibo.util

import com.google.gson.Gson
import com.jp_ais_training.keibo.util.kawase.KawaseResponseBody
import com.jp_ais_training.keibo.util.kawase.KawaseService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class KawaseUtil {
    private val TAG = this::class.java.simpleName.toString()
    private val baseUrl = "https://quotation-api-cdn.dunamu.com/"
    private val codes = "FRX.KRWJPY"

    fun getCurrentKawaseRate(): Float? {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val kawaseService = retrofit.create(KawaseService::class.java)
        val call = kawaseService.getCurrentKawaseRate(codes)

        val result = Gson().fromJson(call.execute().body()?.get(0), KawaseResponseBody::class.java)

        return result.basePrice
    }
}