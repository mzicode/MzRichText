package com.mz.richtext.loader;

import android.content.Context;
import android.widget.ImageView;

public interface MzImageLoader {
    void load(Context context, String source, ImageView imageView, MzImageLoadCallback callback);
    void clear(ImageView imageView);
}
