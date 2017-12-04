package net.syarihu.android.listcardview

import android.content.Context
import android.database.Observable
import android.graphics.Color
import android.support.v4.util.LongSparseArray
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

class ListCardView : CardView {
    private val dividerSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics).toInt()
    private val defaultPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
    private val listItemCache = LongSparseArray<View>()
    private val dividerCache = LongSparseArray<View>()

    private var dividerColor = Color.LTGRAY
    private var listLimit = 0

    private var rootView: RelativeLayout = RelativeLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    var headerView: View? = TextView(context).apply {
        id = R.id.header_view
        layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
    }
    private var listView: LinearLayout = LinearLayout(context).apply {
        id = R.id.list_view
        orientation = LinearLayout.VERTICAL
        layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(RelativeLayout.BELOW, R.id.header_view)
        }
    }
    var footerView: View? = TextView(context).apply {
        id = R.id.footer_view
        gravity = Gravity.END
        setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(RelativeLayout.BELOW, R.id.list_view)
        }
    }

    var adapter: Adapter? = null
        set(value) {
            value?.let {
                it.listLimit = listLimit
                listItemCache.clear()
                dividerCache.clear()
                for (position in 0 until it.getCount()) {
                    listItemCache.put(it.getItemId(position), it.createView(position, this))
                    dividerCache.put(position.toLong(), createDividerView())
                }
                dividerCache.put(listItemCache.size().toLong(), createDividerView())
                it.registerObserver(object : AdapterDataObserver {
                    override fun onChanged() {
                        onAdapterChanged(it)
                    }
                })
                it.notifyDataSetChanged()
            }
            field = value
        }

    fun onAdapterChanged(adapter: Adapter) {
        listLimit = adapter.listLimit
        val diff = adapter.getCount() - listItemCache.size()
        if (diff < 0) {
            for (i in listItemCache.size() - 1 downTo (listItemCache.size() - Math.abs(diff))) {
                listItemCache.removeAt(i)
                dividerCache.removeAt(i)
            }
        } else if (diff > 0) {
            val startPosition = if (listItemCache.size() == 0) 0 else listItemCache.size()
            for (i in startPosition until listItemCache.size() + Math.abs(diff)) {
                listItemCache.put(adapter.getItemId(i), adapter.createView(i, this@ListCardView))
                dividerCache.put(i.toLong(), createDividerView())
            }
            dividerCache.put(listItemCache.size().toLong(), createDividerView())
        }
        listView.removeAllViews()
        for (i in 0 until listItemCache.limit()) {
            listView.addView(dividerCache.valueAt(i))
            listView.addView(adapter.bindView(i, listItemCache.get(adapter.getItemId(i))))
        }
        listView.addView(dividerCache.valueAt(listItemCache.size()))
    }

    constructor(context: Context?) : super(context) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(attrs)
    }

    private fun initialize(attrs: AttributeSet? = null) {
        attrs?.let {
            context.obtainStyledAttributes(attrs, R.styleable.ListCardView).apply {
                if (headerView is TextView) {
                    (headerView as TextView).run {
                        text = getString(R.styleable.ListCardView_headerText)
                        setTextColor(getColor(R.styleable.ListCardView_headerTextColor, Color.BLACK))
                    }
                }
                if (footerView is TextView) {
                    (footerView as TextView).run {
                        text = getString(R.styleable.ListCardView_footerText)
                        setTextColor(getColor(R.styleable.ListCardView_footerTextColor, Color.BLACK))
                    }
                }
                dividerColor = getColor(R.styleable.ListCardView_android_divider, Color.LTGRAY)
                listLimit = getInteger(R.styleable.ListCardView_listLimit, 0)
            }
        }
        rootView.addView(headerView)
        rootView.addView(listView)
        rootView.addView(footerView)
        addView(rootView)
    }

    private fun createDividerView(): View {
        return View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dividerSize)
            setBackgroundColor(dividerColor)
        }
    }

    private fun LongSparseArray<View>.limit(): Int {
        return if (listLimit == 0) {
            size()
        } else {
            if (listLimit < size()) {
                listLimit
            } else {
                size()
            }
        }
    }

    abstract class Adapter {
        var listLimit: Int = 0
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        private val observable = AdapterDataObservable()
        abstract fun createView(position: Int, viewGroup: ViewGroup?): View
        abstract fun bindView(position: Int, view: View): View
        abstract fun getItem(position: Int): Any
        abstract fun getItemId(position: Int): Long
        abstract fun getCount(): Int
        fun notifyDataSetChanged() {
            observable.notifyChanged()
        }

        fun registerObserver(observer: AdapterDataObserver) {
            observable.registerObserver(observer)
        }
    }

    private class AdapterDataObservable : Observable<AdapterDataObserver>() {
        fun notifyChanged() {
            for (i in mObservers.indices.reversed()) {
                mObservers[i].onChanged()
            }
        }
    }

    interface AdapterDataObserver {
        fun onChanged()
    }
}