package com.anb.offlinetranslator.views.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.view.GravityCompat
import androidx.work.*
import com.anb.offlinetranslator.BuildConfig
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.anb.offlinetranslator.data.LanguageItem
import com.anb.offlinetranslator.data.LanguagesList
import com.anb.offlinetranslator.data.HistoryItem
import com.anb.offlinetranslator.databinding.ActivityMainBinding
import com.anb.offlinetranslator.databinding.DashboardBinding
import com.anb.offlinetranslator.databinding.NavigationHeaderBinding
import com.anb.offlinetranslator.utils.AppConfig
import com.anb.offlinetranslator.utils.TinyDB
import com.anb.offlinetranslator.utils.Utils
import com.anb.offlinetranslator.views.fragments.FullScreenFragment
import com.anb.offlinetranslator.views.fragments.SelectLanguageFragment
import com.anb.offlinetranslator.workers.ModelDownloadWorker
import java.util.*
import com.anb.offlinetranslator.R

class MainActivity : AppCompatActivity(), SelectLanguageFragment.LangSelectedCallback {
    lateinit var mainBinding: ActivityMainBinding
    lateinit var dashBinding: DashboardBinding
    lateinit var drawerBinding: NavigationHeaderBinding
    lateinit var langFromA: LanguageItem
    lateinit var langToA: LanguageItem
    lateinit var tinyDB: TinyDB
    private val TAG = "TextTranslator"
    var bundle: Bundle? = Bundle()
    private val REQUEST_CODE_SPEECH_INPUT = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        dashBinding = mainBinding.dashboard
        drawerBinding = mainBinding.navigationDrawer
        setContentView(mainBinding.root)
        tinyDB = TinyDB(this)
        initDrawerViews()
        initDashViews()
        preSelectLang()
        bundle = intent.extras
        if (bundle!=null) {
            if (bundle!!.getBoolean("fromHistory")){
                fillHistoryData()
            }
        }
        val filter = IntentFilter()
        filter.addAction("DOWNLOAD_COMPLETE")
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        this.registerReceiver(mMessageReceiver, filter)
    }

    private fun initDashViews() {
        dashBinding.apply {
            icDrawer.setOnClickListener {
                mainBinding.drawerLayout.openDrawer(GravityCompat.START)
            }
            pasteTxt.setOnClickListener {
                translateEdt.append(Utils.pasteTxt(this@MainActivity))
            }
            translateOutputTv.movementMethod = ScrollingMovementMethod()//make textview scrollable
            langFromCard.setOnClickListener {
                Utils.hideKeyBoard(this@MainActivity)
                selectLanguageFrame.visibility = View.VISIBLE
                supportFragmentManager.beginTransaction()
                    .add(selectLanguageFrame.id,
                        SelectLanguageFragment("from",this@MainActivity)).commit()
            }
            langToCard.setOnClickListener {
                Utils.hideKeyBoard(this@MainActivity)
                selectLanguageFrame.visibility = View.VISIBLE
                supportFragmentManager.beginTransaction()
                    .add(selectLanguageFrame.id,
                        SelectLanguageFragment("to",this@MainActivity)).commit()
            }
            switchLangs.setOnClickListener { switchLangs() }
            tranlateBtn.setOnClickListener {
                if (::langFromA.isInitialized
                    &&::langToA.isInitialized)
                {
                    if (TextUtils.isEmpty(translateEdt.text.toString())) {
                        Toast.makeText(this@MainActivity, "Enter some text",
                            Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    translateTxt()
                }else{
                    Toast.makeText(this@MainActivity, "Select both languages",
                        Toast.LENGTH_SHORT).show()
                }
            }
            resetViews.setOnClickListener {
                resetViews.visibility = View.GONE
                tranlateBtn.visibility = View.GONE
                translateEdt.setText("")
            }
            copyTranslatedTxt.setOnClickListener {
                Utils.copyText(this@MainActivity,translateOutputTv.text.toString())
                Toast.makeText(this@MainActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            shareTxt.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                val shareBody = translateOutputTv.text.toString() +"\nText translated using : https://play.google.com/store/apps/details?id="+ BuildConfig.APPLICATION_ID
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Audio translator")
                intent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(intent,"Share"))
            }
            fullScreenTxt.setOnClickListener {
                selectLanguageFrame.visibility = View.VISIBLE
                supportFragmentManager.beginTransaction()
                    .add(selectLanguageFrame.id,
                        FullScreenFragment(translateOutputTv.text.toString())).commit()
            }
            speechBtn.setOnClickListener { initSpeechToTxt() }
        }

        translateTextWatcher()
    }

    private fun initSpeechToTxt() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, langFromA.code)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(
                    this@MainActivity, "Error, check connection\nOnly text translations work offline" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun translateTextWatcher() {
        dashBinding.translateEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.isNotEmpty() == true)
                {
                    dashBinding.resetViews.visibility = View.VISIBLE
                    dashBinding.tranlateBtn.visibility = View.VISIBLE
                }else{
                    dashBinding.resetViews.visibility = View.GONE
                    dashBinding.tranlateBtn.visibility = View.GONE
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun initDrawerViews() {
        drawerBinding.apply {
            navDownloadLang.setOnClickListener {
                startActivity(Intent(this@MainActivity,DownloadLanguagesActivity::class.java))
            }

            navHistory.setOnClickListener {
                mainBinding.drawerLayout.closeDrawers()
                startActivity(Intent(this@MainActivity,HistoryActivity::class.java))
            }

            navTermsAndPrivacy.setOnClickListener {
                mainBinding.drawerLayout.closeDrawers()
                startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
            }
            navShare.setOnClickListener {
                ShareCompat.IntentBuilder(this@MainActivity)
                    .setType("text/plain")
                    .setChooserTitle("You can now translate any language completely offline, check out this app")
                    .setText("http://play.google.com/store/apps/details?id=" + this@MainActivity.packageName)
                    .startChooser();
            }
            navRateUs.setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                }
            }
        }

    }
    private fun preSelectLang(){
        //TODO add settings with option to select default startup language
        //english and spanish are default
        langFromA =    LanguageItem("ENGLISH","en","gb")
        dashBinding.langFromTv.text = langFromA.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
    private fun switchLangs(){
        if (::langFromA.isInitialized
            &&::langToA.isInitialized){
            val tempLang = langFromA
            langFromA = langToA
            langToA = tempLang
            dashBinding.langFromTv.text = langFromA.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            dashBinding.langToTv.text = langToA.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }else Toast.makeText(this, "Select both languages", Toast.LENGTH_SHORT).show()

    }

    private fun translateTxt() {
        Utils.hideKeyBoard(this@MainActivity)
        dashBinding.resetViews.visibility = View.VISIBLE

        if (!tinyDB.getListString(AppConfig.DOWNLOADED_LANGS as String).contains(langFromA.code))
        {
            val data = Data.Builder()
            data.putString("lang", langFromA.code)
            data.putString("langName",langFromA.name)
            val langDownloadConditions = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(data.build())
                .build()
            WorkManager.getInstance(this).enqueue(langDownloadConditions)
            if (Utils.isOnline(this))
                Toast.makeText(this, "Language not downloaded, downloading now", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Internet not available, unable to download language", Toast.LENGTH_SHORT).show()
            return
        }
        if (!tinyDB.getListString(AppConfig.DOWNLOADED_LANGS).contains(langToA.code))
        {
            val data = Data.Builder()
            data.putString("lang", langToA.code)
            data.putString("langName",langToA.name)
            val langDownloadConditions = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(data.build())
                .build()
            WorkManager.getInstance(this).enqueue(langDownloadConditions)
            if (Utils.isOnline(this))
                Toast.makeText(this, "Language not downloaded, downloading now", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "No Internet, unable to download language", Toast.LENGTH_SHORT).show()
            return
        }

        dashBinding.translateOutputTv.text = getString(R.string.tranlating)

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(langFromA.code)
            .setTargetLanguage(langToA.code)
            .build()
        Log.d(TAG, "translateTxt: "+langFromA.code+"\n"+langToA.code)
        val langTranslator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        langTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                langTranslator.translate(dashBinding.translateEdt.text.toString())
                    .addOnSuccessListener { translatedText ->
                        dashBinding.translateOutputTv.text = translatedText

                        val txtHist = tinyDB.getListObject(AppConfig.TEXT_HISTORY, HistoryItem::class.java)
                        txtHist.add(HistoryItem(langFromA.name, langToA.name,dashBinding.translateEdt.text.toString(),translatedText))
                        tinyDB.putListObject(AppConfig.TEXT_HISTORY,txtHist)
                    }
                    .addOnFailureListener { exception ->
                        dashBinding.translateOutputTv.text = exception.localizedMessage
                    }

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "onCreate: ex2 $exception")
                dashBinding.translateOutputTv.text = exception.localizedMessage
            }
        val translator = Translation.getClient(options)
        lifecycle.addObserver(translator)
    }



    private fun fillHistoryData() {
        if (bundle==null)return
        dashBinding.translateEdt.setText( bundle!!.getString("fromTxt"))
        dashBinding.translateOutputTv.text = bundle!!.getString("toTxt")
        for (x in LanguagesList.languages) {
            if (x.name.lowercase().trim() == bundle!!.getString("langFrom")?.lowercase()?.trim()) {
                langFromA = x
            }
            if (x.name.lowercase().trim()==bundle!!.getString("langTo")?.lowercase()?.trim()) {
                langToA = x
            }
        }
        dashBinding.resetViews.visibility = View.VISIBLE
        dashBinding.langFromTv.text = langFromA.name
        dashBinding.langToTv.text = langToA.name
    }
    override fun onBackPressed() {
        if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainBinding.drawerLayout.closeDrawers()
            return
        }
        if (dashBinding.selectLanguageFrame.visibility == View.VISIBLE) {
            dashBinding.selectLanguageFrame.visibility = View.GONE
            return
        }
        super.onBackPressed()
    }

    override fun langFrom(langFrom: LanguageItem) {
        this.langFromA = langFrom
        tinyDB.putObject(AppConfig.RECENTLY_USED_FIRST,langFrom)
        dashBinding.langFromTv.text  =langFrom.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    override fun langTo(langTo: LanguageItem) {
        this.langToA = langTo
        tinyDB.putObject(AppConfig.RECENTLY_USED_SECOND, langTo)
        dashBinding.langToTv.text = langTo.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
                Toast.makeText(this@MainActivity, "Downloaded: "
                        +intent.getStringExtra("lang"), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val res: ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                dashBinding.translateEdt.append( " "+Objects.requireNonNull(res)[0])
            }
        }
    }
}