package com.olivadevelop.kore.ui;

import android.view.MotionEvent;
import android.view.View;

public final class SwipeHandler {
    private static final float SWIPE_THRESHOLD = 150f;
    private static final float MAX_DRAG = 300f;
    private static final long ANIM_DURATION = 350L;

    public interface SwipeAction {
        void run();
    }

    public interface SwipeCheck {
        boolean check();
    }
    private SwipeHandler() { }
    public static void attach(View targetView, SwipeAction onPrev, SwipeAction onNext, SwipeCheck canPrev, SwipeCheck canNext) {
        final float[] downX = new float[1];
        final boolean[] isDragging = new boolean[1];
        targetView.setOnTouchListener((v, event) -> {
            v.performClick();
            return action(onPrev, onNext, canPrev, canNext, v, event, downX, isDragging);
        });
    }
    private static boolean action(SwipeAction onPrev, SwipeAction onNext, SwipeCheck canPrev, SwipeCheck canNext, View v, MotionEvent event, float[] downX,
            boolean[] isDragging) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX[0] = x;
                isDragging[0] = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - downX[0];
                if (Math.abs(deltaX) > 10) {
                    isDragging[0] = true;
                }
                if (isDragging[0]) {
                    float limited = Math.max(-MAX_DRAG, Math.min(MAX_DRAG, deltaX));
                    v.setTranslationX(limited);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!isDragging[0]) { return true; }
                float totalDelta = x - downX[0];
                if (Math.abs(totalDelta) > SWIPE_THRESHOLD) {
                    if (totalDelta < 0) {
                        if (canNext.check()) {
                            animateSwipeAndPerform(v, true, onNext);
                        } else {
                            bounceBack(v);
                        }
                    } else {
                        if (canPrev.check()) {
                            animateSwipeAndPerform(v, false, onPrev);
                        } else {
                            bounceBack(v);
                        }
                    }
                } else {
                    v.animate().translationX(0).setDuration(ANIM_DURATION).start();
                }
                return true;
        }
        return false;
    }

    private static void animateSwipeAndPerform(View v, boolean forward, SwipeAction action) {
        float end = forward ? -v.getWidth() : v.getWidth();
        v.animate().translationX(end).setDuration(ANIM_DURATION).withEndAction(() -> {
            // Acción del usuario (cambiar día)
            action.run();
            // Ponerlo en el lado opuesto
            v.setTranslationX(-end);
            // Volver al centro
            v.animate().translationX(0).setDuration(ANIM_DURATION).start();
        }).start();
    }

    private static void bounceBack(View v) {
        v.animate().translationX(v.getTranslationX() * 0.25f).setDuration(100).withEndAction(() -> v.animate().translationX(0).setDuration(150).start());
    }
}