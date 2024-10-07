package com.jp_ais_training.keibo.util.kawase

data class KawaseResponseBody (
    val code: String?,
    val currencyCode: String?,
    val currencyName: String?,
    val country: String?,
    val name: String?,
    val date: String?,
    val time: String?,
    val recurrenceCount: Int?,
    val basePrice: Float?,     // 현재 환율(100엔 대비 XXX원)
    val openingPrice: Double?,  // 장시작 환율
    val highPrice: Double?,     // 오늘 최고가?
    val lowPrice: Double?,      // 오늘 최저가?
    val change: String?,
    val changePrice: Double?,
    val cashBuyingPrice: Double?,   // 현찰 살때
    val cashSellingPrice: Double?,  // 현찰 팔때
    val ttBuyingPrice: Double?,
    val ttSellingPrice: Double?,
    val fcSellingPrice: Double?,
    val exchangeCommission: Double?,
    val usDollarRate: Double?,
    val high52wPrice: Double?,
    val high52wDate: String?,
    val low52wPrice: Double?,
    val low52wDate: String?,
    val currencyUnit: Int?,
    val provider: String?,
    val timestamp: Long?,
    val id: Long?,
    val modifiedAt: String?,
    val createdAt: String?,
    val signedChangePrice: Double?,
    val changeRate: Double?,
    val signedChangeRate: Double,
)