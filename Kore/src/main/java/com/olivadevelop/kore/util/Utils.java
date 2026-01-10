package com.olivadevelop.kore.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.imageview.ShapeableImageView;
import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.R;
import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.annotation.CustomViewRender;
import com.olivadevelop.kore.annotation.RegularExpressionField;
import com.olivadevelop.kore.component.CustomEditNumberView;
import com.olivadevelop.kore.component.CustomEditTextView;
import com.olivadevelop.kore.component.CustomSwitchView;

import org.apache.commons.lang3.time.DateUtils;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public abstract class Utils {
    private static final String DEVICE_NAME = "device_name";
    private static final String BLUETOOTH_NAME = "bluetooth_name";

    public static class Files {
        public static boolean clearInternalDirectory(Context ctx, String relativePath) {
            relativePath = relativePath.replaceFirst("^/", "");
            File target = new File(ctx.getFilesDir(), relativePath);
            if (!target.exists()) { return true; }
            File[] children = target.listFiles();
            if (children != null) {
                for (File f : children) {
                    if (!deleteRecursive(f)) { return false; }
                }
            }
            return true;
        }
        public static boolean deleteInternalDirectory(Context ctx, String relativePath) {
            if (relativePath == null) { return false; }
            relativePath = relativePath.replaceFirst("^/", "");
            File base = ctx.getFilesDir(); // o ctx.getCacheDir() según tu caso
            File targetDir = new File(base, relativePath);
            if (!targetDir.exists()) { return true; }
            if (!targetDir.isDirectory()) { return false; }
            return deleteRecursive(targetDir);
        }
        private static boolean deleteRecursive(File file) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children != null) {
                    for (File child : children) {
                        if (!deleteRecursive(child)) { return false; }
                    }
                }
            }
            return file.delete();
        }
        public static Bitmap blurBitmap(Context context, Bitmap bitmap, float radius) {
            float BITMAP_SCALE = 0.4f;
            float BLUR_RADIUS = 7.5f;
            int width = Math.round(bitmap.getWidth() * BITMAP_SCALE);
            int height = Math.round(bitmap.getHeight() * BITMAP_SCALE);
            Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
            RenderScript rs = RenderScript.create(context);
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);
            return outputBitmap;
        }
    }

    public static class DateTime {
        public static String formatDate(Date date) { return formatDate(date, "dd-MM-yyyy"); }
        public static String formatDate(Date date, String pattern) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            return sdf.format(date);
        }
        public static Date toDate(String value) {
            try {
                return DateUtils.parseDate(value, "dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "yyyy/MM/dd");
            } catch (ParseException e) {
                Log.e(Constants.Log.TAG, "Error al convertir fecha: " + e.getMessage(), e);
                return null;
            }
        }
        public static LocalDate fromTimestampToDate(long timestamp) {
            return fromTimestamp(timestamp).toLocalDate();
        }
        public static LocalDateTime fromTimestamp(long timestamp) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        }
        public static long fromLocalDateTime(LocalDateTime dateTime) { return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(); }
        public static long fromLocalDate(LocalDate dateTime) { return Date.from(dateTime.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime(); }
        public static long daysBetween(Date startDate, Date endDate) {
            if (startDate == null || endDate == null) { throw new IllegalArgumentException("Las fechas no pueden ser nulas"); }
            LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            // Diferencia en días (puede ser negativa si la primera fecha es posterior)
            return ChronoUnit.DAYS.between(start, end);
        }
        public static LocalDateTime combineDateAndTime(Date datePart, Date timePart) {
            if (datePart == null && timePart == null) { return null; }
            // Si no hay fecha, se usa la de hoy
            LocalDate localDate = datePart != null ? datePart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now();
            // Si no hay hora, se usa 00:00
            LocalTime localTime = timePart != null ? timePart.toInstant().atZone(ZoneId.systemDefault()).toLocalTime() : LocalTime.MIDNIGHT;
            return LocalDateTime.of(localDate, localTime);
        }
        public static boolean isBefore(Date date, Date when) { return date.before(when); }
        public static boolean isAfter(Date date, Date when) { return date.after(when); }
        public static boolean isBeforeOrEquals(Date date, Date when) { return date.equals(when) || date.before(when); }
        public static boolean isAfterOrEquals(Date date, Date when) { return date.equals(when) || date.after(when); }
        public static Date truncateTime(Date d) { return DateUtils.truncate(d, Calendar.DAY_OF_MONTH); }
    }
    public static int getDimen(Context ctx, int pixels) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, ctx.getResources().getDisplayMetrics());
    }
    public static String getDeviceName(Context ctx) {
        String name = Settings.Global.getString(ctx.getContentResolver(), DEVICE_NAME);
        if (name == null || name.isEmpty()) { name = Settings.Secure.getString(ctx.getContentResolver(), BLUETOOTH_NAME); }
        if (name == null || name.isEmpty()) { name = android.os.Build.MODEL + " " + android.os.Build.MANUFACTURER; }
        return name;
    }
    public static String getVersionName(Context ctx) {
        String versionName = "";
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.Log.TAG, "GetVersionName: " + e.getMessage());
        }
        return versionName;
    }
    public static boolean isEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }
    public static boolean isNotEmpty(Collection<?> list) {
        return !isEmpty(list);
    }
    public static void addArgsToIntent(Map<String, Object> args, Intent intent) {
        if (args == null) { return; }
        for (Map.Entry<String, Object> arg : args.entrySet()) {
            if (arg.getValue() instanceof Boolean) {
                intent.putExtra(arg.getKey(), (Boolean) arg.getValue());
            } else if (arg.getValue() instanceof Byte) {
                intent.putExtra(arg.getKey(), (Byte) arg.getValue());
            } else if (arg.getValue() instanceof Short) {
                intent.putExtra(arg.getKey(), (Short) arg.getValue());
            } else if (arg.getValue() instanceof Integer) {
                intent.putExtra(arg.getKey(), (Integer) arg.getValue());
            } else if (arg.getValue() instanceof Long) {
                intent.putExtra(arg.getKey(), (Long) arg.getValue());
            } else if (arg.getValue() instanceof Float) {
                intent.putExtra(arg.getKey(), (Float) arg.getValue());
            } else if (arg.getValue() instanceof Double) {
                intent.putExtra(arg.getKey(), (Double) arg.getValue());
            } else if (arg.getValue() instanceof String) {
                intent.putExtra(arg.getKey(), (String) arg.getValue());
            } else if (arg.getValue() instanceof Byte[]) {
                intent.putExtra(arg.getKey(), (Byte[]) arg.getValue());
            } else if (arg.getValue() instanceof Boolean[]) {
                intent.putExtra(arg.getKey(), (Boolean[]) arg.getValue());
            } else if (arg.getValue() instanceof Short[]) {
                intent.putExtra(arg.getKey(), (Short[]) arg.getValue());
            } else if (arg.getValue() instanceof Integer[]) {
                intent.putExtra(arg.getKey(), (Integer[]) arg.getValue());
            } else if (arg.getValue() instanceof Long[]) {
                intent.putExtra(arg.getKey(), (Long[]) arg.getValue());
            } else if (arg.getValue() instanceof Float[]) {
                intent.putExtra(arg.getKey(), (Float[]) arg.getValue());
            } else if (arg.getValue() instanceof Double[]) {
                intent.putExtra(arg.getKey(), (Double[]) arg.getValue());
            } else if (arg.getValue() instanceof String[]) {
                intent.putExtra(arg.getKey(), (String[]) arg.getValue());
            } else if (arg.getValue() instanceof CharSequence) {
                intent.putExtra(arg.getKey(), (CharSequence) arg.getValue());
            } else if (arg.getValue() instanceof CharSequence[]) {
                intent.putExtra(arg.getKey(), (CharSequence[]) arg.getValue());
            } else if (arg.getValue() instanceof Serializable) {
                intent.putExtra(arg.getKey(), (Serializable) arg.getValue());
            } else if (arg.getValue() instanceof Parcelable) {
                intent.putExtra(arg.getKey(), (Parcelable) arg.getValue());
            } else if (arg.getValue() instanceof Parcelable[]) {
                intent.putExtra(arg.getKey(), (Parcelable[]) arg.getValue());
            }
        }
    }
    public static String cleanProjectName(String projectName) {
        if (projectName == null) { return null; }
        return projectName.trim().toLowerCase().replaceAll(Constants.Regex.REGEX_CLEAN_SIMPLE, "");
    }
    public static String formatNumber(int number) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMANY);
        return formatter.format(number);
    }
    public static String formatNumber(long number) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMANY);
        return formatter.format(number);
    }
    public static String formatNumber(double number) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMANY);
        return formatter.format(number);
    }
    public static String camelCaseToConstant(String name) {
        if (name == null || name.isEmpty()) { return name; }
        StringBuilder result = new StringBuilder();
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // Inserta _ si:
            // - cambio de minúscula a mayúscula
            // - cambio de letra a número
            // - cambio de número a letra
            if (i > 0) {
                char prev = chars[i - 1];
                boolean lowerToUpper = Character.isLowerCase(prev) && Character.isUpperCase(c);
                boolean letterToDigit = Character.isLetter(prev) && Character.isDigit(c);
                boolean digitToLetter = Character.isDigit(prev) && Character.isLetter(c);
                boolean acronymBoundary =
                        Character.isUpperCase(prev) && Character.isUpperCase(c) && i + 1 < chars.length && Character.isLowerCase(chars[i + 1]);
                if (lowerToUpper || letterToDigit || digitToLetter || acronymBoundary) { result.append('_'); }
            }
            result.append(Character.toUpperCase(c));
        }
        return result.toString();
    }
    public static int getInputTypeFromClass(Map<Class<?>, List<? extends Annotation>> mapProperty) {
        if (mapProperty == null || mapProperty.size() != 1) { return InputType.TYPE_CLASS_TEXT; }
        for (Map.Entry<Class<?>, List<? extends Annotation>> entry : mapProperty.entrySet()) {
            Class<?> property = entry.getKey();
            if (property.equals(String.class)) {
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            } else if (property.equals(Byte.class) || property.equals(Short.class) || property.equals(Integer.class) || property.equals(Long.class) || property.equals(byte.class) || property.equals(short.class) || property.equals(int.class) || property.equals(long.class)) {
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
            } else if (property.equals(Float.class) || property.equals(Double.class) || property.equals(float.class) || property.equals(double.class)) {
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL;
            }
        }
        return InputType.TYPE_CLASS_TEXT;
    }
    public static String translateStringIdFromResourceStrings(Context context, String s) {
        return translateStringIdFromResourceStrings(context, s, "");
    }
    public static String translateStringIdFromResourceStrings(Context context, String s, String defaultHint, Object... args) {
        int id = context.getResources().getIdentifier(s, "string", context.getPackageName());
        return id != 0 ? context.getString(id, args) : defaultHint;
    }
    public static <T> void processListWithIndex(List<T> list, BiConsumer<Integer, T> action) {
        for (int i = 0; i < list.size(); i++) { action.accept(i, list.get(i)); }
    }
    public static String intStringFromInputType(KoreActivity<?, ?> activity, Class<?> property, RegularExpressionField regex) {
        if (property == null || regex == null) { return null; }
        String result = null;
        if (property.isAssignableFrom(Number.class)) { result = activity.getString(R.string.error_input_type_number); }
        if (property.isAssignableFrom(String.class)) {
            if (regex.value().equals(Constants.Regex.STRING_SIMPLE)) {
                result = activity.getString(R.string.error_input_type_string_simple, regex.maxLength());
            }
            if (regex.value().equals(Constants.Regex.STRING_FULL_CHARS)) {
                result = activity.getString(R.string.error_input_type_string_full, regex.maxLength());
            }
        }
        return result;
    }
    public static class Animation {
        public static void animateStrokeColor(ShapeableImageView view, @ColorInt int fromColor, @ColorInt int toColor, long duration) {
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
            animator.setDuration(duration);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.addUpdateListener(a -> {
                int animatedColor = (int) a.getAnimatedValue();
                view.setStrokeColor(ColorStateList.valueOf(animatedColor));
            });
            animator.start();
        }
        public static void animateTextColor(TextView view, @ColorInt int fromColor, @ColorInt int toColor, long duration) {
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
            animator.setDuration(duration);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.addUpdateListener(a -> {
                int animatedColor = (int) a.getAnimatedValue();
                view.setTextColor(ColorStateList.valueOf(animatedColor));
            });
            animator.start();
        }
        public static void animateBackgroundTint(View view, @ColorInt int fromColor, @ColorInt int toColor, long duration) {
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
            animator.setDuration(duration);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.addUpdateListener(a -> {
                int animatedColor = (int) a.getAnimatedValue();
                view.setBackgroundTintList(ColorStateList.valueOf(animatedColor));
            });
            animator.start();
        }
        public static void animatePulse(View target) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 1f, 1.05f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 1f, 1.05f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(scaleX, scaleY);
            set.setDuration(250);
            set.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            set.start();
        }
        public static void animateHorizontalSlide(TextView view, boolean toRight, String newText) {
            float direction = toRight ? 1f : -1f;
            view.animate().translationX(view.getWidth() * direction).alpha(0f).setDuration(250).withEndAction(() -> {
                view.setText(newText);
                view.setTranslationX(view.getWidth() * -direction);
                view.animate().translationX(0).alpha(1f).setDuration(250).start();
            }).start();
        }
        public static void bounceAnimation(View view, boolean toRight) {
            view.animate().translationX(toRight ? 50 : -50)   // desplazamiento pequeño
                    .setDuration(60).withEndAction(() -> view.animate().translationX(0).setDuration(60)).start();
        }
        public static void interpolation(View view, int duration, float scaleMax) {
            interpolation(view, duration, scaleMax, 0);
        }
        public static void interpolation(View view, int duration, float scaleMax, long delay) {
            interpolation(view, duration, 0, scaleMax, 1, delay);
        }
        public static void interpolation(View view, int duration, float scaleMin, float scaleMax, float finalScale, long delay) {
            interpolation(view, duration, scaleMin, scaleMax, finalScale, delay, null);
        }
        public static void interpolation(View view, int duration, float scaleMin, float scaleMax, float finalScale, long delay, Runnable actionOnFinish) {
            view.setScaleX(scaleMin);
            view.setScaleY(scaleMin);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scaleMin, scaleMax, finalScale);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scaleMin, scaleMax, finalScale);
            scaleX.setInterpolator(new OvershootInterpolator());
            scaleY.setInterpolator(new OvershootInterpolator());
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(duration);
            animatorSet.setStartDelay(delay);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) { if (actionOnFinish != null) { actionOnFinish.run(); } }
            });
            animatorSet.start();
        }
    }

    @SuppressWarnings("unchecked")
    public static class Reflex {
        private static final Map<String, Class<?>> classCache = new HashMap<>();
        private static final Map<String, Method> methodCache = new HashMap<>();
        private static final HashMap<Class<?>, Class<? extends View>> mapClassView;
        private static final Map<Class<?>, Function<String, ?>> componentConverters = new HashMap<>();

        static {
            mapClassView = new HashMap<>();
            mapClassView.put(boolean.class, CustomSwitchView.class);
            mapClassView.put(byte.class, CustomEditNumberView.class);
            mapClassView.put(short.class, CustomEditNumberView.class);
            mapClassView.put(int.class, CustomEditNumberView.class);
            mapClassView.put(long.class, CustomEditNumberView.class);
            mapClassView.put(float.class, CustomEditNumberView.class);
            mapClassView.put(double.class, CustomEditNumberView.class);
            mapClassView.put(Boolean.class, CustomSwitchView.class);
            mapClassView.put(Byte.class, CustomEditNumberView.class);
            mapClassView.put(Short.class, CustomEditNumberView.class);
            mapClassView.put(Integer.class, CustomEditNumberView.class);
            mapClassView.put(Long.class, CustomEditNumberView.class);
            mapClassView.put(Float.class, CustomEditNumberView.class);
            mapClassView.put(Double.class, CustomEditNumberView.class);
            mapClassView.put(BigInteger.class, CustomEditNumberView.class);
            mapClassView.put(BigDecimal.class, CustomEditNumberView.class);
            mapClassView.put(String.class, CustomEditTextView.class);
            mapClassView.put(CharSequence.class, CustomEditTextView.class);
            // Definir converters
            componentConverters.put(Byte.class, Byte::parseByte);
            componentConverters.put(byte.class, Byte::parseByte);
            componentConverters.put(Short.class, Short::parseShort);
            componentConverters.put(short.class, Short::parseShort);
            componentConverters.put(Integer.class, Integer::parseInt);
            componentConverters.put(int.class, Integer::parseInt);
            componentConverters.put(Long.class, Long::parseLong);
            componentConverters.put(long.class, Long::parseLong);
            componentConverters.put(Double.class, Double::parseDouble);
            componentConverters.put(double.class, Double::parseDouble);
            componentConverters.put(Float.class, Float::parseFloat);
            componentConverters.put(float.class, Float::parseFloat);
            componentConverters.put(Boolean.class, Boolean::parseBoolean);
            componentConverters.put(boolean.class, Boolean::parseBoolean);
            componentConverters.put(String.class, s -> s);
        }

        public static <T> Class<T> getClassTypeArgument(Object obj) { return getClassTypeArgument(obj, 0); }
        public static <T> Class<T> getClassTypeArgument(Object obj, int indexParam) {
            Type type = obj.getClass().getGenericSuperclass();
            Class<T> objClass = null;
            if (type instanceof Class) {
                objClass =
                        (Class<T>) ((ParameterizedType) Objects.requireNonNull(((Class<?>) obj.getClass().getGenericSuperclass()).getGenericSuperclass())).getActualTypeArguments()[indexParam];
            } else if (type instanceof ParameterizedType) {
                objClass = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[indexParam];
            }
            return objClass;
        }
        public static Class<?> getClassTypeFromClassType(Type type) {
            // Caso: Something<T>
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Type inner = pt.getActualTypeArguments()[0];
                if (inner instanceof Class<?>) { return (Class<?>) inner; }
                // Para casos anidados tipo LiveData<List<X>>
                if (inner instanceof ParameterizedType) { return getClassTypeFromClassType(inner); }
            }
            // Caso: tipo simple
            if (type instanceof Class<?>) { return (Class<?>) type; }
            return null;
        }
        public static TypeDescriptor getClassTypeFromType(Type type) {
            // Caso parametrizado: Something<T> o Pair<K,V>
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Class<?> raw = (Class<?>) pt.getRawType();
                List<Class<?>> args = new ArrayList<>();
                for (Type arg : pt.getActualTypeArguments()) {
                    if (arg instanceof Class<?>) {
                        args.add((Class<?>) arg);
                    } else if (arg instanceof ParameterizedType) {
                        // Extraemos el tipo base del anidado
                        args.add((Class<?>) ((ParameterizedType) arg).getRawType());
                    }
                }
                return new TypeDescriptor(raw, args);
            }
            // Caso simple
            if (type instanceof Class<?>) { return new TypeDescriptor(null, List.of((Class<?>) type)); }
            return null;
        }
        public Class<?> getInnermostType(TypeDescriptor td) { return td.getArguments().isEmpty() ? null : td.getArguments().get(td.getArguments().size() - 1); }
        public static <T extends ViewBinding> T initBinding(Activity ctx) { return initBinding(ctx, 0); }
        public static <T extends ViewBinding> T initBinding(Activity ctx, int indexParam) {
            try {
                Class<T> clazz = Reflex.getClassTypeArgument(ctx, indexParam);
                Method inflateMethod = clazz.getMethod("inflate", LayoutInflater.class);
                return (T) inflateMethod.invoke(ctx, ctx.getLayoutInflater());
            } catch (Exception e) {
                throw new RuntimeException("Error al inicializar ViewBinding en " + ctx.getClass().getSimpleName(), e);
            }
        }
        public static <T extends ViewBinding> T initBinding(Fragment ctx, @NonNull LayoutInflater inflater, @Nullable ViewGroup container) { return initBinding(ctx, 0, inflater, container); }
        public static <T extends ViewBinding> T initBinding(Fragment ctx, int indexParam, @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
            try {
                Class<T> clazz = Reflex.getClassTypeArgument(ctx, indexParam);
                Method inflateMethod = clazz.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
                return (T) inflateMethod.invoke(ctx, inflater, container, false);
            } catch (Exception e) {
                throw new RuntimeException("Error al inicializar ViewBinding en " + ctx.getClass().getSimpleName(), e);
            }
        }
        public static <T extends ViewBinding> T initBinding(ViewGroup viewGroup) { return initBinding(viewGroup, 0); }
        public static <T extends ViewBinding> T initBinding(ViewGroup viewGroup, int indexParam) {
            try {
                Class<T> clazz = Reflex.getClassTypeArgument(viewGroup, indexParam);
                Method inflateMethod = clazz.getMethod("inflate", LayoutInflater.class, ViewGroup.class);
                return (T) inflateMethod.invoke(null, LayoutInflater.from(viewGroup.getContext()), viewGroup);
            } catch (Exception e) {
                throw new RuntimeException("Error al inicializar ViewBinding en " + viewGroup.getContext().getClass().getSimpleName(), e);
            }
        }
        public static <T extends ViewBinding> T initBinding(Activity ctx, ViewGroup viewGroup, Class<T> clazz) {
            try {
                Method inflateMethod = clazz.getMethod("inflate", LayoutInflater.class, ViewGroup.class);
                return (T) inflateMethod.invoke(null, LayoutInflater.from(viewGroup.getContext()), viewGroup);
            } catch (Exception e) {
                throw new RuntimeException("Error al inicializar ViewBinding en " + ctx.getClass().getSimpleName(), e);
            }
        }
        public static <D> D newInstance(Class<D> dtoClass) throws InstantiationException {
            if (dtoClass == null) { return null; }
            D dto;
            try {
                dto = dtoClass.getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                throw new InstantiationException(e.getMessage());
            }
            return dto;
        }
        public static Class<? extends View> getViewFromTypeClass(Class<?> property, List<? extends Annotation> annotations) {
            Optional<? extends CustomViewRender> opCvr =
                    annotations.stream().filter(a -> a instanceof CustomViewRender).map(a -> (CustomViewRender) a).findFirst();
            if (opCvr.isPresent()) { return opCvr.get().value(); }
            return mapClassView.getOrDefault(property, null);
        }
        public static <V> Optional<V> castToType(Map<Class<?>, List<? extends Annotation>> mapTargetType, String value) {
            Optional<V> result = Optional.empty();
            if (value == null || value.trim().isEmpty() || mapTargetType == null || mapTargetType.size() != 1) { return result; }
            try {
                for (Map.Entry<Class<?>, List<? extends Annotation>> entry : mapTargetType.entrySet()) {
                    Class<?> targetType = entry.getKey();
                    Optional<? extends CustomViewRender> optCvr =
                            entry.getValue().stream().filter(a -> a instanceof CustomViewRender).map(a -> (CustomViewRender) a).findFirst();
                    if (optCvr.isPresent()) { targetType = optCvr.get().converTo(); }
                    Function<String, ?> converter = componentConverters.get(targetType);
                    return Optional.ofNullable((V) (converter != null ? converter.apply(value.trim()) : null));
                }
                return result;
            } catch (Exception e) {
                return result;
            }
        }
        public static String processAllValueProperties(Activity a, String[] rawProperties) {
            List<String> results = new ArrayList<>();
            for (String raw : rawProperties) {
                raw = parseToBuilder(raw.trim());
                if (!raw.isEmpty()) { results.add(executeValueProperty(a, raw)); }
            }
            return results.isEmpty() ? "" : results.size() == 1 ? results.get(0) : String.join(", ", results);
        }
        private static String parseToBuilder(String property) {
            StringBuilder path = new StringBuilder("class=%s,method=%s");
            String[] p = property.split("#");
            String[] m = p[1].split("\\(");
            if (m.length > 1) {
                String[] params = m[1].replace("(", "").replace(")", "").trim().split(",");
                for (String param : params) {
                    path.append(",param=").append(param);
                }
            }
            return String.format("[" + path + "]", p[0], m[0]);
        }
        private static String executeValueProperty(Activity a, String raw) {
            try {
                PropertyCall p = parsePropertyCall(raw);
                Object result = invokePropertyCall(a, p);
                return result != null ? result.toString() : "";

            } catch (Exception e) {
                Log.e("CustomInfoRowView", "Error: " + e.getMessage());
                return "";
            }
        }
        private static PropertyCall parsePropertyCall(String raw) {
            raw = raw.replace("[", "").replace("]", "");
            String[] pairs = raw.split(",");
            PropertyCall call = new PropertyCall();
            for (String pair : pairs) {
                String[] kv = pair.trim().split("=");
                if (kv.length != 2) { continue; }
                String key = kv[0].trim();
                String value = kv[1].trim();
                switch (key) {
                    case "class":
                        call.className = value;
                        break;
                    case "method":
                        call.methodName = value;
                        break;
                    case "param":
                        call.rawParams.add(value);
                        break;
                }
            }
            return call;
        }
        private static Object resolveParam(Activity a, String param) {
            if ("this".equals(param) || "activity".equals(param) || "context".equals(param)) { return a; }
            if (param.matches("-?\\d+")) { return Integer.parseInt(param); }
            if ("true".equalsIgnoreCase(param) || "false".equalsIgnoreCase(param)) { return Boolean.parseBoolean(param); }
            return param;
        }
        private static Object invokePropertyCall(Activity a, PropertyCall call) throws Exception {
            Class<?> clazz = classCache.computeIfAbsent(call.className, name -> {
                try { return Class.forName(name); } catch (Exception e) { throw new RuntimeException(e); }
            });
            List<Object> params = call.rawParams.stream().map(p -> resolveParam(a, p)).collect(Collectors.toList());
            Class<?>[] paramTypes = params.stream().map(Object::getClass).toArray(Class[]::new);
            String methodKey = call.className + "#" + call.methodName + Arrays.toString(paramTypes);
            Method method = methodCache.get(methodKey);
            if (method == null) {
                try {
                    method = clazz.getMethod(call.methodName, paramTypes);
                } catch (NoSuchMethodException e) {
                    for (Method m :
                            Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.getName().equalsIgnoreCase(call.methodName)).collect(Collectors.toList())) {
                        Class<?>[] parameterTypes = m.getParameterTypes();
                        if (parameterTypes.length != params.size()) {
                            throw new RuntimeException("COMPONENT ERROR:valueProperties: Parameters length is not the same");
                        }
                        boolean compatible = true;
                        for (int i = 0; i < parameterTypes.length; i++) {
                            Object arg = params.get(i);
                            Class<?> expectedType = parameterTypes[i];
                            // 1. Null es válido para cualquier tipo NO primitivo
                            if (arg == null) {
                                if (expectedType.isPrimitive()) {
                                    compatible = false;
                                    break;
                                }
                                continue;
                            }
                            Class<?> actualType = arg.getClass();
                            if (expectedType.equals(actualType) || expectedType.isAssignableFrom(actualType) || isPrimitiveWrapperCompatible(expectedType,
                                    actualType)) {
                                continue;
                            }
                            // Si no cumple, métoodo no válido
                            compatible = false;
                            break;
                        }
                        if (compatible) {
                            method = m;
                            break;
                        }
                    }
                }
                if (method == null) { throw new NoSuchMethodException(methodKey); }
                methodCache.put(methodKey, method);
            }
            Object instance = null;
            if (!Modifier.isStatic(method.getModifiers())) { instance = clazz.getDeclaredConstructor().newInstance(); }
            return method.invoke(instance, params.toArray());
        }
        private static boolean isPrimitiveWrapperCompatible(Class<?> expected, Class<?> actual) {
            if (!expected.isPrimitive()) { return false; }
            return (expected == int.class && actual == Integer.class) || (expected == boolean.class && actual == Boolean.class) || (expected == long.class && actual == Long.class) || (expected == double.class && actual == Double.class) || (expected == float.class && actual == Float.class) || (expected == short.class && actual == Short.class) || (expected == byte.class && actual == Byte.class) || (expected == char.class && actual == Character.class);
        }
        private static class PropertyCall {
            String className;
            String methodName;
            List<String> rawParams = new ArrayList<>();
        }
    }

    public static class Lombok {
        public static void addArgsToIntent(Map<String, Object> args, Intent intent) {
            if (args == null) { return; }
            args.forEach((k, v) -> put(intent, k, v));
        }
        public static Bundle mapToBundle(Map<String, Object> args) {
            Bundle b = new Bundle();
            if (args == null) { return b; }
            args.forEach((k, v) -> put(b, k, v));
            return b;
        }
        private static void put(Intent intent, String key, Object value) {
            if (value instanceof Integer) { intent.putExtra(key, (Integer) value); } else if (value instanceof String) {
                intent.putExtra(key, (String) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (Long) value);
            } else if (value instanceof Float) { intent.putExtra(key, (Float) value); }
        }
        private static void put(Bundle b, String key, Object value) {
            if (value instanceof Integer) { b.putInt(key, (Integer) value); } else if (value instanceof String) {
                b.putString(key, (String) value);
            } else if (value instanceof Boolean) {
                b.putBoolean(key, (Boolean) value);
            } else if (value instanceof Long) {
                b.putLong(key, (Long) value);
            } else if (value instanceof Float) { b.putFloat(key, (Float) value); }
        }
    }
}
