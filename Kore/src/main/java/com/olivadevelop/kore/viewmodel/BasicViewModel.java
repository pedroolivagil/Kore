package com.olivadevelop.kore.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.component.BasicComponentView;
import com.olivadevelop.kore.db.entity.CustomEntity;
import com.olivadevelop.kore.error.InvalidPropertyErrorVM;
import com.olivadevelop.kore.nav.Navigation;
import com.olivadevelop.kore.util.Utils;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class BasicViewModel<T extends CustomEntity> extends ViewModel implements Cloneable {
    private final MutableLiveData<Navigation.NavigationScreen> screenBack = new MutableLiveData<>(null);

    private Context ctx;
    private T data;
    private boolean hasValidation = true;
    private Map<String, BasicComponentView<?>> componentViewMap = new HashMap<>();
    private Set<InvalidPropertyErrorVM> errors = new HashSet<>();
    public abstract boolean isValid();
    public abstract BasicViewModel<T> buildEntityData();

    @NonNull
    @Override
    public BasicViewModel<T> clone() {
        try {
            return (BasicViewModel<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    public void reload() {
        buildNewData();
    }
    public BasicViewModel() { buildNewData(); }
    public void clearData() { buildNewData(); }
    protected final void buildNewData() {
        try {
            this.data = Utils.Reflex.newInstance(Utils.Reflex.getClassTypeArgument(this));
        } catch (InstantiationException e) {
            Log.e(Constants.Log.TAG, "Error al crear los datos del viewmodel. " + e.getMessage());
        }
    }
    public final void copyDataViewModel(BasicViewModel<?> viewModel) {
        Predicate<? super Field> filter = f -> {
            String name = f.getName();
            return !name.equals("componentViewMap") && !name.equals("errors") && !name.equals("hasValidation") && !name.equals("step") && !name.equals("impl");
        };
        Set<Field> fields = new HashSet<>(FieldUtils.getAllFieldsList(viewModel.getClass()));
        fields.stream().filter(filter).forEach(f -> {
            try {
                f.setAccessible(true);
                f.set(this, f.get(viewModel));
            } catch (IllegalAccessException e) { Log.e(Constants.Log.TAG, "Error al recuperar el estado anterior del viewmodel. " + e.getMessage()); }
        });
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        BasicViewModel<?> that = (BasicViewModel<?>) o;
        return hasValidation == that.hasValidation && Objects.equals(componentViewMap, that.componentViewMap) && Objects.equals(errors, that.errors) && Objects.equals(data, that.data);
    }
    @Override
    public int hashCode() {
        return Objects.hash(componentViewMap, errors, hasValidation, data);
    }
}
