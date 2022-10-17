package com.anb.offlinetranslator.utils

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.inputmethod.InputMethodManager


class Utils {
    companion object{
        fun pasteTxt(context: Context) : String
        {
            val clipboard = (context.getSystemService(CLIPBOARD_SERVICE)) as? ClipboardManager
            val textToPaste = clipboard?.primaryClip?.getItemAt(0)?.text ?:  ""
            return textToPaste.toString()
        }
        fun copyText(context:  Context ,str: String){
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText(label.toString(), str)
            clipboard!!.setPrimaryClip(clip)
        }
        fun hideKeyBoard(activity: Activity){
                val imm =
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                var view = activity.currentFocus
                if (view == null) {
                    view = View(activity)
                }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
            return false
        }
    }

}