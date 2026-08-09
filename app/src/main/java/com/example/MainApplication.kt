package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("youtubeprive-kids")
                    .setApplicationId("1:123456789012:android:1234567890abcdef")
                    .setApiKey("AIzaSyDemoKeyForKidsApp")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("MainApplication", "FirebaseApp initialized with default fallback options.")
            }
        } catch (e: Throwable) {
            Log.e("MainApplication", "Could not initialize FirebaseApp: ${e.message}")
        }
    }
}
