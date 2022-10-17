package com.anb.offlinetranslator.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.anb.offlinetranslator.databinding.ActivitySplashBinding
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import com.anb.offlinetranslator.utils.Utils
import com.anb.offlinetranslator.workers.ModelDownloadWorker


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding : ActivitySplashBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tinyDB = TinyDB(this)
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
                    val langDownloadConditions = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .setInputData(data.build())
                        .build()
                    WorkManager.getInstance(this).enqueue(langDownloadConditions)
                    downloadedLangs.add("es")
                    tinyDB.putListString(AppConfig.DOWNLOADED_LANGS,downloadedLangs)
                }
            }


        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }, 4000)

        if (Build.VERSION.SDK_INT >= 33)
            pushNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted){
            //TODO
        }


    }

}