package com.olivadevelop.kore.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.lottiefiles.dotlottie.core.widget.DotLottieAnimation;
import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.R;
import com.olivadevelop.kore.databinding.LoaderWrapperBinding;
import com.olivadevelop.kore.error.FormErrorException;
import com.olivadevelop.kore.media.CameraPermissionProvider;
import com.olivadevelop.kore.nav.Navigation;
import com.olivadevelop.kore.security.PermissionContract;
import com.olivadevelop.kore.security.PermissionManager;
import com.olivadevelop.kore.ui.SnackbarBuilder;
import com.olivadevelop.kore.util.ServiceInjector;
import com.olivadevelop.kore.util.Utils;
import com.olivadevelop.kore.viewmodel.KoreViewModel;
import com.olivadevelop.kore.viewmodel.SimpleTextWatcher;
import com.olivadevelop.thirdpart.anim.Animations;
import com.olivadevelop.thirdpart.anim.CustomLottieEventListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@SuppressWarnings("unchecked")
public abstract class KoreActivity<T extends ViewBinding, V extends KoreViewModel<?>> extends AppCompatActivity implements View.OnClickListener,
        CameraPermissionProvider {
    public interface OnActivityResultListener {
        void run(int requestCode, int resultCode, @Nullable Intent data);
    }

    private long lastBackPressTime = 0;
    private static final long DOUBLE_BACK_INTERVAL = 1200; // ms

    @Setter
    private OnActivityResultListener listener;
    @Setter
    private GestureDetector gestureDetector;

    private T binding;
    private V viewModel;
    private PermissionManager<? extends PermissionContract> permissionManager;
    private Bundle extras;
    @Getter
    private final Set<AdView> adViews = new HashSet<>();
    @Setter(AccessLevel.PROTECTED)
    private NavigationBarView navigationView;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceInjector.inject(this, this);
        this.extras = getIntent().getExtras();
        this.permissionManager = new PermissionManager<>(this);
        this.binding = Utils.Reflex.initBinding(this);
        this.viewModel = initViewModel(Utils.Reflex.getClassTypeArgument(this, 1));
        this.viewModel.setCtx(this);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            setSystemBarsInsets(v, insets);
            return insets;
        });
        init(savedInstanceState);
        binding.getRoot().post(() -> initPost(savedInstanceState));
        getExtras().stream().filter(e -> e.keySet().contains(Constants.Field.SCREEN_BACK)).findFirst().ifPresent(e -> {
            Navigation.NavigationScreen back;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                back = e.getSerializable(Constants.Field.SCREEN_BACK, Navigation.NavigationScreen.class);
            } else {
                back = (Navigation.NavigationScreen) e.getSerializable(Constants.Field.SCREEN_BACK);
            }
            this.viewModel.getScreenBack().setValue(back);
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { handleBackPress(); }
        });
        initListeners();
    }
    protected void setSystemBarsInsets(View v, WindowInsetsCompat insets) {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        getOptNavigationView().ifPresent(nav -> {
            Optional<Navigation.NavigationScreen> fragmentLoader = getExtraValue(Constants.Field.FRAGMENT_LOADING);
            fragmentLoader.ifPresent(fgmt -> nav.setSelectedItemId(fgmt.getIdFragment()));
        });
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
    @Override
    public void onClick(View v) { }

    protected void init(Bundle savedInstanceState) { }
    protected void initPost(Bundle savedInstanceState) { }
    protected void configAdsVisibility() { }
    public void goBack(Class<? extends Fragment> fragment, Map<String, Object> args) { }
    public void scrollTo(int i, int bottom) { }
    protected void initListeners() { }
    protected void validations() { }
    protected void validateFailed() { }
    public final View getRoot() { return binding.getRoot(); }
    protected final void addClickListener(View v) { if (v != null) { v.setOnClickListener(this); } }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (listener != null) { listener.run(requestCode, resultCode, data); }
    }
    protected final void addTextChangedListener(TextInputEditText editName, MutableLiveData<String> viewModel) {
        editName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setValue(s.toString());
            }
        });
    }
    protected final <O> void addTextChangedListener(TextInputEditText editName, MutableLiveData<O> viewModel, Function<O, String> transformer) {
        editName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setValue((O) transformer.apply(viewModel.getValue()));
            }
        });
    }
    protected final void addTextValidation(TextInputEditText editText, TextInputLayout layout, Supplier<Boolean> validator, String errorMessage) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (validator.get()) {
                    layout.setError(null);
                    layout.setBoxStrokeColor(getColor(R.color.black));
                } else {
                    layout.setError(errorMessage);
                    layout.setBoxStrokeColor(getColor(android.R.color.holo_red_light));
                }
            }
        });
    }
    protected final void validateEdit(boolean viewModel, TextInputLayout layoutName, String invalidNameError) {
        if (!viewModel) { layoutName.setError(invalidNameError); } else { layoutName.setError(null); }
    }
    public final V initViewModel(Class<V> viewModel) { return new ViewModelProvider(this).get(viewModel); }
    public Optional<Bundle> getExtras() { return Optional.ofNullable(this.extras); }
    public Optional<NavigationBarView> getOptNavigationView() { return Optional.ofNullable(this.navigationView); }
    protected LoaderWrapperBinding getLayoutLoaderWrapper() { return null; }
    public void goBack(Map<String, Object> args) { Navigation.Instance.goBack(this, args); }
    protected void showExitConfirmationDialog() {
        new MaterialAlertDialogBuilder(this).setTitle(R.string.exit_app_title).setMessage(R.string.exit_app_message).setPositiveButton(R.string.btn_exit,
                (dialog, which) -> finishAffinity()).setNegativeButton(R.string.btn_cancel, null).show();
    }
    protected void validateAndCreate() throws FormErrorException {
        if (!viewModel.isValid()) {
            validateFailed();
            SnackbarBuilder.with(getBinding().getRoot()).message(R.string.needs_correction_fields).show();
            throw new FormErrorException(getString(R.string.needs_correction_fields));
        }
    }
    protected void handleBackPress() {
        if (Navigation.Instance.canGoBack()) {
            Navigation.Instance.goBack(this, null);
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastBackPressTime < DOUBLE_BACK_INTERVAL) {
            finishAffinity(); // cierra la app
            return;
        }
        lastBackPressTime = now;
        showExitConfirmationDialog();
    }
    public final <U> Optional<U> getExtraValue(String extraKey) {
        Optional<U> result = Optional.empty();
        if (getExtras().isPresent()) {
            Bundle bundle = getExtras().get();
            if (bundle.containsKey(extraKey)) {
                Object o = bundle.get(extraKey);
                if (o != null) { result = Optional.of((U) o); }
            }
        }
        return result;
    }
    public final void runWithLoader(ConstraintLayout loaderWrapper, DotLottieAnimation loaderAnim, Consumer<CustomLottieEventListener> listener) {
        loaderWrapper.setTranslationZ(9999);
        loaderWrapper.setVisibility(View.VISIBLE);
        loaderWrapper.post(() ->
                Animations.animate(loaderAnim, Animations.Instance.getLoaderAnim(), 250L, new CustomLottieEventListener() {
                    @Override
                    public void onPlay() {
                        if (listener != null) {
                            loaderAnim.removeEventListener(this);
                            listener.accept(this);
                            loaderWrapper.animate().setStartDelay(2500).alpha(0f).setDuration(150).withEndAction(() -> {
                                loaderWrapper.setTranslationZ(0);
                                loaderWrapper.setVisibility(View.GONE);
                            }).start();
                        }
                    }
                }));
    }
    public final void loadFragment(Navigation.NavigationScreen screen, Bundle args) {
        Fragment fragment;
        try {
            fragment = screen.getFragment().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot instantiate fragment: " + screen.getFragment(), ex);
        }
        if (args != null) { fragment.setArguments(args); }
        getSupportFragmentManager().beginTransaction().replace(screen.getFragmentWrapper(), fragment).commit();
    }
}
