package com.anb.offlinetranslator.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.anb.offlinetranslator.R
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import kotlinx.coroutines.tasks.await

class ModelDownloadWorker (val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val TAG = "ModelDownloadWorker"

    private val lang = workerParams.inputData.getString("lang")
    private val langName = workerParams.inputData.getString("langName")

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: $lang")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val name = NotificationConstants.CHANNEL_NAME
        val description = NotificationConstants.CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NotificationConstants.CHANNEL_ID,name,importance)
        channel.description = description
        channel.setSound(null, null)

        val notificationManager = this@ModelDownloadWorker.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(this@ModelDownloadWorker.applicationContext,NotificationConstants.CHANNEL_ID)
        .setSmallIcon(R.drawable.icon)
        .setContentTitle("Downloading languages...")
        .setOngoing(true)
        .setProgress(0,0,true)

    NotificationManagerCompat.from(this@ModelDownloadWorker.applicationContext).notify(NotificationConstants.NOTIFICATION_ID,builder.build())
    val res: Boolean = downloadLang() //async ha
    NotificationManagerCompat.from(this@ModelDownloadWorker.applicationContext).cancelAll()

    Log.d(TAG, "doWork: $res")
    return if (res) Result.success()
    else Result.failure()
    }

    private suspend fun downloadLang(): Boolean{
        val modelManager = RemoteModelManager.getInstance()
        val langModel = lang?.let { TranslateRemoteModel.Builder(it).build() }
        val conditions = DownloadConditions.Builder()
            .build()

        return try {
            Log.d(TAG, "downloadLang:1 ")
            modelManager.download(langModel!!, conditions)
                .await()

            Log.d(TAG, "downloadLang: 2")
            val tinyDB = TinyDB(this@ModelDownloadWorker.applicationContext)
            val dl = tinyDB.getListString(AppConfig.DOWNLOADED_LANGS)
            dl.add(lang)
            tinyDB.putListString(AppConfig.DOWNLOADED_LANGS,dl)
            //sending broadcast to update UI and
            val intent = Intent()
            intent.action = "DOWNLOAD_COMPLETE"
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.putExtra("lang",langName.toString())
            this@ModelDownloadWorker.applicationContext.sendBroadcast(intent)
//            Toast.makeText(this@ModelDownloadWorker.applicationContext, "Downloaded: $lang", Toast.LENGTH_SHORT).show()
            true
        }catch (e : Exception){
            Log.d(TAG, "downloadLang: 3")
            false
        }
    }
    object NotificationConstants{
        const val CHANNEL_NAME = "Downloading Progress"
        const val CHANNEL_DESCRIPTION = "download_language"
        const val CHANNEL_ID = "download_language_worker_205"
        const val NOTIFICATION_ID = 1
    }
}