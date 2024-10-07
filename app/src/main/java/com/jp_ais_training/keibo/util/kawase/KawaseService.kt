package com.jp_ais_training.keibo.util.kawase

import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface KawaseService {
    @GET("v1/forex/recent")
    fun getCurrentKawaseRate(
        @Query("codes")
        codes:String
    )
            : Call<JsonArray>
}