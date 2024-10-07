package com.jp_ais_training.keibo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.frament.CircleStatisticsFragment
import com.jp_ais_training.keibo.databinding.ExpandablelistParentRowBinding
import com.jp_ais_training.keibo.databinding.ExpandablelistChildRowBinding


class CircleStatisticsExpandableListAdapter(
    private val context: Context,
    private val parents: MutableList<CircleStatisticsFragment.MenuTitle>,
    private val childList: MutableList<MutableList<CircleStatisticsFragment.MenuSpecific>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return parents.size
    }

    override fun getChildrenCount(parent: Int): Int {
        return childList[parent].size
    }

    override fun getGroup(parent: Int): Any {
        return parents[parent]
    }

    override fun getChild(parent: Int, child: Int): Any {
        return childList[parent][child]
    }

    override fun getGroupId(parent: Int): Long {
        return parent.toLong()
    }

    override fun getChildId(parent: Int, child: Int): Long {
        return child.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(parent: Int, isExpanded: Boolean, convertView: View?, parentView: ViewGroup?): View {
        val binding = ExpandablelistParentRowBinding.inflate(LayoutInflater.from(context), parentView, false)

        binding.expandTitle.text = parents[parent].title
        binding.mainCategorySumPrice.text = parents[parent].price

        setArrow(binding, isExpanded)

        return binding.root
    }

    override fun getChildView(parent: Int, child: Int, isLastChild: Boolean, convertView: View?, parentView: ViewGroup?): View {
        val binding = ExpandablelistChildRowBinding.inflate(LayoutInflater.from(context), parentView, false)
        val item = getChild(parent, child) as CircleStatisticsFragment.MenuSpecific

        binding.expandChildTitle.text= item.title
        binding.expandChildDetail.text = item.detail

        return binding.root
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    private fun setArrow(binding: ExpandablelistParentRowBinding, isExpanded: Boolean) {
        if(isExpanded) {
            binding.explainIcon.setImageResource(R.drawable.ic_close_explain)
        } else {
            binding.explainIcon.setImageResource(R.drawable.ic_show_explain)
        }
    }
}