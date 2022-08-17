package com.sentry.myapplication

import androidx.multidex.MultiDexApplication

class TestApplication : MultiDexApplication() {

    companion object {
        lateinit var instance: TestApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}