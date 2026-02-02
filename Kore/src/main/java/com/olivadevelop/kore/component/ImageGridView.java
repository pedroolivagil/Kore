package com.olivadevelop.kore.component;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.olivadevelop.kore.adapter.ImageGridAdapter;
import com.olivadevelop.kore.databinding.ViewImageGridBinding;

import java.util.List;
import java.util.function.Consumer;

import lombok.Getter;

@Getter
public class ImageGridView extends KoreComponentView<ViewImageGridBinding> {
    private final CustomImageSelector selector;
    private final RecyclerView recyclerView;
    private final ImageGridAdapter adapter;
    private int spanCount;
    private int maxImages;

    public ImageGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.recyclerView = getBinding().recycler;
        this.selector = getBinding().selector;
        this.selector.setMaxImages(this.maxImages);
        this.recyclerView.setLayoutManager(new GridLayoutManager(context, getSpanCount()));
        this.adapter = new ImageGridAdapter();
        this.recyclerView.setAdapter(adapter);
        OnValueChange ovc = getBinding().selector.getOnValueChange();
        getBinding().selector.setOnValueChange(s -> {
            if (ovc != null) { ovc.run(s); }
            if (getBinding().selector.getPhotoUri() != null) { adapter.addImage(getBinding().selector.getPhotoUri()); }
        });
    }
    @Override
    protected void configureFromLayout(@NonNull ComponentAttributes c) {
        this.spanCount = c.getSpanCount();
        this.maxImages = c.getMaxImages();
    }
    public void setImages(List<Uri> images) { adapter.setImages(images); }
    public void addImage(Uri uri) { adapter.addImage(uri); }
    public void removeImage(Uri uri) { adapter.removeImage(uri); }
    public List<Uri> getImages() { return adapter.getImages(); }
    public void setOnImageClickListener(ImageGridAdapter.OnImageClickListener listener) { adapter.setOnImageClickListener(listener); }
    public void setOnAddClickListener(Consumer<ImageGridAdapter> onAddClick) { adapter.setOnAddClickListener(onAddClick); }
}