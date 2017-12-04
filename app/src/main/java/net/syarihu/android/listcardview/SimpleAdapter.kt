package net.syarihu.android.listcardview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class SimpleAdapter(
        private val inflater: LayoutInflater, private val list: List<String>,
        private val onItemClickListener: (position: Int, adapter: ListCardView.Adapter) -> Unit
) : ListCardView.Adapter() {
    override fun createView(position: Int, viewGroup: ViewGroup?): View {
        return inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false)
    }

    override fun bindView(position: Int, view: View): View {
        view.findViewById<TextView>(android.R.id.text1).apply {
            text = getItem(position).toString()
            setOnClickListener {
                onItemClickListener.invoke(position, this@SimpleAdapter)
            }
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}