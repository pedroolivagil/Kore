package com.olivadevelop.kore.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olivadevelop.kore.databinding.ItemImageAddBinding;
import com.olivadevelop.kore.databinding.ItemImageGridBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_ADD = 1;
    private final List<Uri> images = new ArrayList<>();
    private OnImageClickListener onImageClickListener;
    private Consumer<ImageGridAdapter> onAddClickListener;

    @Override
    public int getItemViewType(int position) { return position < images.size() ? TYPE_IMAGE : TYPE_ADD; }

    @Override
    public int getItemCount() { return images.size() + 1; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_IMAGE) {
            return new ImageViewHolder(ItemImageGridBinding.inflate(inflater, parent, false));
        } else {
            return new AddViewHolder(ItemImageAddBinding.inflate(inflater, parent, false));
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            Uri uri = images.get(position);
            ((ImageViewHolder) holder).bind(uri);
        }
    }
    public void setImages(List<Uri> list) {
        images.clear();
        images.addAll(list);
        notifyDataSetChanged();
    }
    public void addImage(Uri uri) {
        images.add(uri);
        notifyItemInserted(images.size() - 1);
    }
    public void removeImage(Uri uri) {
        int index = images.indexOf(uri);
        if (index >= 0) {
            images.remove(index);
            notifyItemRemoved(index);
        }
    }
    public List<Uri> getImages() { return new ArrayList<>(images); }
    @Getter
    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemImageGridBinding b;
        ImageViewHolder(ItemImageGridBinding binding) {
            super(binding.getRoot());
            this.b = binding;
            b.image.setOnClickListener(v -> { if (onImageClickListener != null) { onImageClickListener.onImageClick(images.get(getBindingAdapterPosition())); } });
        }
        void bind(Uri uri) {
            b.image.setImageURI(uri); // o Picasso/Glide
        }
    }

    @Getter
    class AddViewHolder extends RecyclerView.ViewHolder {
        private final ItemImageAddBinding b;
        AddViewHolder(ItemImageAddBinding binding) {
            super(binding.getRoot());
            this.b = binding;
            binding.getRoot().setOnClickListener(v -> { if (onAddClickListener != null) { onAddClickListener.accept(ImageGridAdapter.this); } });
        }
    }

    public interface OnImageClickListener {
        void onImageClick(Uri uri);
    }
}
