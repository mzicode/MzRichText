package com.mz.richtext.loader;

public interface MzImageLoadCallback {
    void onSuccess(int width, int height);
    void onFailure(Throwable throwable);
}
