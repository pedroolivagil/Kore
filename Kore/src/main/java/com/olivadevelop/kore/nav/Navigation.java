package com.olivadevelop.kore.nav;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.AnimRes;
import androidx.fragment.app.Fragment;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Data;

public final class Navigation {
    public static final Navigation Instance = new Navigation();
    private final List<HistoryData> history = new ArrayList<>();
    private Navigation() { }
    public interface NavigationScreen extends Serializable {
        Class<? extends Activity> getPage();
        Class<? extends Fragment> getFragment();
        Integer getFragmentWrapper();
        Integer getIdFragment();
        boolean isIgnoreHistory();
        Optional<? extends NavigationScreen> fromPageFragment(Class<? extends Activity> page, Class<? extends Fragment> fragment);
    }

    public interface AnimationScreenInOut extends Serializable {
        @AnimRes
        int getAnimOut();
        @AnimRes
        int getAnimIn();
    }
    public void navigate(NavigationData data) { navigateToActivity(data, true); }
    private void navigateToActivity(NavigationData data, boolean toHistory) {
        KoreActivity<?, ?> ctx = data.getContext();
        NavigationScreen from = data.getFrom();
        NavigationScreen to = data.getTo();
        Map<String, Object> map = new HashMap<>(data.getArgs());
        if (to.getFragment() != null) {
            map.put(Constants.Field.FRAGMENT_LOADING, to);
            map.put(Constants.Field.FRAGMENT_ARGUMENTS, Utils.Lombok.mapToBundle(map));
            data.setArgs(map);
        }
        Intent intent = new Intent(ctx, to.getPage());
        Utils.addArgsToIntent(data.getArgs(), intent);
        ctx.startActivity(intent);
        if (data.getAnimation() != null) { overrideAnimation(ctx, data.getAnimation()); }
        if (data.isFinishCurrent()) { ctx.finish(); }
        if (!toHistory || data.getFrom().isIgnoreHistory()) { return; }
        this.history.add(new HistoryData(this.history.size(), ctx, from, to, data.getArgs(), data));
    }
    private void navigateToActivity(KoreActivity<?, ?> ctx, HistoryData h) {
        NavigationData d = NavigationData.builder(ctx, h.to, h.from).args(h.args).build();
        this.navigateToActivity(d, false);
    }
    public void goBack(KoreActivity<?, ?> ctx, Map<String, Object> args) {
        HistoryData h = lastHistory();
        if (h == null) { return; }
        if (args != null) { args.forEach((key, value) -> h.getArgs().computeIfAbsent(key, k -> value)); }
        HistoryData entry = history.remove(history.size() - 1);
        if (!ctx.getClass().equals(entry.getCtx().getClass())) { navigateToActivity(ctx, h); }
    }
    public boolean canGoBack() { return !this.history.isEmpty(); }
    private HistoryData lastHistory() {
        return this.history.isEmpty() ? null : this.history.get(this.history.size() - 1);
    }
    private void overrideAnimation(Activity ctx, AnimationScreenInOut anim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.overridePendingTransition(anim.getAnimIn(), anim.getAnimOut(), android.R.color.white);
        } else {
            ctx.overridePendingTransition(anim.getAnimIn(), anim.getAnimOut());
        }
    }
    @Data
    private static class HistoryData {
        private final int order;
        private final KoreActivity<?, ?> ctx;
        private final NavigationScreen from;
        private final NavigationScreen to;
        private final NavigationData data;
        private final Map<String, Object> args = new HashMap<>();
        public HistoryData(int order, KoreActivity<?, ?> ctx, NavigationScreen from, NavigationScreen to, Map<String, Object> args, NavigationData data) {
            this.order = order;
            this.ctx = ctx;
            this.from = from;
            this.to = to;
            if (args != null) { this.args.putAll(args); }
            this.data = data;
        }
    }
}
