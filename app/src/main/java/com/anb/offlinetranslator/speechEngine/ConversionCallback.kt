package com.anb.offlinetranslator.speechEngine

interface ConversionCallback {

    fun onSuccess(result: String)

    fun onCompletion()

    fun onStart()

    fun onErrorOccurred(errorMessage: String)

}