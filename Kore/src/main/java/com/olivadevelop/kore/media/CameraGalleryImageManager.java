package com.olivadevelop.kore.media;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.ui.SnackbarBuilder;
import com.olivadevelop.kore.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Builder;
import lombok.Data;

public interface CameraGalleryImageManager {
    int REQUEST_CODE_GALLERY = 1234567890;
    int REQUEST_IMAGE_CAPTURE = 1987654321;
    static void loadImage(String uri, ImageView view) {
        loadImage(uri, view, R.drawable.ic_img, R.drawable.ic_error);
    }
    static void loadImage(String uri, ImageView view, int placeholderResId, int errorResId) {
        if (uri == null) {
            Picasso.get().load(placeholderResId).error(errorResId).fit().centerCrop().into(view);
        } else {
            Picasso.get().load(new File(uri)).placeholder(placeholderResId).error(errorResId).fit().centerCrop().into(view);
        }
    }
    static void loadImage(Uri uri, ImageView view) {
        Picasso.get().load(uri).placeholder(R.drawable.ic_img).error(R.drawable.ic_error).fit().centerCrop().into(view);
    }
    static void loadImage(Uri uri, ImageView view, Callback callback) {
        Picasso.get().load(uri).placeholder(R.drawable.ic_img).error(R.drawable.ic_error).fit().centerCrop().into(view, callback);
    }
    static void openGallery(KoreActivity<?, ?> activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !activity.getPermissionManager().areGranted(activity.getReadStoragePermission())) {
            activity.getPermissionManager().requestPermissions(activity.getReadStoragePermission());
            return;
        }
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType(Constants.IntentParam.INTENT_IMAGE_TYPE);
        startActivityForResult(activity, photoPickerIntent, REQUEST_CODE_GALLERY, null);
    }
    static Uri openCamera(KoreActivity<?, ?> activity, View btn, String imageName) {
        Uri photoUri = null;
        if (!activity.getPermissionManager().areGranted(activity.getCameraPermission())) {
            activity.getPermissionManager().requestPermissions(activity.getCameraPermission());
            return photoUri;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile(activity, imageName);
            photoUri = FileProvider.getUriForFile(activity, Constants.Provider.CAMERA_PROVIDER, photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        } catch (IOException e) {
            SnackbarBuilder.with(btn).message(activity.getString(R.string.the_camera_has_not_been_detected)).show();
        }
        startActivityForResult(activity, takePictureIntent, REQUEST_IMAGE_CAPTURE, null);
        return photoUri;
    }
    static File createImageFile(KoreActivity<?, ?> activity, String url) throws IOException {
        String timeStamp = new SimpleDateFormat(Constants.Formats.YYYY_MM_DD_HH_MM_SS, Locale.getDefault()).format(new Date());
        File storageDir = activity.getExternalFilesDir(url);
        return File.createTempFile("image_" + timeStamp, Constants.Files.EXTENSION_JPG, storageDir);
    }
    static File save(Uri uri, String dirName, String imageName, Context context) throws IOException {
        final InputStream imageStream = context.getContentResolver().openInputStream(uri);
        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        return save(selectedImage, dirName, imageName, context);
    }
    private static File save(Bitmap bitmap, String dirName, String imageName, Context context) throws IOException {
        File directory = new File(context.getFilesDir(), dirName);
        boolean existsDir = directory.exists();
        if (!existsDir) { existsDir = directory.mkdirs(); }
        if (!existsDir) { return null; }
        String name = imageName;
        if (!name.toLowerCase().trim().endsWith(Constants.Files.EXTENSION_JPG)) { name += Constants.Files.EXTENSION_JPG; }
        File imageFile = new File(directory, name);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); }
        return imageFile;
    }
    static Drawable getFileFromUri(Context context, Uri imageUri, ImageTransformConfig config) throws FileNotFoundException {
        InputStream imageStream = openStream(context, imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        if (bitmap == null) { return null; }
        // 1. Redimensionar si procede
        if (config.shouldResize()) {
            int widthPx = dpToPx(context, config.getDesiredWidthDp() != null ? config.getDesiredWidthDp() : bitmap.getWidth());
            int heightPx = dpToPx(context, config.getDesiredHeightDp() != null ? config.getDesiredHeightDp() : bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, widthPx, heightPx, true);
        }
        // 2. Aplicar recorte circular si procede
        if (config.isCircularCrop()) {
            bitmap = circularCrop(context, bitmap, config);
        }
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        InsetDrawable inset;
        if (config.getPadding() != null) {
            inset = new InsetDrawable(drawable,
                    dpToPx(context, config.getPadding()),
                    dpToPx(context, config.getPadding()),
                    dpToPx(context, config.getPadding()),
                    dpToPx(context, config.getPadding())
            );
        } else if (config.anyPadding()) {
            inset = new InsetDrawable(drawable,
                    config.getPaddingLeft() != null ? dpToPx(context, config.getPaddingLeft()) : 0,
                    config.getPaddingTop() != null ? dpToPx(context, config.getPaddingTop()) : 0,
                    config.getPaddingRight() != null ? dpToPx(context, config.getPaddingRight()) : 0,
                    config.getPaddingBottom() != null ? dpToPx(context, config.getPaddingBottom()) : 0
            );
        } else {
            inset = new InsetDrawable(drawable, 0, 0, 0, 0);
        }
        return inset;
    }
    private static InputStream openStream(Context context, Uri imageUri) throws FileNotFoundException {
        if ("content".equals(imageUri.getScheme()) || "file".equals(imageUri.getScheme())) {
            return context.getContentResolver().openInputStream(imageUri);
        } else {
            return new FileInputStream(imageUri.toString());
        }
    }
    private static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
    private static Bitmap circularCrop(Context context, Bitmap src, ImageTransformConfig config) {
        int size = Math.min(src.getWidth(), src.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float radius = size / 2f;
        // Dibujar fondo circular
        canvas.drawCircle(radius, radius, radius, paint);
        // Recorte circular
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);
        // Añadir borde si está configurado
        if (config.shouldDrawBorder()) {
            Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(dpToPx(context, Math.round(config.getBorderWidthDp())));
            borderPaint.setColor(config.getBorderColor());
            canvas.drawCircle(radius, radius, radius - borderPaint.getStrokeWidth() / 2, borderPaint);
        }
        src.recycle();
        return output;
    }
    @Data
    @Builder
    class ImageTransformConfig {
        private Integer desiredWidthDp;
        private Integer desiredHeightDp;
        private boolean circularCrop;
        private Float borderWidthDp;
        private Integer borderColor;
        private Integer padding;
        private Integer paddingLeft;
        private Integer paddingRight;
        private Integer paddingTop;
        private Integer paddingBottom;
        public boolean shouldResize() { return desiredWidthDp != null || desiredHeightDp != null; }
        public boolean shouldDrawBorder() { return borderWidthDp != null && borderWidthDp > 0 && borderColor != null; }
        public boolean anyPadding() { return padding != null || paddingLeft != null || paddingRight != null || paddingTop != null || paddingBottom != null; }
    }
}