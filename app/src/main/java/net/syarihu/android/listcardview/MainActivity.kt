package net.syarihu.android.listcardview

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.syarihu.android.listcardview.databinding.ActivityMainBinding
import net.syarihu.android.listcardview.databinding.ViewholderListcardBinding
import java.util.Random

class MainActivity : AppCompatActivity() {
    private var simpleAdapter: SimpleAdapter? = null
    private var limit: Int = 0
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.recyclerView.adapter = Adapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val list = ArrayList<String>().apply {
            for (i in 0..5) {
                add("test$i")
            }
        }
        simpleAdapter = SimpleAdapter(LayoutInflater.from(this@MainActivity), list, { pos, adapter ->
            list.removeAt(pos)
            adapter.notifyDataSetChanged()
        })
        binding.add.setOnClickListener {
            list.add("test${Random().nextInt()}")
            simpleAdapter?.notifyDataSetChanged()
        }
        binding.remove.setOnClickListener {
            if (list.size > 0) {
                list.removeAt(0)
                simpleAdapter?.notifyDataSetChanged()
            }
        }
        binding.change.setOnClickListener {
            if (list.size > 0) {
                list[0] = "test${Random().nextInt()}"
                simpleAdapter?.notifyDataSetChanged()
            }
        }
        binding.limitPlus.setOnClickListener {
            limit++
            binding.setLimit(limit)
            simpleAdapter?.listLimit = limit
        }
        binding.limitMinus.setOnClickListener {
            if (limit > 0) {
                limit--
                binding.setLimit(limit)
                simpleAdapter?.listLimit = limit
            }
        }

    }

    inner class Adapter : RecyclerView.Adapter<BindingHolder<ViewholderListcardBinding>>() {
        override fun onBindViewHolder(holder: BindingHolder<ViewholderListcardBinding>, position: Int) {
            holder.binding.listCardView.adapter = simpleAdapter
            limit = simpleAdapter?.listLimit ?: 0
            binding.setLimit(limit)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder<ViewholderListcardBinding> {
            return BindingHolder(this@MainActivity, parent, R.layout.viewholder_listcard)
        }

        override fun getItemCount(): Int {
            return 3
        }
    }
}
