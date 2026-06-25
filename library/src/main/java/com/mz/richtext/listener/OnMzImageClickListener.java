package com.mz.richtext.listener;

import android.widget.ImageView;

import java.util.List;

public interface OnMzImageClickListener {
    void onImageClick(ImageView imageView, List<String> imageUrls, int position);
}
