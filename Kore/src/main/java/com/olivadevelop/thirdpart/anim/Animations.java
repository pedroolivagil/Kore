package com.olivadevelop.thirdpart.anim;

import android.os.Handler;
import android.os.Looper;

import com.dotlottie.dlplayer.Mode;
import com.lottiefiles.dotlottie.core.model.Config;
import com.lottiefiles.dotlottie.core.util.DotLottieSource;
import com.lottiefiles.dotlottie.core.widget.DotLottieAnimation;
import com.olivadevelop.kore.Constants;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class Animations {
    public static final Animations Instance = new Animations();
    private final Map<String, Config> animations = new HashMap<>();
    public static void animate(DotLottieAnimation view, Config animation) { animate(view, animation, 0L, null); }
    public static void animate(DotLottieAnimation view, Config animation, long delayMillis) { animate(view, animation, delayMillis, null); }
    public static void animate(DotLottieAnimation view, Config animation, long delayMillis, CustomLottieEventListener eventListener) {
        if (view == null) { throw new RuntimeException("VIEW ANIMATION FAILED."); }
        if (animation == null) { throw new RuntimeException("ANIMATION FAILED."); }
        if (delayMillis < 0) { throw new RuntimeException("THE DELAY MUST BE GREATER THAN OR EQUAL TO 0."); }
        if (eventListener != null) { view.addEventListener(eventListener); }
        view.load(animation);
        new Handler(Looper.getMainLooper()).postDelayed(view::play, delayMillis);
    }
    private Animations() { init(); }

    public void init() {
        animations.put(Constants.Animations.INTRO, new Config.Builder()
                .themeId("INTRO ANIM")
                .autoplay(false)
                .speed(1)
                .loop(true)
                .source(new DotLottieSource.Asset("intro.json"))
                .playMode(Mode.FORWARD).build());
        animations.put(Constants.Animations.LOADER, new Config.Builder()
                .themeId("LOADER ANIM")
                .autoplay(false)
                .speed(1)
                .loop(true)
                .source(new DotLottieSource.Asset("loader.json"))
                .playMode(Mode.FORWARD).build());
    }
}
