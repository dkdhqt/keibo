package com.jp_ais_training.keibo.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "SubCategory", foreignKeys = [ForeignKey(
    entity = MainCategory::class,
    parentColumns = ["main_category_id"],
    childColumns = ["main_category_id"]
)])
data class SubCategory(
    @PrimaryKey(autoGenerate = true) val sub_category_id: Int,
    val  main_category_id: Int,
    @ColumnInfo(name = "sub_category_name") val sub_category_name: String,
    @ColumnInfo(name = "deleted_yn", defaultValue = "n") val deleted_yn: String
)
