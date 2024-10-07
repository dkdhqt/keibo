package com.jp_ais_training.keibo.model.response

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class LoadSumII(
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "price") val price: Int?
)

data class LoadSumEI(
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "price") val price: Int?
)

data class LoadSumSubCategoryEI(
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "price") val price: Int?,
    @ColumnInfo(name = "sub_name") val sub_name: String?,
    @ColumnInfo(name = "main_id") val main_id: Int?
)

data class LoadSumMainCategoryEI(
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "price") val price: Int?,
    @ColumnInfo(name = "main_name") val main_name: String?,
    @ColumnInfo(name = "main_id") val main_id: Int?,
    @ColumnInfo(name = "type") val type: String?
)

data class ResponseItem(
    @ColumnInfo(name = "income_item_id") var income_item_id: Int?,
    @ColumnInfo(name = "expense_item_id") var expense_item_id: Int?,
    @ColumnInfo(name = "main_category_id") var main_category_id: Int?,
    @ColumnInfo(name = "sub_category_id") var sub_category_id: Int?,
    @ColumnInfo(name = "main_category_name") var main_category_name: String?,
    @ColumnInfo(name = "sub_category_name") var sub_category_name: String?,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "price") var price: Int?,
    @ColumnInfo(name = "datetime") var datetime: String?
)

data class ExpenseItemType(
    @ColumnInfo(name = "expense_item_id") val expense_item_id: Int?,
    @ColumnInfo(name = "sub_category_id") val sub_category_id: Int?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "price") val price: Int?,
    @ColumnInfo(name = "datetime") val datetime: String?,
    @ColumnInfo(name = "type") val type: String?
)
