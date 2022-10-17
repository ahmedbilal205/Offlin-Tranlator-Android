package com.anb.offlinetranslator.views.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anb.offlinetranslator.adapters.TextHistoryAdapter
import com.anb.offlinetranslator.data.HistoryItem
import com.anb.offlinetranslator.databinding.ActivityHistoryBinding
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB

class HistoryActivity : AppCompatActivity() {
    lateinit var binding: ActivityHistoryBinding
    lateinit var tinyDB: TinyDB
    lateinit var textHistoryAdapter: TextHistoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tinyDB = TinyDB(this)
        initViews()
    }
    private fun initViews() {
        binding.icBack.setOnClickListener {onBackPressed()}
        val txtHist = tinyDB.getListObject(AppConfig.TEXT_HISTORY, HistoryItem::class.java) as List<HistoryItem>
        textHistoryAdapter = TextHistoryAdapter(txtHist,this)
        binding.txtHistoryRecycler.layoutManager = LinearLayoutManager(this)
        binding.txtHistoryRecycler.adapter = textHistoryAdapter
        if (textHistoryAdapter.itemCount<=0)
        {
            binding.noTxtHistImg.visibility = View.VISIBLE
        }else{
            binding.noTxtHistImg.visibility = View.GONE
        }
        textHistoryAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                if (textHistoryAdapter.itemCount<=0)
                {
                    binding.noTxtHistImg.visibility = View.VISIBLE
                }else{
                    binding.noTxtHistImg.visibility = View.GONE
                }
            }
        })
    }
}