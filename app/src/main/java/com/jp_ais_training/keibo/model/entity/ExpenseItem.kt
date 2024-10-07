package com.jp_ais_training.keibo.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.jp_ais_training.keibo.model.entity.SubCategory


@Entity(tableName = "ExpenseItem", foreignKeys = [ForeignKey(
    entity = SubCategory::class,
    parentColumns = ["sub_category_id"],
    childColumns = ["sub_category_id"],
)])
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val expense_item_id: Int,
    val  sub_category_id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price") val price: Int,
    @ColumnInfo(name = "datetime") val datetime: String
)