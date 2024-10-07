import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jp_ais_training.keibo.R
import com.jp_ais_training.keibo.databinding.FragmentDetailBinding
import com.jp_ais_training.keibo.util.Const
import net.cachapa.expandablelayout.ExpandableLayout

class DetailFragment() : Fragment() {
    private var targetDate = ""
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    fun changeRate(flag :Boolean){
        val contentsFragmentsList =childFragmentManager.fragments as List<ContentsFragment>
        contentsFragmentsList.forEach{contentsFragment ->
            contentsFragment.changeRate(flag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val bundle = arguments
        if (bundle != null) {
            targetDate = bundle.getString("targetDate").toString()
        }
        println("onCreate : $targetDate")
        var frameLayoutId = ArrayList<Int>()

        frameLayoutId.add(R.id.contents_frame1)
        frameLayoutId.add(R.id.contents_frame2)
        frameLayoutId.add(R.id.contents_frame3)
        frameLayoutId.add(R.id.contents_frame4)

        for (i in 0..3) {
            val bundle = Bundle()
            bundle.putInt(Const.TYPE, i)
            bundle.putString(Const.TARGET_DATE, targetDate)
            val fragment = ContentsFragment()
            fragment.arguments = bundle
            childFragmentManager.beginTransaction().apply {
                add(frameLayoutId[i], fragment)
                commitNowAllowingStateLoss()
            }
        }

        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        var colorArray = ArrayList<Int>()
        var expandButton = ArrayList<LinearLayout>()
        var expandTitle = ArrayList<TextView>()
        var expandArrow = ArrayList<TextView>()
        var expandLayout = ArrayList<ExpandableLayout>()

        colorArray.add(Color.rgb(255, 204, 204))
        colorArray.add(Color.rgb(229, 255, 204))
        colorArray.add(Color.rgb(204, 255, 255))
        colorArray.add(Color.rgb(229, 204, 255))

        expandButton.add(binding.expandButton1)
        expandButton.add(binding.expandButton2)
        expandButton.add(binding.expandButton3)
        expandButton.add(binding.expandButton4)

        expandTitle.add(binding.expandTitle1)
        expandTitle.add(binding.expandTitle2)
        expandTitle.add(binding.expandTitle3)
        expandTitle.add(binding.expandTitle4)

        expandArrow.add(binding.expandArrow1)
        expandArrow.add(binding.expandArrow2)
        expandArrow.add(binding.expandArrow3)
        expandArrow.add(binding.expandArrow4)

        expandLayout.add(binding.expandableLayout1)
        expandLayout.add(binding.expandableLayout2)
        expandLayout.add(binding.expandableLayout3)
        expandLayout.add(binding.expandableLayout4)


        binding.expandTitle1.text = "固定収入"
        binding.expandTitle2.text = "変動収入"
        binding.expandTitle3.text = "固定支出"
        binding.expandTitle4.text = "変動支出"

        for (i in 0..3) {
            expandButton[i].setBackgroundColor(colorArray[i])
            expandButton[i].setOnClickListener {
                if (expandLayout[i].isExpanded) {
                    expandLayout[i].collapse()
                } else {
                    expandLayout[i].expand()
                }
            }

            expandTitle[i].setTextColor(Color.BLACK)
            expandTitle[i].setBackgroundColor(Color.TRANSPARENT)

            expandArrow[i].setTextColor(Color.BLACK)
            expandArrow[i].setBackgroundColor(Color.TRANSPARENT)

            expandLayout[i].setExpanded(false, false)
            expandLayout[i].setOnExpansionUpdateListener { expansionFraction, _ ->
                expandArrow[i].rotation = expansionFraction * -90.0f
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}