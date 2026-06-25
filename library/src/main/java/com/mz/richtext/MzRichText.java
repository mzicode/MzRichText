package com.mz.richtext;

import android.content.Context;

import com.mz.richtext.listener.OnMzImageClickListener;
import com.mz.richtext.listener.OnMzUrlClickListener;
import com.mz.richtext.loader.MzImageLoader;

public class MzRichText {
    private String html;
    private MzImageLoader imageLoader;
    private OnMzImageClickListener imageClickListener;
    private OnMzUrlClickListener urlClickListener;
    private int textColor;
    private boolean hasTextColor;
    private float textSizeSp = -1f;
    private int textBottomDp = 6;
    private int imageBottomDp = 8;
    private boolean textSelectable;

    private MzRichText(Context context) {
    }

    public static MzRichText with(Context context) {
        return new MzRichText(context);
    }

    public MzRichText html(String html) {
        this.html = html;
        return this;
    }

    public MzRichText imageLoader(MzImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public MzRichText imageClickListener(OnMzImageClickListener listener) {
        this.imageClickListener = listener;
        return this;
    }

    public MzRichText urlClickListener(OnMzUrlClickListener listener) {
        this.urlClickListener = listener;
        return this;
    }

    public MzRichText textColor(int color) {
        this.textColor = color;
        this.hasTextColor = true;
        return this;
    }

    public MzRichText textSizeSp(float sizeSp) {
        this.textSizeSp = sizeSp;
        return this;
    }

    public MzRichText marginsDp(int textBottomDp, int imageBottomDp) {
        this.textBottomDp = textBottomDp;
        this.imageBottomDp = imageBottomDp;
        return this;
    }

    public MzRichText textSelectable(boolean selectable) {
        this.textSelectable = selectable;
        return this;
    }

    public void into(MzRichShowView target) {
        if (imageLoader != null) {
            target.setImageLoader(imageLoader);
        }
        if (imageClickListener != null) {
            target.setOnImageClickListener(imageClickListener);
        }
        if (urlClickListener != null) {
            target.setOnUrlClickListener(urlClickListener);
        }
        if (hasTextColor) {
            target.setTextColor(textColor);
        }
        if (textSizeSp > 0) {
            target.setTextSizeSp(textSizeSp);
        }
        target.setTextSelectable(textSelectable);
        target.setBlockMarginsDp(textBottomDp, imageBottomDp);
        target.setHtml(html);
    }
}
