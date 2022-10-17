package com.anb.offlinetranslator.views.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.anb.offlinetranslator.adapters.LanguageDownloaderAdapter
import com.anb.offlinetranslator.data.LanguageItem
import com.anb.offlinetranslator.data.LanguagesList
import com.anb.offlinetranslator.databinding.ActivityDownloadLanguagesBinding
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import com.anb.offlinetranslator.utils.Utils
import com.anb.offlinetranslator.workers.ModelDownloadWorker

class DownloadLanguagesActivity : AppCompatActivity(), LanguageDownloaderAdapter.OnLangClicked
{
    lateinit var binding: ActivityDownloadLanguagesBinding
    lateinit var languageDownloaderAdapter: LanguageDownloaderAdapter
    lateinit var tinyDB: TinyDB
    var languagesList = LanguagesList.languages
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadLanguagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tinyDB = TinyDB(this)
        languageDownloaderAdapter = LanguageDownloaderAdapter(languagesList, this,this)
        binding.apply {
            downloadRecycler.layoutManager = LinearLayoutManager(this@DownloadLanguagesActivity)
            downloadRecycler.adapter = languageDownloaderAdapter
        }
        initToolBar()
        try {
            val filter = IntentFilter()
            filter.addAction("DOWNLOAD_COMPLETE")
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            registerReceiver(mMessageReceiver, filter)
            Log.d("bb-btn", "receiver registered ")
        } catch (ignored: Exception) {}
    }

    override fun onLangClicked(lang: LanguageItem) {
        if (tinyDB.getListString(AppConfig.DOWNLOADED_LANGS).contains(lang.code)) return

        val data = Data.Builder()
        data.putString("lang", lang.code)
        data.putString("langName", lang.name)
        val langDownloadConditions = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(data.build())
            .build()
        WorkManager.getInstance(this).enqueue(langDownloadConditions)
        if (Utils.isOnline(this))
            Toast.makeText(this, "downloading now: "+lang.name, Toast.LENGTH_SHORT).show()
        else Toast.makeText(this, "Internet not available, unable to download language", Toast.LENGTH_SHORT).show()
    }
    private fun initToolBar()
    {
        binding.searchLangs.setOnClickListener {
            binding.searchLangs.visibility = View.GONE
            binding.headerTv.visibility = View.GONE
            binding.searchLangsEdt.visibility = View.VISIBLE
            binding.removeAds.visibility = View.INVISIBLE
        }
        binding.icBack.setOnClickListener {
            if (binding.searchLangsEdt.visibility== View.VISIBLE)
            {
                binding.searchLangs.visibility = View.VISIBLE
                binding.headerTv.visibility = View.VISIBLE
                binding.searchLangsEdt.visibility = View.GONE
                binding.removeAds.visibility = View.GONE
            }else onBackPressed()

        }
        initSearch()
    }
    private fun initSearch() {
        binding.searchLangsEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(searchTxt: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (TextUtils.isEmpty(searchTxt.toString()))
                {
                    languagesList= LanguagesList.languages
                }else{
                    languagesList= ArrayList()
                    languagesList.clear()
                    for (i in LanguagesList.languages)
                    {
                        if (i.name.lowercase().contains(searchTxt.toString().lowercase()))
                        {
                            languagesList.add(i)
                        }
                    }
                }
                //TODO add auto detect in from
                languageDownloaderAdapter.updateData(languagesList)
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
                languageDownloaderAdapter.notifyDataSetChanged()
                Toast.makeText(this@DownloadLanguagesActivity, "Downloaded: "+intent.getStringExtra("lang"), Toast.LENGTH_SHORT).show()
        }
    }
}