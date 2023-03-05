package com.anb.offlinetranslator.views.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anb.offlinetranslator.adapters.LanguagesAdapter
import com.anb.offlinetranslator.data.LanguageItem
import com.anb.offlinetranslator.data.LanguagesList
import com.anb.offlinetranslator.databinding.FragmentSelectLanguageBinding
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import com.anb.offlinetranslator.utils.Utils

class SelectLanguageFragment()
    : Fragment(), LanguagesAdapter.OnLangClicked {

    private var status: String = ""
    private var langSelectedCallback: LangSelectedCallback? = null

    constructor(status: String, langSelectedCallback: LangSelectedCallback) : this() {
        this.status =status
        this.langSelectedCallback = langSelectedCallback
    }

    private val TAG = "SelectLanguageFragment"
    lateinit var binding: FragmentSelectLanguageBinding
    lateinit var languagesAdapter : LanguagesAdapter
    lateinit var recentlyUsedAdapter: LanguagesAdapter
    var searchDataList = LanguagesList.languages
    lateinit var tinyDB: TinyDB
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectLanguageBinding.inflate(inflater,container,false)
        tinyDB = TinyDB(context)
        initLanguagesRecycler()
        initViews()
        try {
            val filter = IntentFilter()
            filter.addAction("DOWNLOAD_COMPLETE")
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            requireActivity().registerReceiver(mMessageReceiver, filter)
            Log.d("bb-btn", "receiver registered ")
        } catch (ignored: Exception) {}
        return binding.root
    }

    private fun initViews()
    {
        binding.searchLangs.setOnClickListener {
            binding.searchLangs.visibility = View.GONE
            binding.headerTv.visibility = View.GONE
            binding.recentlyUsedLayout.visibility = View.GONE
            binding.searchLangsEdt.visibility = View.VISIBLE
            binding.removeAds.visibility = View.VISIBLE
            binding.searchLangsEdt.requestFocus()
        }
        binding.icBack.setOnClickListener {
            if (binding.searchLangsEdt.visibility==View.VISIBLE)
            {
                activity?.let { k -> Utils.hideKeyBoard(k) }
                binding.searchLangs.visibility = View.VISIBLE
                binding.headerTv.visibility = View.VISIBLE
                binding.recentlyUsedLayout.visibility = View.VISIBLE
                binding.searchLangsEdt.visibility = View.GONE
                binding.removeAds.visibility = View.GONE
            }else activity?.onBackPressed()

        }
        initSearch()
        initRecentlyUsed()
    }

    private fun initRecentlyUsed() {
        val recentlyUsed = ArrayList<LanguageItem>()
        try {
            recentlyUsed.add(tinyDB.getObject(AppConfig.RECENTLY_USED_FIRST,LanguageItem::class.java))
            recentlyUsed.add(tinyDB.getObject(AppConfig.RECENTLY_USED_SECOND,LanguageItem::class.java))
            recentlyUsedAdapter = LanguagesAdapter(recentlyUsed,context,this)
            binding.recentlyUsedRecycler.layoutManager= LinearLayoutManager(activity)
            binding.recentlyUsedRecycler.adapter = recentlyUsedAdapter
        }catch (ignored: Exception){
            binding.recentlyUsedLayout.visibility =View.GONE
        }
    }

    private fun initSearch() {
        binding.searchLangsEdt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(searchTxt: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (TextUtils.isEmpty(searchTxt.toString()))
                {
                    searchDataList= LanguagesList.languages
                    languagesAdapter.updateData(searchDataList)
                }else{
                    searchDataList= ArrayList()
                    searchDataList.clear()
                    for (i in LanguagesList.languages)
                    {
                        if (i.name.contains(searchTxt.toString(),ignoreCase = true))
                        {
                            Log.d(TAG, "onTextChanged 2: $searchTxt")
                            searchDataList.add(i)
                        }
                    }
                    languagesAdapter.updateData(searchDataList)
                }

            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }

    private fun initLanguagesRecycler() {
        languagesAdapter = LanguagesAdapter(searchDataList, context,this)

        binding.apply {
            languagesRecycler.layoutManager = LinearLayoutManager(activity)
            languagesRecycler.adapter = languagesAdapter
        }
    }

    override fun onLangClicked(lang: LanguageItem) {
        if (status=="to"){
            langSelectedCallback?.langTo(lang)
            activity?.onBackPressed()
        }else {
            langSelectedCallback?.langFrom(lang)
            activity?.onBackPressed()
        }
    }

    interface LangSelectedCallback{
        fun langFrom(langFrom: LanguageItem)
        fun langTo(langTo: LanguageItem)
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {
            languagesAdapter.notifyDataSetChanged()
        }
    }
}