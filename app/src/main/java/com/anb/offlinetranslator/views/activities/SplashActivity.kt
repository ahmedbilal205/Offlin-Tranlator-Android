package com.anb.offlinetranslator.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.anb.offlinetranslator.databinding.ActivitySplashBinding
import com.anb.offlinetranslator.remoteConfig.RemoteDataConfig
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import com.anb.offlinetranslator.utils.Utils
import com.anb.offlinetranslator.workers.ModelDownloadWorker
import com.bumptech.glide.Glide

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding : ActivitySplashBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var tinyDB: TinyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tinyDB = TinyDB(this)

        setupRemoteConfig()

        val downloadedLangs : ArrayList<String> = ArrayList()
        val modelManager = RemoteModelManager.getInstance()
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                models.forEach{
                    downloadedLangs.add(it.language)
                }
                tinyDB.putListString(AppConfig.DOWNLOADED_LANGS,downloadedLangs)
            }
            .addOnCompleteListener{
                if(!downloadedLangs.contains("es")
                    &&Utils.isOnline(this)){
                    //download spanish by default
                    val data = Data.Builder()
                    data.putString("lang", "es")
                    data.putString("langName", "spanish")
                    val langDownloadConditions = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .setInputData(data.build())
                        .build()
                    WorkManager.getInstance(this).enqueue(langDownloadConditions)
                    downloadedLangs.add("es")
                    tinyDB.putListString(AppConfig.DOWNLOADED_LANGS,downloadedLangs)
                }
            }
        Glide.with(this@SplashActivity)
            .load(com.anb.offlinetranslator.R.drawable.icon)
            .into(binding.splashIc)

        binding.startBtn.setOnClickListener{
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashProgress.visibility = View.GONE
            binding.startBtn.visibility = View.VISIBLE
        }, 5500)


        if (Build.VERSION.SDK_INT >= 33)
            pushNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    private fun setupRemoteConfig() {
        RemoteDataConfig()
        RemoteDataConfig().getSplashRemoteConfig {
            tinyDB.putString(AppConfig.APP_OPEN,it.admob_app_open_id.value)
//            Toast.makeText(this, ""+it.admob_app_open_id.value, Toast.LENGTH_SHORT).show()
        }
    }

    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted){
            //TODO
        }


    }

}