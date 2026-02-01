package com.olivadevelop.kore.component;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olivadevelop.kore.R;
import com.olivadevelop.kore.adapter.ImageGridAdapter;

import java.util.List;

public class ImageGridView extends FrameLayout {

    private RecyclerView recyclerView;
    private ImageGridAdapter adapter;

    private int spanCount = 3;

    public ImageGridView(Context context) {
        super(context);
        init(context);
    }

    public ImageGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_image_grid, this);

        recyclerView = findViewById(R.id.recycler);

        recyclerView.setLayoutManager(
                new GridLayoutManager(context, spanCount)
        );

        adapter = new ImageGridAdapter();
        recyclerView.setAdapter(adapter);
    }

    /* ===== API PÚBLICA ===== */

    public void setImages(List<Uri> images) {
        adapter.setImages(images);
    }

    public void addImage(Uri uri) {
        adapter.addImage(uri);
    }

    public void removeImage(Uri uri) {
        adapter.removeImage(uri);
    }

    public List<Uri> getImages() {
        return adapter.getImages();
    }

    public void setOnImageClickListener(ImageGridAdapter.OnImageClickListener listener) {
        adapter.setOnImageClickListener(listener);
    }

    public void setOnAddClickListener(Runnable onAddClick) {
        adapter.setOnAddClickListener(onAddClick);
    }
}