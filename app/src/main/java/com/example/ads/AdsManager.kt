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

    // Ad load hoye গেছে কিনা track kora hocche, noile show() call korle silently fail kore
    private var isInterstitialReady = false
    private var isRewardedReady = false

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
        isInterstitialReady = false
        UnityAds.load(INTERSTITIAL_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                isInterstitialReady = true
                Log.d(TAG, "Interstitial loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                isInterstitialReady = false
                Log.e(TAG, "Interstitial failed to load: $error - $message")
            }
        })
    }

    /**
     * Interstitial dekhay. Ready na thakle show() call na kore
     * shudhu abar load koray, jate porerbar dekhano jai - ekhon dekhabe na
     * kintu app crash/silent fail hobe na. onClosed shob khetreই call hoy
     * jate navigation atke na thake.
     */
    fun showInterstitial(activity: Activity, onClosed: () -> Unit = {}) {
        if (!isInterstitialReady) {
            Log.w(TAG, "Interstitial not ready yet, skipping show and reloading")
            loadInterstitial()
            onClosed()
            return
        }

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
                    Log.e(TAG, "Interstitial show failed: $error - $message")
                    isInterstitialReady = false
                    onClosed()
                    loadInterstitial()
                }

                override fun onUnityAdsShowStart(placementId: String) {
                    Log.d(TAG, "Interstitial show started")
                }

                override fun onUnityAdsShowClick(placementId: String) {}

                override fun onUnityAdsShowComplete(
                    placementId: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    isInterstitialReady = false
                    onClosed()
                    loadInterstitial() // porerbar er jonno abar load kore rakhun
                }
            }
        )
    }

    // ---------- REWARDED ----------

    fun loadRewarded() {
        isRewardedReady = false
        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                isRewardedReady = true
                Log.d(TAG, "Rewarded loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                isRewardedReady = false
                Log.e(TAG, "Rewarded failed to load: $error - $message")
            }
        })
    }

    fun showRewarded(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailedOrSkipped: () -> Unit = {}
    ) {
        if (!isRewardedReady) {
            Log.w(TAG, "Rewarded not ready yet, skipping show and reloading")
            loadRewarded()
            onFailedOrSkipped()
            return
        }

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
                    Log.e(TAG, "Rewarded show failed: $error - $message")
                    isRewardedReady = false
                    onFailedOrSkipped()
                    loadRewarded()
                }

                override fun onUnityAdsShowStart(placementId: String) {
                    Log.d(TAG, "Rewarded show started")
                }

                override fun onUnityAdsShowClick(placementId: String) {}

                override fun onUnityAdsShowComplete(
                    placementId: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    isRewardedReady = false
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onRewarded()
                    } else {
                        onFailedOrSkipped()
                    }
                    loadRewarded() // porerbar er jonno abar load kore rakhun
                }
            }
        )
    }
}

