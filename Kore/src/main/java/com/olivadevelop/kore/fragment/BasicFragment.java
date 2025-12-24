package com.olivadevelop.kore.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.google.android.gms.ads.AdView;
import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.activity.BasicActivity;
import com.olivadevelop.kore.component.BasicComponentView;
import com.olivadevelop.kore.util.Utils;
import com.olivadevelop.kore.viewmodel.BasicViewModel;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import kotlinx.serialization.Serializable;
import lombok.Getter;

@Serializable
@SuppressWarnings("unchecked")
public abstract class BasicFragment<A extends BasicActivity<V, L>, V extends ViewBinding, L extends BasicViewModel<?>, T extends BasicViewModel<?>,
        B extends ViewBinding> extends Fragment implements View.OnClickListener {
    @Getter
    private T viewModel;
    @Getter
    private B binding;
    @Getter
    private View root;
    private final List<Runnable> listeners = new ArrayList<>();
    @Getter
    private final Set<AdView> adViews = new HashSet<>();
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = Utils.Reflex.initBinding(this, 4, inflater, container);
        this.root = this.binding.getRoot();
        onCreateViewInternal(inflater, this.root, savedInstanceState);
        configAdsVisibility();
        return this.root;
    }
    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.viewModel = new ViewModelProvider(this).get(Utils.Reflex.getClassTypeArgument(this, 3));
        this.viewModel.setCtx(this.getParentActivity());
        initCreated(view, savedInstanceState);
        view.post(() -> initCreatedPost(view, savedInstanceState));
        if (!this.listeners.isEmpty()) {
            this.listeners.forEach(Runnable::run);
            this.listeners.clear();
        }
    }

    @Override
    public void onClick(View v) { }
    protected void onCreateViewInternal(LayoutInflater inflater, @NonNull View view, @Nullable Bundle savedInstanceState) { }
    protected void initCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { }
    protected void initCreatedPost(@NonNull View view, @Nullable Bundle savedInstanceState) { }
    protected void configAdsVisibility() { }
    protected final A getParentActivity() { return (A) getActivity(); }
    public final void addPostViewCreatedListener(@NonNull Runnable runnable) { this.listeners.add(runnable); }
    protected final void addClickListener(View v) { if (v != null) { v.setOnClickListener(this); } }
    protected final List<BasicComponentView<?>> invalidComponents(LinearLayout wrapperLayoutStep) {
        List<BasicComponentView<?>> list = new ArrayList<>();
        for (int x = 0; x < wrapperLayoutStep.getChildCount(); x++) {
            View child = wrapperLayoutStep.getChildAt(x);
            if (child instanceof BasicComponentView<?>) {
                BasicComponentView<?> componentView = (BasicComponentView<?>) child;
                if (!(componentView).isValid()) { list.add(componentView); }
            }
        }
        return list;
    }
    protected final void setCleanComponents(LinearLayout wrapperLayoutStep) {
        for (int x = 0; x < wrapperLayoutStep.getChildCount(); x++) {
            View child = wrapperLayoutStep.getChildAt(x);
            if (child instanceof BasicComponentView<?>) { ((BasicComponentView<?>) child).setHasCleanPending(true); }
        }
    }
    protected final <U> Optional<U> getExtraValue(String key) {
        return getParentActivity() != null ? getParentActivity().getExtraValue(key) : Optional.empty();
    }
    @Override
    public void onPause() {
        if (!getAdViews().isEmpty()) { getAdViews().forEach(AdView::pause); }
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        viewModel.reload();
        if (!getAdViews().isEmpty()) { getAdViews().forEach(AdView::resume); }
    }
    @Override
    public void onDestroy() {
        if (!getAdViews().isEmpty()) {
            getAdViews().forEach(AdView::destroy);
            getAdViews().clear();
        }
        super.onDestroy();
    }
    private String buildHint(String id) {
        return Utils.translateStringIdFromResourceStrings(requireContext(), Constants.UI.LABEL_FORM + id, StringUtils.capitalize(id));
    }
}