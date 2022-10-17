package com.anb.offlinetranslator.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.anb.offlinetranslator.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    lateinit var binding : ActivityPrivacyPolicyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.icBack.setOnClickListener{onBackPressed()}
    }
}