package com.anb.offlinetranslator.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.anb.offlinetranslator.R
import com.anb.offlinetranslator.data.HistoryItem
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import com.anb.offlinetranslator.views.activities.MainActivity


class TextHistoryAdapter(private var mList: List<HistoryItem>, val context: Context?):
    RecyclerView.Adapter<TextHistoryAdapter.ViewHolder>()
{
    var tinyDB = TinyDB(context)
    private val TAG = "TextHistoryAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_history_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]
        holder.langToFromTxt.text = "${itemsViewModel.langFrom} to ${itemsViewModel.langTo}"
        holder.fromTxt.text = itemsViewModel.txtFrom
        holder.toTxt.text = itemsViewModel.txtTo
        holder.delBtn.setOnClickListener {
         delItem(holder)
        }
        holder.txtHistoryItem.setOnClickListener {
            openHistoryInText(itemsViewModel)
        }

    }

    private fun openHistoryInText(itemsViewModel: HistoryItem) {
        val bundle = Bundle()
        bundle.putString("langFrom", itemsViewModel.langFrom)
        bundle.putString("langTo", itemsViewModel.langTo)
        bundle.putString("fromTxt", itemsViewModel.txtFrom)
        bundle.putString("toTxt", itemsViewModel.txtTo)
        bundle.putBoolean("fromHistory", true)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtras(bundle)
        context?.startActivity(intent)
    }

    private fun delItem(holder: ViewHolder){
        val dialog = Dialog(context!!)
        dialog.setContentView(R.layout.confirm_del_dialog)
        val noDelBtn = dialog.findViewById<Button>(R.id.noDelBtn)
        val yesDelBtn = dialog.findViewById<Button>(R.id.yesDelBtn)
        noDelBtn.setOnClickListener {
            dialog.dismiss()
        }
        yesDelBtn.setOnClickListener {
            val txtHist = tinyDB.getListObject(AppConfig.TEXT_HISTORY,HistoryItem::class.java)
            Log.d(TAG, "onBindViewHolder: list before \n$txtHist")
            txtHist.removeAt(holder.adapterPosition)
            Log.d(TAG, "onBindViewHolder: list after \n$txtHist")
            Log.d(TAG, "onBindViewHolder: $holder.adapterPosition")
            tinyDB.putListObject(AppConfig.TEXT_HISTORY,txtHist)
            notifyItemRemoved(holder.adapterPosition)
            mList = txtHist as List<HistoryItem>
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    override fun getItemCount(): Int {
      return  mList.size
    }
    fun updateData(mList: List<HistoryItem>){
        this.mList = mList
        notifyDataSetChanged()
    }
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val txtHistoryItem : MaterialCardView = itemView.findViewById(R.id.txtHistoryItem)
        val delBtn: ImageView = itemView.findViewById(R.id.delBtn)
        val langToFromTxt: TextView = itemView.findViewById(R.id.langToFromTxt)
        val toTxt: TextView = itemView.findViewById(R.id.toTxt)
        val fromTxt: TextView = itemView.findViewById(R.id.fromTxt)
    }
}