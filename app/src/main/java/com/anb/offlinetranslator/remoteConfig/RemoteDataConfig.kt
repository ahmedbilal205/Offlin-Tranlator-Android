package com.anb.offlinetranslator.remoteConfig

import androidx.annotation.Keep
import com.anb.offlinetranslator.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class RemoteDataConfig {

    private var remoteConfig: FirebaseRemoteConfig? = null
    private val timeInMillis: Long = if (BuildConfig.DEBUG) 0L else 3600L
//    private val remoteTopic = "translator"
    private val remoteTopic = "test"

    companion object {
        var remoteAdSettings = RemoteAdSettings()
    }

    private fun getInstance(): FirebaseRemoteConfig? {
        remoteConfig?.let {
            return it
        }
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSetting = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(timeInMillis)
            .build()
        remoteConfig?.setConfigSettingsAsync(configSetting)
        remoteConfig?.setDefaultsAsync(
            mapOf(remoteTopic to Gson().toJson(RemoteAdSettings()))
        )
        return remoteConfig
    }


    private fun getRemoteConfig(): RemoteAdSettings {
        return Gson().fromJson(
            getInstance()?.getString(remoteTopic),
            RemoteAdSettings::class.java
        )
    }

    fun getSplashRemoteConfig(listener: (RemoteAdSettings) -> Unit) {
        getInstance()?.fetchAndActivate()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val value = getRemoteConfig()
                    remoteAdSettings = value
                    listener.invoke(value)
                }

            }
    }
}

@Keep
data class RemoteAdSettings(

    @SerializedName("admob_banner_id")
    val admob_banner_id: RemoteAdDetails = RemoteAdDetails(),

    @SerializedName("admob_app_open_id")
    val admob_app_open_id: RemoteAdDetails = RemoteAdDetails()
) {
//    override fun toString(): String {
//        return "$admob_app_inter_id, $admob_splash_inter_id, $admob_splash_native_id, $admob_app_native_id"
//    }
}

@Keep
data class RemoteAdDetails(
    @SerializedName("value")
    var value: String = "off",
)