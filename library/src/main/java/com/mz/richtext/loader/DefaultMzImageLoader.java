package com.mz.richtext.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultMzImageLoader implements MzImageLoader {

    @Override
    public void load(Context context, String source, ImageView imageView, MzImageLoadCallback callback) {
        if (TextUtils.isEmpty(source)) {
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("source is empty"));
            }
            return;
        }
        imageView.setTag(source);
        new BitmapTask(context, imageView, source, callback).execute(source);
    }

    @Override
    public void clear(ImageView imageView) {
        imageView.setTag(null);
        imageView.setImageDrawable(null);
    }

    private static class BitmapTask extends AsyncTask<String, Void, Bitmap> {
        private final Context context;
        private final WeakReference<ImageView> imageViewRef;
        private final String source;
        private final MzImageLoadCallback callback;
        private Throwable error;

        BitmapTask(Context context, ImageView imageView, String source, MzImageLoadCallback callback) {
            this.context = context.getApplicationContext();
            this.imageViewRef = new WeakReference<ImageView>(imageView);
            this.source = source;
            this.callback = callback;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                if (source.startsWith("http://") || source.startsWith("https://")) {
                    HttpURLConnection connection = (HttpURLConnection) new URL(source).openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(15000);
                    InputStream inputStream = connection.getInputStream();
                    try {
                        return BitmapFactory.decodeStream(inputStream);
                    } finally {
                        inputStream.close();
                        connection.disconnect();
                    }
                }
                String path = source.startsWith("file://") ? source.substring(7) : source;
                return BitmapFactory.decodeFile(new File(path).getAbsolutePath());
            } catch (Throwable throwable) {
                error = throwable;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = imageViewRef.get();
            if (imageView == null || !source.equals(imageView.getTag())) {
                return;
            }
            if (bitmap == null) {
                if (callback != null) {
                    callback.onFailure(error == null ? new RuntimeException("decode bitmap failed") : error);
                }
                return;
            }
            imageView.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
            if (callback != null) {
                callback.onSuccess(bitmap.getWidth(), bitmap.getHeight());
            }
        }
    }
}
