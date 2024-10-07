import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.method.KeyListener
import android.text.method.MovementMethod
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jp_ais_training.keibo.KeiboApplication
import com.jp_ais_training.keibo.databinding.FragmentContentsBinding
import com.jp_ais_training.keibo.databinding.RecyclerContentsItemBinding
import com.jp_ais_training.keibo.dialog.CategoryDialog
import com.jp_ais_training.keibo.model.entity.ExpenseItem
import com.jp_ais_training.keibo.model.entity.IncomeItem
import com.jp_ais_training.keibo.model.response.ResponseItem
import com.jp_ais_training.keibo.util.Const
import com.jp_ais_training.keibo.util.PreferenceUtil
import kotlinx.coroutines.*
import net.cachapa.expandablelayout.ExpandableLayout
import java.lang.reflect.Field
import java.text.DecimalFormat

class ContentsFragment() : Fragment() {

    private lateinit var app: KeiboApplication
    private var targetDate = ""
    private var type = -1 // 0 IncomeFix 1 IncomeFlex 2 ExpenseFix 3 ExpenseFlex
    private lateinit var dataList: ArrayList<ResponseItem>
    private var parentColor = -1
    private var color = -1

    private var _binding: FragmentContentsBinding? = null
    private val binding get() = _binding!!

    fun changeRate(flag: Boolean) {
        val adapter = binding.recyclerContentsView.adapter as SimpleAdapter
        adapter.toggle(flag)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        app = requireActivity().application as KeiboApplication


        val bundle = arguments
        if (bundle != null) {
            targetDate = bundle.getString(Const.TARGET_DATE).toString()
            type = bundle.getInt(Const.TYPE)
        }
        super.onCreate(savedInstanceState)
    }

    private fun loadData() {

        when (type) {
            0 -> {
                dataList = ArrayList(app.db.loadFixII(targetDate))
                parentColor = Color.rgb(255, 204, 204)
                color = Color.rgb(255, 225, 225)
            }
            1 -> {
                dataList = ArrayList(app.db.loadFlexII(targetDate))
                parentColor = Color.rgb(229, 255, 204)
                color = Color.rgb(250, 255, 225)
            }
            2 -> {
                dataList = ArrayList(app.db.loadFixEI(targetDate))
                parentColor = Color.rgb(204, 255, 255)
                color = Color.rgb(225, 255, 255)
            }
            else -> {
                dataList = ArrayList(app.db.loadFlexEI(targetDate))
                parentColor = Color.rgb(229, 204, 255)
                color = Color.rgb(250, 225, 255)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContentsBinding.inflate(inflater, container, false)

        val recyclerView = binding.recyclerContentsView
        recyclerView.setBackgroundColor(color)
        recyclerView.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Dispatchers.Main).launch {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                loadData()
                context?.let {
                    ContextCompat.getMainExecutor(it).execute {
                        recyclerView.adapter =
                            SimpleAdapter(
                                recyclerView,
                                dataList,
                                parentColor,
                                color,
                                type,
                                targetDate,
                                super.requireActivity(),
                                it,
                                app
                            )
                    }
                }
            }
        }
        return binding.root
    }

    private class SimpleAdapter(
        private val recyclerView: RecyclerView, private val dataList: ArrayList<ResponseItem>,
        private val parentColor: Int, private val color: Int,
        private val type: Int, private val targetDate: String,
        private val activity: Activity, private val ctx: Context,
        private val app: KeiboApplication
    ) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

        private var selectedItem = UNSELECTED
        private var _itemBinding: RecyclerContentsItemBinding? = null
        private val itemBinding get() = _itemBinding!!
        private var isJPY = true


        fun toggle(flag: Boolean) {
            val rate = (PreferenceUtil(ctx).getKawaseRate().div(100.0)).toInt()
            if (flag) {
                dataList.forEach { data ->
                    data.price = data.price?.times(rate)
                    isJPY = false
                }
            } else {
                dataList.forEach { data ->
                    data.price = data.price?.times(1.div(rate.toFloat()))?.toInt()
                    isJPY = true
                }
            }
            this.notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            _itemBinding = RecyclerContentsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
            setFadeAnimation(holder.itemView)
        }

        private fun setFadeAnimation(view: View) {
            val anim = AlphaAnimation(0.0f, 1.0f)
            anim.duration = 500
            view.startAnimation(anim)
        }

        override fun getItemCount(): Int {
            return dataList.size + 1
        }

        fun addItem(item: ResponseItem, position: Int) {
            activity.runOnUiThread {
                dataList.add(item)
                //this.notifyDataSetChanged()
                this.notifyItemChanged(position)
            }
        }

        fun removeItem(position: Int) {
            activity.runOnUiThread {
                dataList.removeAt(position)
                //this.notifyDataSetChanged()
                this.notifyItemRemoved(position)
            }
        }


        inner class ViewHolder(binding: RecyclerContentsItemBinding) :
            RecyclerView.ViewHolder(binding.root),
            View.OnClickListener, ExpandableLayout.OnExpansionUpdateListener {
            private val cardView: CardView
            private val msgNull: TextView
            private val name: EditText
            private val price: EditText
            private val mainCg: Button
            private val subCg: Button
            private val taxCheckBox: CheckBox
            private val taxLayout: LinearLayout
            private val topLayout: LinearLayout

            private val nameListener: KeyListener
            private val priceListener: KeyListener
            private val nameMethod: MovementMethod
            private val priceMethod: MovementMethod

            private val categoryDialog: CategoryDialog

            override fun onClick(v: View?) {
                if (isJPY) {
                    val holder =
                        recyclerView.findViewHolderForAdapterPosition(selectedItem) as ViewHolder?

                    if (holder != null) {

                        deActivationItem(holder)
                        // 널체크
                        if (nullChecker(holder)) {
                            holder.msgNull.visibility = View.GONE
                            val position = selectedItem

                            val taxFlag = holder.taxCheckBox.isChecked
                            val tempPrice =
                                holder.price.text.toString().replace(("[^\\d.]").toRegex(), "")
                                    .toInt()
                            var price = if (taxFlag)
                                tempPrice
                            else
                                (tempPrice * 1.1).toInt()
                            var strType = ""
                            when (type) {
                                0 -> strType = "fix"
                                1 -> strType = "flex"
                                2 -> strType = "fix"
                                3 -> strType = "flex"
                            }
                            //마지막 아이템인가?
                            if (position == dataList.size) {

                                CoroutineScope(Dispatchers.Main).launch {
                                    withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                                        if (type == 0 || type == 1) {
                                            val id = app.db.loadIILastId()
                                            val item = IncomeItem(
                                                id + 1, strType, holder.name.text.toString(),
                                                price,
                                                targetDate
                                            )
                                            app.db.insertII(item)
                                            addItem(
                                                ResponseItem(
                                                    item.income_item_id,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    item.name,
                                                    item.price,
                                                    item.datetime
                                                ), position
                                            )
                                        } else {
                                            val id = app.db.loadEILastId()
                                            val item = ExpenseItem(
                                                id + 1,
                                                holder.subCg.tag.toString().toInt(),
                                                holder.name.text.toString(),
                                                price,
                                                targetDate
                                            )
                                            app.db.insertEI(item)
                                            addItem(
                                                ResponseItem(
                                                    null,
                                                    item.expense_item_id,
                                                    holder.mainCg.tag.toString().toInt(),
                                                    holder.subCg.tag.toString().toInt(),
                                                    holder.mainCg.text.toString(),
                                                    holder.subCg.text.toString(),
                                                    item.name,
                                                    item.price,
                                                    item.datetime
                                                ), position
                                            )
                                        }
                                    }
                                }
                            } else {
                                CoroutineScope(Dispatchers.IO).async {
                                    //기존 데이터에서 변화된 값 체크
                                    if (dataCompare(holder, position)) {

                                        if (type == 0 || type == 1) {

                                            val item = IncomeItem(
                                                dataList.elementAt(position).income_item_id!!,
                                                strType,
                                                holder.name.text.toString(),
                                                price,
                                                targetDate
                                            )
                                            app.db.updateII(item)
                                            val data = dataList.elementAt(position)
                                            data.income_item_id = item.income_item_id
                                            data.name = holder.name.text.toString()
                                            data.price = price
                                        } else {

                                            val item = ExpenseItem(
                                                dataList.elementAt(position).expense_item_id!!,
                                                holder.subCg.tag.toString().toInt(),
                                                holder.name.text.toString(),
                                                price,
                                                targetDate
                                            )
                                            app.db.updateEI(item)
                                            val data = dataList.elementAt(position)
                                            data.expense_item_id = item.expense_item_id
                                            data.main_category_id =
                                                holder.mainCg.tag.toString().toInt()
                                            data.sub_category_id =
                                                holder.subCg.tag.toString().toInt()
                                            data.main_category_name = holder.mainCg.text.toString()
                                            data.sub_category_name = holder.subCg.text.toString()
                                            data.name = holder.name.text.toString()
                                            data.price = price
                                            //세금계산된 가격 EditText 에 반영
                                            holder.price.text =
                                                SpannableStringBuilder(
                                                    DecimalFormat("-#,###,###.#円").format(
                                                        price
                                                    )
                                                )
                                            //체크박스 초기화
                                            holder.taxCheckBox.isChecked = true
                                        }
                                    }
                                }
                            }
                        } else {
                            holder.msgNull.visibility = View.VISIBLE
                        }
                    }

                    val position = bindingAdapterPosition
                    if (position == selectedItem) {

                        setActivationItem(false)

                        selectedItem = UNSELECTED

                    } else {

                        setActivationItem(true)

                        selectedItem = position
                    }
                } else
                    Toast.makeText(ctx, "₩表示の時にはデータの変更はできません。", Toast.LENGTH_LONG).show()

            }

            override fun onExpansionUpdate(expansionFraction: Float, state: Int) {
                TODO("Not yet implemented")
            }

            fun bind() {
                val position = bindingAdapterPosition
                val isSelected = position == selectedItem

                if (dataList.size != position) {
                    name.text = SpannableStringBuilder(dataList[position].name)
                    if (type == 0 || type == 1) {
                        val fPrice = if (isJPY)
                            DecimalFormat("+#,###,###.#円").format(dataList[position].price)
                        else
                            DecimalFormat("+#,###,###.#₩").format(dataList[position].price)
                        price.text = SpannableStringBuilder(fPrice)
                        price.setTextColor(Color.BLUE)
                        topLayout.visibility = View.GONE
                        taxLayout.visibility = View.INVISIBLE
                    } else {
                        val fPrice = if (isJPY)
                            DecimalFormat("-#,###,###.#円").format(dataList[position].price)
                        else
                            DecimalFormat("-#,###,###.#₩").format(dataList[position].price)
                        price.text = SpannableStringBuilder(fPrice)
                        price.setTextColor(Color.RED)

                        mainCg.text = dataList[position].main_category_name
                        mainCg.tag = dataList[position].main_category_id
                        subCg.text = dataList[position].sub_category_name
                        subCg.tag = dataList[position].sub_category_id

                        topLayout.visibility = View.VISIBLE
                        taxLayout.visibility = View.VISIBLE
                    }
                } else {
                    name.text = SpannableStringBuilder("")
                    price.text = SpannableStringBuilder("")
                    mainCg.text = "カテゴリ"
                    subCg.text = "サーブカテゴリ"
                    subCg.visibility = View.INVISIBLE
                    taxLayout.visibility = View.VISIBLE

                    if (type == 0 || type == 1) {
                        price.setTextColor(Color.BLUE)
                        topLayout.visibility = View.GONE
                    } else {
                        price.setTextColor(Color.RED)
                        taxLayout.visibility = View.VISIBLE
                    }

                }

                val onKeyListener = View.OnKeyListener { _, KeyCode, event ->
                    if (KeyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                        closeKeyBoard()
                        name.clearFocus()
                        name.clearComposingText()
                        price.clearFocus()
                        name.clearComposingText()
                        return@OnKeyListener true
                    }
                    false
                }

                val onNameFocusChangeListener = View.OnFocusChangeListener { view, isFocus ->
                    if (isFocus) {
                    } else {
                        name.clearFocus()
                        name.clearComposingText()
                        closeKeyBoard()
                    }
                }

                val onPriceFocusChangeListener = View.OnFocusChangeListener { view, isFocus ->
                    if (!price.text.isNullOrEmpty()) {
                        if (isFocus) {
                            val regexPrice = price.text.toString().replace(("[^\\d.]").toRegex(), "").toInt()
                            val fPrice =
                                DecimalFormat("########").format(regexPrice)

                            val inputFilter = arrayOf(InputFilter.LengthFilter(8))
                            price.filters = inputFilter
                            price.text = SpannableStringBuilder(fPrice)

                        } else {
                            val inputFilter = arrayOf(InputFilter.LengthFilter(16))
                            price.filters = inputFilter
                            val fPrice = if (type == 0 || type == 1) {
                                if (isJPY)
                                    DecimalFormat("+#,###,###.#円").format(
                                        price.text.toString().toInt()
                                    )
                                else
                                    DecimalFormat("+#,###,###.#₩").format(
                                        price.text.toString().toInt()
                                    )
                            } else {
                                if (isJPY)
                                    DecimalFormat("-#,###,###.#円").format(
                                        price.text.toString().toInt()
                                    )
                                else
                                    DecimalFormat("-#,###,###.#₩").format(
                                        price.text.toString().toInt()
                                    )
                            }
                            price.text = SpannableStringBuilder(fPrice)
                            price.clearFocus()
                            price.clearComposingText()
                            closeKeyBoard()
                        }
                    }else{
                        val inputFilter = arrayOf(InputFilter.LengthFilter(8))
                        price.filters = inputFilter
                    }
                }

                mainCg.setOnClickListener {
                    categoryDialog.callMainCategory(type)
                }

                subCg.setOnClickListener {
                    categoryDialog.callSubCategory(
                        mainCg.tag.toString().toInt(),
                        mainCg.text.toString()
                    )
                }

                categoryDialog.setOnClickedListener(object : CategoryDialog.ButtonClickListener {
                    override fun onClicked(id: Int, name: String, type: String) {
                        if (type == "main") {
                            mainCg.text = name
                            mainCg.tag = id
                            subCg.visibility = View.VISIBLE
                        } else if (type == "sub") {
                            subCg.text = name
                            subCg.tag = id
                        } else if (type == "delete") {
                            removeItem(id) //id is position
                        }
                    }
                })

                cardView.setBackgroundColor(Color.WHITE)
                name.setBackgroundColor(Color.WHITE)
                name.setTextColor(Color.BLACK)
                name.setCursorColor(ctx, parentColor)
                name.setOnKeyListener(onKeyListener)
                name.onFocusChangeListener = onNameFocusChangeListener
                price.setBackgroundColor(Color.WHITE)
                price.setCursorColor(ctx, parentColor)
                price.setOnKeyListener(onKeyListener)
                price.onFocusChangeListener = onPriceFocusChangeListener
                mainCg.setBackgroundColor(parentColor)
                mainCg.setTextColor(Color.WHITE)
                subCg.setBackgroundColor(parentColor)
                subCg.setTextColor(Color.WHITE)
                taxCheckBox.buttonTintList = ColorStateList.valueOf(parentColor)

                cardView.isSelected = isSelected

                cardView.setOnLongClickListener {
                    val position = bindingAdapterPosition
                    if (position != dataList.size) {
                        if (type == 0 || type == 1) {
                            categoryDialog.callDeleteItemDialog(
                                dataList[position].income_item_id!!,
                                type,
                                position
                            )
                        } else {
                            categoryDialog.callDeleteItemDialog(
                                dataList[position].expense_item_id!!,
                                type,
                                position
                            )
                        }
                    }
                    return@setOnLongClickListener true
                }

                setActivationItem(false)
            }

            init {
                cardView = binding.cardView
                msgNull = binding.msgNull
                name = binding.name
                name.maxWidth = name.width
                price = binding.price
                price.maxWidth = price.width
                topLayout = binding.topLayout
                mainCg = binding.mainCategory
                subCg = binding.subCategory
                taxCheckBox = binding.taxCheckBox
                taxLayout = binding.taxLayout
                cardView.setOnClickListener(this)

                nameListener = name.keyListener
                nameMethod = name.movementMethod
                priceListener = price.keyListener
                priceMethod = price.movementMethod
                categoryDialog = CategoryDialog(activity)


                setActivationItem(false)
            }


            fun closeKeyBoard() {
                val view = activity.currentFocus
                if (view != null) {
                    val imm =
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }

            fun nullChecker(holder: ViewHolder): Boolean {

                return if (type == 0 || type == 1) {
                    !holder.name.text.isNullOrBlank()
                            && !holder.price.text.isNullOrBlank()
                } else {
                    !holder.name.text.isNullOrBlank()
                            && !holder.price.text.isNullOrBlank()
                            && holder.mainCg.text.toString() != "カテゴリ"
                            && holder.subCg.text.toString() != "サーブカテゴリ"
                }

            }

            fun dataCompare(holder: ViewHolder, position: Int): Boolean {


                val price = holder.price.text.toString().replace(("[^\\d.]").toRegex(), "").toInt()
                return if (type == 0 || type == 1) {
                    holder.name.text.toString() != dataList[position].name
                            || price != dataList[position].price
                } else {
                    val taxFlag = holder.taxCheckBox.isChecked
                    var price = if (taxFlag)
                        price
                    else
                        (price * 1.1).toInt()

                    holder.name.text.toString() != dataList[position].name
                            || price != dataList[position].price
                            || holder.mainCg.text.toString() != dataList[position].main_category_name
                            || holder.subCg.text.toString() != dataList[position].sub_category_name

                }

            }

            fun deActivationItem(holder: ViewHolder) {
                closeKeyBoard()

                holder.cardView.isSelected = false
                holder.cardView.setBackgroundColor(Color.WHITE)

                holder.name.isClickable = false
                holder.name.keyListener = null
                holder.name.movementMethod = null
                holder.name.clearFocus()
                holder.name.clearComposingText()

                holder.price.isClickable = false
                holder.price.keyListener = null
                holder.price.movementMethod = null
                holder.price.clearFocus()
                holder.price.clearComposingText()

                holder.taxLayout.visibility = View.INVISIBLE

                holder.mainCg.setTextColor(Color.WHITE)
                holder.mainCg.setBackgroundColor(parentColor)
                holder.mainCg.isEnabled = false
                holder.mainCg.isClickable = false
                holder.subCg.setTextColor(Color.WHITE)
                holder.subCg.setBackgroundColor(parentColor)
                holder.subCg.isEnabled = false
                holder.subCg.isClickable = false

            }


            fun setActivationItem(active: Boolean) {

                if (active) {

                    cardView.isSelected = true
                    cardView.setBackgroundColor(color)

                    name.isClickable = true
                    name.keyListener = nameListener
                    name.movementMethod = nameMethod

                    price.isClickable = true
                    price.keyListener = priceListener
                    price.movementMethod = priceMethod

                    taxLayout.visibility = View.VISIBLE

                    mainCg.setTextColor(parentColor)
                    mainCg.setBackgroundColor(Color.WHITE)
                    mainCg.isEnabled = true
                    mainCg.isClickable = true
                    subCg.setTextColor(parentColor)
                    subCg.setBackgroundColor(Color.WHITE)
                    subCg.isEnabled = true
                    subCg.isClickable = true

                } else {
                    closeKeyBoard()

                    cardView.isSelected = false
                    cardView.setBackgroundColor(Color.WHITE)

                    name.isClickable = false
                    name.keyListener = null
                    name.movementMethod = null
                    name.clearFocus()
                    name.clearComposingText()

                    price.isClickable = false
                    price.keyListener = null
                    price.movementMethod = null
                    price.clearFocus()
                    price.clearComposingText()

                    taxLayout.visibility = View.INVISIBLE

                    mainCg.setTextColor(Color.WHITE)
                    mainCg.setBackgroundColor(parentColor)
                    mainCg.isEnabled = false
                    mainCg.isClickable = false
                    subCg.setTextColor(Color.WHITE)
                    subCg.setBackgroundColor(parentColor)
                    subCg.isEnabled = false
                    subCg.isClickable = false

                }
            }


            fun EditText.setCursorColor(context: Context, color: Int) {
                val editText = this

                val shapeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setSize(2.dpToPixels(context), 0)
                    setColor(color)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    textCursorDrawable = shapeDrawable
                } else {
                    try {
                        // get the cursor resource id
                        TextView::class.java.getDeclaredField("mCursorDrawableRes").apply {
                            isAccessible = true
                            val drawableResId: Int = getInt(editText)

                            // get the editor
                            val editorField: Field = TextView::class.java
                                .getDeclaredField("mEditor")
                            editorField.isAccessible = true
                            val editor: Any = editorField.get(editText)

                            // get the drawable and set a color filter
                            val drawable: Drawable? = ContextCompat
                                .getDrawable(editText.context, drawableResId)
                            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)

                            // set the drawables
                            editor.javaClass.getDeclaredField("mCursorDrawable").apply {
                                isAccessible = true
                                set(editor, arrayOf(drawable, drawable))
                            }
                        }
                    } catch (e: Exception) {
                        // log exception here
                    }
                }


            }

            fun Int.dpToPixels(context: Context): Int = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
            ).toInt()

        }


        companion object {
            private const val UNSELECTED = -1
        }
    }

    override fun onDestroy() {


        super.onDestroy()
    }

}