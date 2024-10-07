package com.jp_ais_training.keibo.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jp_ais_training.keibo.databinding.CalendarItemBinding
import com.jp_ais_training.keibo.util.Const
import com.jp_ais_training.keibo.activity.DetailActivity

class CalendarAdapter(private val itemList: ArrayList<CalendarItem>, val context: Context?) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
    val TAG = "View Holder"

    // 캘린더 뷰에 작성될 홀더
    inner class ViewHolder(private val binding: CalendarItemBinding, val context: Context?) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // 아이템 클릭 리스너 작성
            binding.calendarItem.setOnClickListener {
                val item = itemList[bindingAdapterPosition]
                if (item.index == 0) return@setOnClickListener

                Log.d(TAG, item.date)
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra(Const.TARGET_DATE, item.date)
                context?.startActivity(intent)
            }

        }

        // 뷰 바인딩
        fun bind(calendarItem: CalendarItem) {
            if (calendarItem.date != Const.NULL) {
                var day = calendarItem.date.substring(8, 10)
                day = day.toInt().toString()
                binding.calendarItemDate.text = day
            } else
                binding.calendarItemDate.text = ""
            if (calendarItem.income != 0)
                binding.calendarItemIncome.text = "+" + calendarItem.income.toString()
            else
                binding.calendarItemIncome.text = ""
            if (calendarItem.expense != 0)
                binding.calendarItemExpense.text = "-" + calendarItem.expense.toString()
            else
                binding.calendarItemExpense.text = ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CalendarItemBinding.inflate(LayoutInflater.from(parent.context))


        return ViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}