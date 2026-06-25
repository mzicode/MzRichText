package com.mz.richtext;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mz.richtext.listener.OnMzImageClickListener;
import com.mz.richtext.listener.OnMzImageDeleteListener;
import com.mz.richtext.loader.DefaultMzImageLoader;
import com.mz.richtext.loader.MzImageLoadCallback;
import com.mz.richtext.loader.MzImageLoader;
import com.mz.richtext.model.MzRichBlock;
import com.mz.richtext.parser.MzHtmlParser;

import java.util.ArrayList;
import java.util.List;

public class MzRichEditorView extends ScrollView {
    private LinearLayout container;
    private EditText lastFocusEdit;
    private MzImageLoader imageLoader = new DefaultMzImageLoader();
    private OnMzImageClickListener imageClickListener;
    private OnMzImageDeleteListener imageDeleteListener;
    private int textColor = Color.parseColor("#333333");
    private int hintColor = Color.parseColor("#999999");
    private float textSizeSp = 16f;
    private int editPaddingVertical;
    private int imageBottomMargin;
    private String hint = "请输入内容";

    private final OnKeyListener keyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && v instanceof EditText) {
                handleBackspace((EditText) v);
            }
            return false;
        }
    };

    private final OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && v instanceof EditText) {
                lastFocusEdit = (EditText) v;
            }
        }
    };

    public MzRichEditorView(Context context) {
        this(context, null);
    }

    public MzRichEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MzRichEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        editPaddingVertical = dp(8);
        imageBottomMargin = dp(8);
        setFillViewport(true);
        initContainer(context);
        addFirstEditText();
    }

    public void setImageLoader(MzImageLoader imageLoader) {
        if (imageLoader != null) {
            this.imageLoader = imageLoader;
        }
    }

    public void setOnImageClickListener(OnMzImageClickListener listener) {
        imageClickListener = listener;
    }

    public void setOnImageDeleteListener(OnMzImageDeleteListener listener) {
        imageDeleteListener = listener;
    }

    public void setHint(String hint) {
        this.hint = hint;
        if (container != null && container.getChildCount() == 1 && container.getChildAt(0) instanceof EditText) {
            ((EditText) container.getChildAt(0)).setHint(hint);
        }
    }

    public void setEditorTextStyle(int color, float sizeSp) {
        textColor = color;
        textSizeSp = sizeSp;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof EditText) {
                EditText editText = (EditText) child;
                editText.setTextColor(textColor);
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
            }
        }
    }

    public void insertImage(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }
        if (lastFocusEdit == null) {
            addFirstEditText();
        }
        insertImageAtFocus(MzRichBlock.image(imageUrl, 0, 0));
    }

    public void setHtml(String html) {
        setBlocks(MzHtmlParser.parse(html));
    }

    public void setBlocks(List<MzRichBlock> blocks) {
        removeAllContent();
        if (blocks == null || blocks.isEmpty()) {
            addFirstEditText();
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            MzRichBlock block = blocks.get(i);
            if (block == null) {
                continue;
            }
            if (block.type == MzRichBlock.TYPE_TEXT) {
                addEditTextAt(container.getChildCount(), htmlToPlainText(block.text), false);
            } else if (block.type == MzRichBlock.TYPE_IMAGE) {
                addImageAt(container.getChildCount(), block);
            }
        }
        ensureTailEditText();
    }

    public List<MzRichBlock> getBlocks() {
        List<MzRichBlock> blocks = new ArrayList<MzRichBlock>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof EditText) {
                String text = ((EditText) child).getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    blocks.add(MzRichBlock.text(textToHtml(text)));
                }
            } else if (child instanceof FrameLayout) {
                MzRichBlock block = (MzRichBlock) child.getTag();
                if (block != null && !TextUtils.isEmpty(block.imageUrl)) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public String getHtml() {
        return MzHtmlParser.toHtml(getBlocks());
    }

    public void clearAll() {
        removeAllContent();
        addFirstEditText();
    }

    private void removeAllContent() {
        clearImageRequests();
        container.removeAllViews();
        lastFocusEdit = null;
    }

    private void initContainer(Context context) {
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(15), dp(12), dp(15), dp(12));
        addView(container, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private void addFirstEditText() {
        if (container.getChildCount() == 0) {
            addEditTextAt(0, "", true);
        }
    }

    private EditText createEditText(String text, boolean showHint) {
        EditText editText = new EditText(getContext());
        editText.setMinHeight(dp(40));
        editText.setTextColor(textColor);
        editText.setHintTextColor(hintColor);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setPadding(0, editPaddingVertical, 0, editPaddingVertical);
        editText.setGravity(Gravity.TOP | Gravity.LEFT);
        editText.setSingleLine(false);
        editText.setMinLines(1);
        editText.setOnKeyListener(keyListener);
        editText.setOnFocusChangeListener(focusChangeListener);
        editText.setHint(showHint ? hint : "");
        editText.setText(text);
        return editText;
    }

    private EditText addEditTextAt(int index, String text, boolean showHint) {
        EditText editText = createEditText(text, showHint);
        container.addView(editText, index, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        lastFocusEdit = editText;
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
        return editText;
    }

    private void insertImageAtFocus(MzRichBlock imageBlock) {
        int editIndex = container.indexOfChild(lastFocusEdit);
        String source = lastFocusEdit.getText().toString();
        int cursor = Math.max(0, lastFocusEdit.getSelectionStart());
        String before = source.substring(0, cursor);
        String after = source.substring(cursor);

        if (source.length() == 0) {
            addImageAt(editIndex, imageBlock);
            addEditTextAt(editIndex + 1, "", false);
        } else if (before.length() == 0) {
            addImageAt(editIndex, imageBlock);
            addEditTextAt(editIndex + 1, "", false);
        } else if (after.length() == 0) {
            addImageAt(editIndex + 1, imageBlock);
            addEditTextAt(editIndex + 2, "", false);
        } else {
            lastFocusEdit.setText(before);
            addImageAt(editIndex + 1, imageBlock);
            addEditTextAt(editIndex + 2, after, false);
        }
        hideKeyboard();
    }

    private void addImageAt(int index, final MzRichBlock block) {
        final FrameLayout frame = new FrameLayout(getContext());
        frame.setTag(block);
        final ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(180));
        frame.addView(imageView, imageParams);

        TextView delete = new TextView(getContext());
        delete.setText("×");
        delete.setTextColor(Color.WHITE);
        delete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        delete.setGravity(Gravity.CENTER);
        delete.setBackgroundColor(0x99000000);
        FrameLayout.LayoutParams deleteParams = new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.RIGHT | Gravity.TOP);
        frame.addView(delete, deleteParams);

        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        frameParams.bottomMargin = imageBottomMargin;
        container.addView(frame, index, frameParams);

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageClickListener != null) {
                    List<String> urls = collectImageUrls();
                    imageClickListener.onImageClick(imageView, urls, urls.indexOf(block.imageUrl));
                }
            }
        });
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = collectImageUrls().indexOf(block.imageUrl);
                container.removeView(frame);
                mergeAdjacentEditTexts();
                if (imageDeleteListener != null) {
                    imageDeleteListener.onImageDelete(block.imageUrl, position);
                }
            }
        });

        imageLoader.load(getContext(), block.imageUrl, imageView, new MzImageLoadCallback() {
            @Override
            public void onSuccess(int width, int height) {
                if (width > 0 && height > 0) {
                    int viewWidth = container.getWidth() - container.getPaddingLeft() - container.getPaddingRight();
                    if (viewWidth <= 0) {
                        viewWidth = getResources().getDisplayMetrics().widthPixels - dp(30);
                    }
                    int imageHeight = Math.max(1, viewWidth * height / width);
                    ViewGroupLayoutHelper.setHeight(imageView, imageHeight);
                    block.imageWidth = width;
                    block.imageHeight = height;
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    private void handleBackspace(EditText editText) {
        if (editText.getSelectionStart() != 0) {
            return;
        }
        int index = container.indexOfChild(editText);
        if (index <= 0) {
            return;
        }
        View preView = container.getChildAt(index - 1);
        if (preView instanceof FrameLayout) {
            MzRichBlock block = (MzRichBlock) preView.getTag();
            int position = block == null ? -1 : collectImageUrls().indexOf(block.imageUrl);
            container.removeView(preView);
            if (imageDeleteListener != null && block != null) {
                imageDeleteListener.onImageDelete(block.imageUrl, position);
            }
        } else if (preView instanceof EditText) {
            EditText preEdit = (EditText) preView;
            String before = preEdit.getText().toString();
            preEdit.setText(before + editText.getText().toString());
            container.removeView(editText);
            preEdit.requestFocus();
            preEdit.setSelection(before.length());
            lastFocusEdit = preEdit;
        }
    }

    private void mergeAdjacentEditTexts() {
        for (int i = 0; i < container.getChildCount() - 1; i++) {
            View first = container.getChildAt(i);
            View second = container.getChildAt(i + 1);
            if (first instanceof EditText && second instanceof EditText) {
                EditText firstEdit = (EditText) first;
                EditText secondEdit = (EditText) second;
                String firstText = firstEdit.getText().toString();
                firstEdit.setText(firstText + secondEdit.getText().toString());
                container.removeView(secondEdit);
                firstEdit.requestFocus();
                firstEdit.setSelection(firstText.length());
                lastFocusEdit = firstEdit;
                return;
            }
        }
        ensureTailEditText();
    }

    private void ensureTailEditText() {
        if (container.getChildCount() == 0) {
            addFirstEditText();
            return;
        }
        View last = container.getChildAt(container.getChildCount() - 1);
        if (!(last instanceof EditText)) {
            addEditTextAt(container.getChildCount(), "", false);
        }
    }

    private List<String> collectImageUrls() {
        List<String> urls = new ArrayList<String>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof FrameLayout) {
                MzRichBlock block = (MzRichBlock) child.getTag();
                if (block != null && !TextUtils.isEmpty(block.imageUrl)) {
                    urls.add(block.imageUrl);
                }
            }
        }
        return urls;
    }

    private String htmlToPlainText(String html) {
        if (TextUtils.isEmpty(html)) {
            return "";
        }
        CharSequence text = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
                : Html.fromHtml(html);
        return text.toString().trim();
    }

    private String textToHtml(String text) {
        return TextUtils.htmlEncode(text).replace("\n", "<br>");
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && lastFocusEdit != null) {
            imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
        }
    }

    private void clearImageRequests() {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof FrameLayout) {
                ImageView imageView = findImageView((FrameLayout) child);
                if (imageView != null) {
                    imageLoader.clear(imageView);
                }
            }
        }
    }

    private ImageView findImageView(FrameLayout frameLayout) {
        for (int i = 0; i < frameLayout.getChildCount(); i++) {
            View child = frameLayout.getChildAt(i);
            if (child instanceof ImageView) {
                return (ImageView) child;
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP && lastFocusEdit != null) {
            lastFocusEdit.requestFocus();
        }
        return super.onTouchEvent(ev);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class ViewGroupLayoutHelper {
        static void setHeight(View view, int height) {
            android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = height;
            view.setLayoutParams(params);
        }
    }
}
