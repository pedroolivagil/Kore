package com.olivadevelop.kore.ads;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.viewbinding.ViewBinding;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.viewmodel.KoreViewModel;

public interface AdsManager {
    static void loadInterstitial(Context context, String id, Consumer<InterstitialAd> adConsumer) {
        InterstitialAd.load(context, id, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) { adConsumer.accept(interstitialAd); }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) { Log.e(Constants.Log.TAG, loadAdError.getMessage()); }
        });
    }
    static <T extends ViewBinding> void loadBannerAdvanced(Context context, String id, Consumer<NativeAd> adConsumer) {
        new Thread(() -> {
            AdLoader adLoader = new AdLoader.Builder(context, id).forNativeAd(adConsumer::accept).withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) { Log.e(Constants.Log.TAG, loadAdError.getMessage()); }
            }).withNativeAdOptions(new NativeAdOptions.Builder().build()).build();
        }).start();
    }
    <V extends KoreViewModel<?>, T extends ViewBinding> boolean shouldShowAds(KoreActivity<T, V> tvKoreActivity);
}