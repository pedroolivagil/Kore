package com.olivadevelop.kore.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class BlurView extends AppCompatImageView {

    private float blurRadius = 20f; // Radio por defecto

    public BlurView(Context context) {
        super(context);
        init();
    }

    public BlurView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlurView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.CENTER_CROP);
    }

    /**
     * Aplica el efecto blur actual sobre el fondo asignado al ImageView.
     */
    public void applyBlur() {
        Drawable drawable = getDrawable();
        if (drawable == null) { return; }
        Bitmap bitmap = drawableToBitmap(drawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+): RenderEffect
            setRenderEffect(RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP));
        } else {
            // Versiones anteriores: RenderScript
            Bitmap blurred = blurBitmap(getContext(), bitmap, blurRadius);
            setImageBitmap(blurred);
        }
    }

    /**
     * Cambia el radio de desenfoque.
     */
    public void setBlurRadius(float radius) {
        this.blurRadius = radius;
        applyBlur();
    }

    /**
     * Convierte cualquier Drawable a Bitmap.
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 1,
                drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 1,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Aplica desenfoque a un bitmap usando RenderScript (deprecado, pero funcional hasta API 30).
     */
    private Bitmap blurBitmap(Context context, Bitmap bitmap, float radius) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        Allocation output = Allocation.createFromBitmap(rs, outputBitmap);
        blurScript.setRadius(Math.min(radius, 25f)); // Máximo 25 permitido
        blurScript.setInput(input);
        blurScript.forEach(output);
        output.copyTo(outputBitmap);
        rs.destroy();
        return outputBitmap;
    }
}
