package com.anb.offlinetranslator.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.anb.offlinetranslator.databinding.FragmentFullScreenBinding
import com.anb.offlinetranslator.utils.Utils

class FullScreenFragment() : Fragment() {
    private var txt = ""
    constructor(txt:String):this(){
        this.txt = txt
    }
    lateinit var binding: FragmentFullScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFullScreenBinding.inflate(inflater, container, false)
        binding.fullTxt.text = txt
        binding.copyFullScreen.setOnClickListener {
            context?.let { it1 -> Utils.copyText(it1,binding.fullTxt.text.toString()) }
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        binding.icBack.setOnClickListener { activity?.onBackPressed() }
        return binding.root
    }
}