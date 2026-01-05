package com.olivadevelop.kore.viewmodel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.KoreViewModelStatic;
import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.annotation.OrderProperty;
import com.olivadevelop.kore.annotation.OrderPropertyOnView;
import com.olivadevelop.kore.annotation.RegularExpressionField;
import com.olivadevelop.kore.annotation.RenderIgnoreView;
import com.olivadevelop.kore.component.ComponentProperty;
import com.olivadevelop.kore.component.KoreComponentView;
import com.olivadevelop.kore.db.dto.KoreDTO;
import com.olivadevelop.kore.db.entity.KoreEntity;
import com.olivadevelop.kore.error.InvalidPropertyErrorVM;
import com.olivadevelop.kore.nav.Navigation;
import com.olivadevelop.kore.util.Utils;
import com.olivadevelop.kore_annotations.StaticProperties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@SuppressWarnings("unchecked")
@StaticProperties(includeHierarchy = true, hierarchyLevel = 1)
public abstract class KoreViewModel<T extends KoreDTO<? extends KoreEntity>> extends ViewModel implements Cloneable {
    private final MutableLiveData<Navigation.NavigationScreen> screenBack = new MutableLiveData<>(null);
    private KoreActivity<?, ?> ctx;
    private T data;
    private boolean hasValidation = true;
    private Map<String, KoreComponentView<?>> componentViewMap = new HashMap<>();
    private Set<InvalidPropertyErrorVM> errors = new HashSet<>();
    public KoreViewModel() { buildNewData(); }
    public boolean isValid() { return false; }
    public KoreViewModel<T> buildEntityData() { return null; }
    public void reload() { buildNewData(); }
    public void clearData() { buildNewData(); }
    protected final void buildNewData() {
        try {
            this.data = Utils.Reflex.newInstance(Utils.Reflex.getClassTypeArgument(this));
        } catch (InstantiationException e) {
            Log.e(Constants.Log.TAG, "Error al crear los datos del viewmodel. " + e.getMessage());
        }
    }
    public final void copyDataViewModel(KoreViewModel<?> viewModel) {
        Predicate<? super Field> filter = f -> !KoreViewModelStatic.properties().contains(f.getName());
        Set<Field> fields = new HashSet<>(FieldUtils.getAllFieldsList(viewModel.getClass()));
        fields.stream().filter(filter).forEach(f -> {
            try {
                f.setAccessible(true);
                f.set(this, f.get(viewModel));
            } catch (IllegalAccessException e) { Log.e(Constants.Log.TAG, "Error al recuperar el estado anterior del viewmodel. " + e.getMessage()); }
        });
    }

    @NonNull
    @Override
    public KoreViewModel<T> clone() {
        try {
            return (KoreViewModel<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        KoreViewModel<?> that = (KoreViewModel<?>) o;
        return hasValidation == that.hasValidation && Objects.equals(componentViewMap, that.componentViewMap) && Objects.equals(errors, that.errors) && Objects.equals(data, that.data);
    }
    @Override
    public int hashCode() {
        return Objects.hash(componentViewMap, errors, hasValidation, data);
    }
    public final void buildComponentsFromProperties() {
        List<ComponentProperty> properties = getComponentPropertyList();
        properties.forEach(this::toComponentMap);
    }
    @NonNull
    private List<ComponentProperty> getComponentPropertyList() {
        List<ComponentProperty> properties = new ArrayList<>();
        Class<? extends KoreViewModel<T>> aClass = (Class<? extends KoreViewModel<T>>) this.getClass();
        Predicate<Field> filterIgnore = f -> {
            String name = f.getName();
            return !f.isAnnotationPresent(RenderIgnoreView.class) && !KoreViewModelStatic.properties().contains(name);
        };
        BiConsumer<OrderPropertyOnView, Field> fieldConsumer = (opov, f) -> {
            int order = 0;
            if (opov != null) {
                Optional<OrderProperty> optOrder = Arrays.stream(opov.value()).filter(op -> op.value().equals(f.getName())).findFirst();
                if (optOrder.isPresent()) { order = optOrder.get().position(); }
            }
            properties.add(ComponentProperty.builder() //Builder
                    .componentClass(extractFromParametrizedType(f))
                    .property(f.getName())
                    .annotations(Arrays.stream(f.getDeclaredAnnotations()).collect(Collectors.toList()))
                    .order(order)
                    .build());
        };
        OrderPropertyOnView opov = aClass.getDeclaredAnnotation(OrderPropertyOnView.class);
        FieldUtils.getAllFieldsList(aClass).stream().filter(filterIgnore).forEach(f -> fieldConsumer.accept(opov, f));
        properties.sort(Comparator.comparingInt(ComponentProperty::getOrder));
        return properties;
    }
    @NonNull
    private static Class<?> extractFromParametrizedType(Field f) { return Utils.Reflex.getClassTypeFromClassType(f.getGenericType()); }
    private void toComponentMap(ComponentProperty cp) {
        Class<? extends View> classComponent = Utils.Reflex.getViewFromTypeClass(cp.getComponentClass(), cp.getAnnotations());
        String id = cp.getProperty();
        try {
            View view = classComponent.getDeclaredConstructor(Context.class, AttributeSet.class).newInstance(getCtx(), null);
            if (view instanceof KoreComponentView) {
                KoreComponentView<?> component = (KoreComponentView<?>) view;
                component.setProperty(Map.of(cp.getComponentClass(), cp.getAnnotations())).setTag(id);
                component.setHint(buildHint(id));
                component.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                component.setVisibility(View.VISIBLE);
                component.setActivity(getCtx());
                component.setMandatory(true);
                cp.getAnnotations().stream().filter(a -> a instanceof RegularExpressionField).map(a -> (RegularExpressionField) a).findFirst().ifPresent(a -> {
                    component.setMandatory(a.mandatory());
                    component.setMaxLines(a.maxLines());
                    component.setMaxLength(a.maxLength());
                    component.setMinLength(a.minLength());
                    component.setImmediateValidation(a.inmediateValidation());
                    component.setRegexPattern(a.value());
                });
                getComponentViewMap().put(id, component);
            }
        } catch (Throwable e) {
            Log.e(Constants.Log.TAG, "Error al crear el componente '" + id + "' ('" + cp.getComponentClass() + "') del viewmodel. " + e.getMessage(), e);
        }
    }
    private String buildHint(String id) {
        return Utils.translateStringIdFromResourceStrings(getCtx(), Constants.UI.LABEL_FORM + id, StringUtils.capitalize(id));
    }
}
