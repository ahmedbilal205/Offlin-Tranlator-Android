package com.anb.offlinetranslator.views.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.anb.offlinetranslator.R
import com.anb.offlinetranslator.databinding.FragmentFullScreenBinding
import com.anb.offlinetranslator.speechEngine.ConversionCallback
import com.anb.offlinetranslator.speechEngine.TranslatorFactory
import com.anb.offlinetranslator.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FullScreenFragment() : Fragment() {
    private var txt = ""
    private var langCode = "en"
    constructor(txt:String):this(){
        this.txt = txt
    }
    constructor(txt:String, langCode: String):this(){
        this.txt = txt
        this.langCode = langCode
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
        binding.speechBtn.setOnClickListener{
            speakTranslatedTxt()
        }
        return binding.root
    }

    private fun speakTranslatedTxt() {
        binding.speechBtnProgress.visibility =View.VISIBLE
        binding.speechBtnTxt.visibility = View.VISIBLE
        TranslatorFactory.instance.with(TranslatorFactory.TRANSLATORS.TEXT_TO_SPEECH,
            object : ConversionCallback {
                override fun onSuccess(result: String) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("callbacks", "onSuccess: ")
                        binding.speechBtn.scaleX = 1F
                        binding.speechBtn.scaleY = 1F
                        binding.speechBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary))
                        binding.speechBtn.isClickable = true
                    }
                }

                override fun onCompletion() {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("callbacks", "onCompletion: ")
                        binding.speechBtn.scaleX = 1F
                        binding.speechBtn.scaleY = 1F
                        binding.speechBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary))
                        binding.speechBtn.isClickable = true
                    }
                }

                override fun onStart() {
                    Log.d("callbacks", "onStart: ")
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.speechBtnProgress.visibility =View.GONE
                        binding.speechBtnTxt.visibility = View.GONE
                        binding.speechBtn.scaleX = 1.4F
                        binding.speechBtn.scaleY = 1.4F
                        binding.speechBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
                        binding.speechBtn.isClickable = false
                    }
                }

                override fun onErrorOccurred(errorMessage: String) {
                    Log.d("callbacks", "onErrorOccurred: $errorMessage")
                    CoroutineScope(Dispatchers.Main).launch {
                    }
                }
            },
            Locale(langCode)).initialize(binding.fullTxt.text.toString(),requireActivity())
    }
}