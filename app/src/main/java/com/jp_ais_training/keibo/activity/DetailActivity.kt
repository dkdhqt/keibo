package com.jp_ais_training.keibo.activity

import DetailFragment
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.jp_ais_training.keibo.databinding.ActivityDetailBinding
import com.jp_ais_training.keibo.util.Const
import com.jp_ais_training.keibo.db.AppDatabase
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class DetailActivity : AppCompatActivity() {
    private lateinit var DB: AppDatabase

    private lateinit var viewPager: ViewPager2
    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val wholeDate = mutableListOf<Fragment>()

        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var strTargetDate: String? = intent.getStringExtra(Const.TARGET_DATE)
        //strTargetDate = "2022-05-05"
        val currentDay = LocalDate.now()
        val lastDayOfMonth = LocalDate.of(currentDay.year, currentDay.month + 1, 1).minusDays(1)

        var targetDate: LocalDate = LocalDate.parse(strTargetDate, format)

        viewPager = binding.pager
        var titleDate = binding.titleDate
        var leftArrow = binding.leftArrow
        var rightArrow = binding.rightArrow


        for (i in -10..10) {
            wholeDate.add(createFragment(targetDate.plusDays(i.toLong())))
        }


        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, wholeDate, lifecycle)
        viewPager.adapter = viewPagerAdapter
        viewPager.isUserInputEnabled = false
        viewPager.setCurrentItem(wholeDate.size / 2, false)

        titleDate.text = targetDate.toString()

        leftArrow.setOnClickListener {
            targetDate = targetDate.minusDays(1)
            binding.titleDate.text = targetDate.toString()

            val minDate = LocalDate.parse(
                viewPagerAdapter.fragments[0].arguments?.getString(Const.TARGET_DATE),
                format
            )

            if (Period.between(minDate, targetDate).days < 1) {
                viewPagerAdapter.addWithNotify(
                    0,
                    createFragment(minDate.minusDays(1))
                ).apply { viewPagerAdapter.notifyItemChanged(viewPager.currentItem) }

            } else {
                viewPager.setCurrentItem(viewPager.currentItem - 1, true)
            }


        }

        rightArrow.setOnClickListener {

            if (Period.between(lastDayOfMonth, targetDate).months == 0
                && Period.between(lastDayOfMonth, targetDate).days == 0
            ) {
            } else {

                targetDate = targetDate.plusDays(1)
                binding.titleDate.text = targetDate.toString()

                val maxDate = LocalDate.parse(
                    viewPagerAdapter.fragments[viewPagerAdapter.itemCount - 1].arguments?.getString(
                        Const.TARGET_DATE
                    ),
                    format
                )
                viewPager.setCurrentItem(viewPager.currentItem + 1, true)



                if (Period.between(targetDate, maxDate).days < 1) {
                    viewPagerAdapter.addWithNotify(
                        viewPagerAdapter.itemCount,
                        createFragment(maxDate.plusDays(1))
                    )
                }
            }
        }


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageSelected(position: Int) {

                super.onPageSelected(position)
            }
        })

        binding.btnRate.setOnClickListener {
            val detailFragment =
                supportFragmentManager.fragments[0] as DetailFragment
            val flag = binding.btnRate.text.toString() == "円"

            if(flag){
                binding.btnRate.text = "₩"
                detailFragment.changeRate(flag)
            }else{
                binding.btnRate.text = "円"
                detailFragment.changeRate(flag)
            }
        }
    }

    private fun createFragment(targetDate: LocalDate?): Fragment {
        var fragment = DetailFragment()
        var bundle = Bundle()
        bundle.putString(Const.TARGET_DATE, targetDate.toString())
        fragment.arguments = bundle
        return fragment
    }


}


class ViewPagerAdapter(
    fragmentManager: FragmentManager, var fragments: MutableList<Fragment>, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }


    fun addWithNotify(index: Int, fragment: Fragment) {
        fragments.add(index, fragment)
        notifyItemChanged(index)
    }

    fun add(index: Int, fragment: Fragment) {
        fragments.reversed()
        //notifyItemChanged(index)
    }

    fun refreshFragment(index: Int, fragment: Fragment) {
        fragments[index] = fragment
        notifyItemChanged(index)
    }

    fun remove(index: Int) {
        fragments.removeAt(index)
        //notifyItemChanged(index)
    }

    override fun getItemId(position: Int): Long {
        return fragments[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragments.find { it.hashCode().toLong() == itemId } != null
    }
}