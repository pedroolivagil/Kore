package com.olivadevelop.kore.nav;

import com.olivadevelop.kore.activity.KoreActivity;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NavigationData {
    private KoreActivity<?, ?> context;
    private Navigation.NavigationScreen from;
    private Navigation.NavigationScreen to;
    @Builder.Default
    private Navigation.AnimationScreenInOut animation = null;
    @Builder.Default
    private Map<String, Object> args = new HashMap<>();
    @Builder.Default
    private boolean finishCurrent = true;
    @Builder.Default
    private final boolean replaceFragment = true;

    public static NavigationDataBuilder builder(KoreActivity<?, ?> context, Navigation.NavigationScreen from, Navigation.NavigationScreen to) {
        return new NavigationDataBuilder().context(context).from(from).to(to);
    }
}
