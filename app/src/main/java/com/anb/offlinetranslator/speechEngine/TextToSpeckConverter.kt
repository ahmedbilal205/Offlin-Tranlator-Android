package com.anb.offlinetranslator.speechEngine
import android.app.Activity
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TextToSpeckConverter(private val conversionCallBack: ConversionCallback, private val locale: Locale)
    : TranslatorFactory.IConverter {
    private   val  TAG = "SpeechToTextConverter"
    private var textToSpeech: TextToSpeech? = null

    override fun initialize(message: String, appContext: Activity): TranslatorFactory.IConverter {
        textToSpeech = TextToSpeech(appContext, { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = locale
                textToSpeech?.setPitch(1f)
                textToSpeech?.setSpeechRate(0.8f)
                textToSpeech?.language = locale

                ttsGreater21(message)
            } else {
                conversionCallBack.onErrorOccurred("Failed to initialize TTS engine")
            }
        },"com.google.android.tts")
        return this
    }



    private fun finish() {
        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    private fun ttsGreater21(text: String) {
        val utteranceId = TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                conversionCallBack.onSuccess("")
                Log.d(TAG, "onDone: $utteranceId")
            }

            @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
            override fun onError(utteranceId: String?) {}

            override fun onError(utteranceId: String?, errorCode: Int) {
                conversionCallBack.onErrorOccurred("Failed to initialize TTS engine")
                Log.d(TAG, "onError: $utteranceId")
                super.onError(utteranceId, errorCode)
            }


            override fun onStart(utteranceId: String) {
                conversionCallBack.onStart()
                Log.d(TAG, "onStart: ")
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                Log.d(TAG, "onStop: ")
                super.onStop(utteranceId, interrupted)
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                Log.d(TAG, "onRangeStart:")
                super.onRangeStart(utteranceId, start, end, frame)
            }
            override fun onBeginSynthesis(
                utteranceId: String?,
                sampleRateInHz: Int,
                audioFormat: Int,
                channelCount: Int
            ) {
                Log.d(TAG, "onBeginSynthesis: ")
                conversionCallBack.onStart()
                super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount)
            }
        })

    }


    override fun getErrorText(errorCode: Int): String {
        val message: String = when (errorCode) {
            TextToSpeech.ERROR -> "Generic error"
            TextToSpeech.ERROR_INVALID_REQUEST -> "Client side error, invalid request"
            TextToSpeech.ERROR_NOT_INSTALLED_YET -> "Insufficient download of the voice data"
            TextToSpeech.ERROR_NETWORK -> "Network error"
            TextToSpeech.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            TextToSpeech.ERROR_OUTPUT -> "Failure in to the output (audio device or a file)"
            TextToSpeech.ERROR_SYNTHESIS -> "Failure of a TTS engine to synthesize the given input."
            TextToSpeech.ERROR_SERVICE -> "error from server"
            else -> "Didn't understand, please try again."
        }
        return message
    }

}