package com.jp_ais_training.keibo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.databinding.ActivityMainBinding
import com.jp_ais_training.keibo.frament.BarStatisticsFragment
import com.jp_ais_training.keibo.frament.CircleStatisticsFragment
import com.jp_ais_training.keibo.frament.HomeFragment
import com.jp_ais_training.keibo.frament.SettingsFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firstFragment = HomeFragment()
        val secondFragment = CircleStatisticsFragment()
        val thirdFragment = BarStatisticsFragment()
        val fourthFragment = SettingsFragment()

        setCurrentFragment(firstFragment)
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(firstFragment)
                R.id.circle_statistics -> setCurrentFragment(secondFragment)
                R.id.bar_statistics -> setCurrentFragment(thirdFragment)
                R.id.settings -> setCurrentFragment(fourthFragment)

            }
            true
        }
    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}