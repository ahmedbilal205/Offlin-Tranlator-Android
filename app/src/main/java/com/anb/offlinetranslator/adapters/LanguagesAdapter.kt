package com.anb.offlinetranslator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bumptech.glide.Glide
import com.anb.offlinetranslator.data.LanguageItem
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import com.anb.offlinetranslator.utils.Utils
import com.anb.offlinetranslator.workers.ModelDownloadWorker
import java.util.*
import com.anb.offlinetranslator.R

class LanguagesAdapter(private var mList: List<LanguageItem>, val context: Context?, private val onLangClicked: OnLangClicked)
    : RecyclerView.Adapter<LanguagesAdapter.ViewHolder>()
{
    var tinyDB = TinyDB(context)
    private  val TAG = "LanguagesAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.language_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]

        if (context != null) {
            Glide.with(context).load(
                "https://countryflagsapi.com/png/"+itemsViewModel.flag
            ).into(holder.imageView)
        }
        if (tinyDB.getListString(AppConfig.DOWNLOADED_LANGS).contains(itemsViewModel.code))
        {
            holder.downloadedCheck.setImageDrawable(context?.getDrawable(R.drawable.ic_check))
        }else{
            holder.downloadedCheck.setImageDrawable(context?.getDrawable(R.drawable.ic_cloud_download))
        }

        holder.textView.text = itemsViewModel.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        holder.langListItem.setOnClickListener {
            if (!tinyDB.getListString(AppConfig.DOWNLOADED_LANGS).contains(itemsViewModel.code))
            {
                if (!Utils.isOnline(context!!))
                {
                    Toast.makeText(context, "No internet, unable to download", Toast.LENGTH_SHORT).show()
                }else{
                    holder.downloadedCheck.setImageResource(R.drawable.progress_animation)
                    it.isClickable = false
                    startDownloading(itemsViewModel.code,itemsViewModel.name)
                    onLangClicked.onLangClicked(itemsViewModel)
                }
            }else{
                onLangClicked.onLangClicked(itemsViewModel)
            }
        }
    }

    private fun startDownloading(code: String, name: String)
    {
        val data = Data.Builder()
        data.putString("lang", code)
        data.putString("langName", name)
        val langDownloadConditions = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(data.build())
            .build()
        WorkManager.getInstance(context!!).enqueue(langDownloadConditions)
        if (Utils.isOnline(context))
            Toast.makeText(context, "downloading now: $name", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Internet not available, unable to download language", Toast.LENGTH_SHORT).show()
    }

    fun updateData(mList : List<LanguageItem>){
        this.mList = mList
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.flag_img)
        val textView: TextView = itemView.findViewById(R.id.langName)
        val langListItem: ConstraintLayout = itemView.findViewById(R.id.langListItem)
        val downloadedCheck: ImageView = itemView.findViewById(R.id.downloadedCheck)
    }

    interface OnLangClicked{
        fun onLangClicked(lang: LanguageItem)
    }
}