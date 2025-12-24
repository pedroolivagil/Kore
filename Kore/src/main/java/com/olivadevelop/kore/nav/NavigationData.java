package com.olivadevelop.kore.nav;

import com.olivadevelop.kore.activity.BasicActivity;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NavigationData {
    private BasicActivity<?, ?> context;
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

    public static NavigationDataBuilder builder(BasicActivity<?, ?> context, Navigation.NavigationScreen from, Navigation.NavigationScreen to) {
        return new NavigationDataBuilder().context(context).from(from).to(to);
    }
}
