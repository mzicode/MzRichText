package com.mz.richtext;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mz.richtext.listener.OnMzImageClickListener;
import com.mz.richtext.listener.OnMzUrlClickListener;
import com.mz.richtext.loader.DefaultMzImageLoader;
import com.mz.richtext.loader.MzImageLoadCallback;
import com.mz.richtext.loader.MzImageLoader;
import com.mz.richtext.model.MzRichBlock;
import com.mz.richtext.parser.MzHtmlParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MzRichShowView extends LinearLayout {
    private static final Map<String, Float> IMAGE_RATIO_CACHE = new HashMap<String, Float>();

    private final ArrayList<String> imageUrls = new ArrayList<String>();
    private MzImageLoader imageLoader = new DefaultMzImageLoader();
    private OnMzImageClickListener imageClickListener;
    private OnMzUrlClickListener urlClickListener;
    private int textColor = Color.parseColor("#333333");
    private float textSizeSp = 16f;
    private float lineSpacingExtra = 0f;
    private float lineSpacingMultiplier = 1.35f;
    private int textBottomMargin;
    private int imageBottomMargin;
    private boolean textSelectable;

    public MzRichShowView(Context context) {
        this(context, null);
    }

    public MzRichShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MzRichShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        textBottomMargin = dp(6);
        imageBottomMargin = dp(8);
    }

    public void setHtml(String html) {
        setBlocks(MzHtmlParser.parse(html));
    }

    public void setBlocks(List<MzRichBlock> blocks) {
        clearImageRequests();
        removeAllViews();
        imageUrls.clear();
        if (blocks == null) {
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            MzRichBlock block = blocks.get(i);
            if (block == null) {
                continue;
            }
            if (block.type == MzRichBlock.TYPE_TEXT) {
                addTextBlock(block.text);
            } else if (block.type == MzRichBlock.TYPE_IMAGE) {
                addImageBlock(block);
            }
        }
    }

    public void setImageLoader(MzImageLoader imageLoader) {
        if (imageLoader != null) {
            this.imageLoader = imageLoader;
        }
    }

    public void setOnImageClickListener(OnMzImageClickListener listener) {
        imageClickListener = listener;
    }

    public void setOnUrlClickListener(OnMzUrlClickListener listener) {
        urlClickListener = listener;
    }

    public void setTextColor(int color) {
        textColor = color;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }
        }
    }

    public void setTextSizeSp(float sizeSp) {
        textSizeSp = sizeSp;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
            }
        }
    }

    public void setBlockMarginsDp(int textBottom, int imageBottom) {
        textBottomMargin = dp(textBottom);
        imageBottomMargin = dp(imageBottom);
    }

    public void setTextSelectable(boolean selectable) {
        textSelectable = selectable;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextIsSelectable(selectable);
            }
        }
    }

    private void addTextBlock(String html) {
        Spanned spanned = trimSpanned(parseHtml(html));
        String plainText = spanned.toString().replace('\u00A0', ' ').trim();
        if (plainText.length() == 0) {
            return;
        }
        TextView textView = new TextView(getContext());
        textView.setText(processUrlSpans(spanned));
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        textView.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier);
        textView.setIncludeFontPadding(false);
        textView.setTextIsSelectable(textSelectable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = textBottomMargin;
        addView(textView, params);
    }

    private void addImageBlock(final MzRichBlock block) {
        if (TextUtils.isEmpty(block.imageUrl)) {
            return;
        }
        final int position = imageUrls.size();
        imageUrls.add(block.imageUrl);
        final ImageView imageView = new ImageView(getContext());
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setVisibility(INVISIBLE);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageClickListener != null) {
                    imageClickListener.onImageClick(imageView, new ArrayList<String>(imageUrls), position);
                }
            }
        });
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = imageBottomMargin;
        float ratio = block.imageWidth > 0 && block.imageHeight > 0 ? block.imageHeight * 1f / block.imageWidth : getCachedRatio(block.imageUrl);
        if (ratio > 0) {
            params.height = calculateImageHeight(ratio);
        }
        addView(imageView, params);
        imageLoader.load(getContext(), block.imageUrl, imageView, new MzImageLoadCallback() {
            @Override
            public void onSuccess(int width, int height) {
                if (width > 0 && height > 0) {
                    float ratio = height * 1f / width;
                    putCachedRatio(block.imageUrl, ratio);
                    resizeImageView(imageView, ratio);
                }
                imageView.setVisibility(VISIBLE);
            }

            @Override
            public void onFailure(Throwable throwable) {
                imageView.setVisibility(VISIBLE);
            }
        });
    }

    private Spanned parseHtml(String html) {
        String source = html == null ? "" : html;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT);
        }
        return Html.fromHtml(source);
    }

    private CharSequence processUrlSpans(Spanned spanned) {
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
        URLSpan[] spans = builder.getSpans(0, builder.length(), URLSpan.class);
        for (int i = 0; i < spans.length; i++) {
            final String url = spans[i].getURL();
            int start = builder.getSpanStart(spans[i]);
            int end = builder.getSpanEnd(spans[i]);
            int flags = builder.getSpanFlags(spans[i]);
            builder.removeSpan(spans[i]);
            builder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (urlClickListener != null) {
                        urlClickListener.onUrlClick(url);
                    }
                }
            }, start, end, flags);
        }
        return builder;
    }

    private Spanned trimSpanned(Spanned spanned) {
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
        while (builder.length() > 0 && Character.isWhitespace(builder.charAt(0))) {
            builder.delete(0, 1);
        }
        while (builder.length() > 0 && Character.isWhitespace(builder.charAt(builder.length() - 1))) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder;
    }

    private void resizeImageView(ImageView imageView, float ratio) {
        int height = calculateImageHeight(ratio);
        if (height <= 0) {
            return;
        }
        LayoutParams params = (LayoutParams) imageView.getLayoutParams();
        if (params.height == height) {
            return;
        }
        params.width = LayoutParams.MATCH_PARENT;
        params.height = height;
        params.bottomMargin = imageBottomMargin;
        imageView.setLayoutParams(params);
    }

    private int calculateImageHeight(float ratio) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        if (width <= 0) {
            width = getResources().getDisplayMetrics().widthPixels - getPaddingLeft() - getPaddingRight();
        }
        return width <= 0 ? 0 : Math.max(1, (int) (width * ratio));
    }

    private float getCachedRatio(String url) {
        synchronized (IMAGE_RATIO_CACHE) {
            Float ratio = IMAGE_RATIO_CACHE.get(url);
            return ratio == null ? 0 : ratio;
        }
    }

    private void putCachedRatio(String url, float ratio) {
        synchronized (IMAGE_RATIO_CACHE) {
            IMAGE_RATIO_CACHE.put(url, ratio);
        }
    }

    private void clearImageRequests() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ImageView) {
                imageLoader.clear((ImageView) child);
            }
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
