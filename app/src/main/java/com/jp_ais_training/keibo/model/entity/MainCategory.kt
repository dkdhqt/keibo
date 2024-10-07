package com.jp_ais_training.keibo.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MainCategory")
data class MainCategory(
    @PrimaryKey val main_category_id: Int,
    @ColumnInfo(name = "main_category_name") val main_category_name: String,
    @ColumnInfo(name = "expense_type") val expense_type: String
)