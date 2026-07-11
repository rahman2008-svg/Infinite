package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

object AdsManager {

    private const val TAG = "AdsManager"

    // Unity Dashboard theke paoa Game ID
    private const val GAME_ID = "800084106"
    private const val TEST_MODE = true // Test build - release deoar age false korte hobe

    // Unity Dashboard > Monetization > Placements theke asha Placement ID
    const val BANNER_PLACEMENT_ID = "Banner_Android"
    const val INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android"
    const val REWARDED_PLACEMENT_ID = "Rewarded_Android"

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        UnityAds.initialize(
            context,
            GAME_ID,
            TEST_MODE,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    isInitialized = true
                    Log.d(TAG, "Unity Ads initialized successfully")
                    // Initialize howa matro interstitial o rewarded ad preload kore rakha bhalo
                    loadInterstitial()
                    loadRewarded()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Ads init failed: $error - $message")
                }
            }
        )
    }

    fun isReady(): Boolean = isInitialized

    // ---------- INTERSTITIAL ----------

    fun loadInterstitial() {
        UnityAds.load(INTERSTITIAL_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Interstitial loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                Log.e(TAG, "Interstitial failed to load: $message")
            }
        })
    }

    fun showInterstitial(activity: Activity, onClosed: () -> Unit = {}) {
        UnityAds.show(
            activity,
            INTERSTITIAL_PLACEMENT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    Log.e(TAG, "Interstitial show failed: $message")
                    onClosed()
                    loadInterstitial()
                }

                override fun onUnityAdsShowStart(placementId: String) {}

                override fun onUnityAdsShowClick(placementId: String) {}

                override fun onUnityAdsShowComplete(
                    placementId: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    onClosed()
                    loadInterstitial() // porerbar er jonno abar load kore rakhun
                }
            }
        )
    }

    // ---------- REWARDED ----------

    fun loadRewarded() {
        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Rewarded loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                Log.e(TAG, "Rewarded failed to load: $message")
            }
        })
    }

    fun showRewarded(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailedOrSkipped: () -> Unit = {}
    ) {
        UnityAds.show(
            activity,
            REWARDED_PLACEMENT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    Log.e(TAG, "Rewarded show failed: $message")
                    onFailedOrSkipped()
                    loadRewarded()
                }

                override fun onUnityAdsShowStart(placementId: String) {}

                override fun onUnityAdsShowClick(placementId: String) {}

                override fun onUnityAdsShowComplete(
                    placementId: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onRewarded()
                    } else {
                        onFailedOrSkipped()
                    }
                    loadRewarded()
                }
            }
        )
    }
}
