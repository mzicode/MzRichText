package com.mz.richtext.demo;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mz.richtext.MzRichEditorView;
import com.mz.richtext.MzRichShowView;
import com.mz.richtext.MzRichText;
import com.mz.richtext.listener.OnMzImageClickListener;
import com.mz.richtext.listener.OnMzImageDeleteListener;
import com.mz.richtext.listener.OnMzUrlClickListener;

import java.util.List;

public class MainActivity extends Activity {
    private static final String IMAGE_1 = "https://cn.bing.com/th?id=OHR.LeatherbackTT_ROW0614606094_1920x1080.jpg";
    private static final String IMAGE_2 = "https://cn.bing.com/th?id=OHR.FlamingosNamibia_ROW1247073951_1920x1080.jpg";
    private static final String SAMPLE_HTML = "<p>这是一个轻量图文混排库 Demo，预览控件不自带滚动，适合 RecyclerView；整页预览时外层套 ScrollView。</p>"
            + "<img src=\"" + IMAGE_1 + "\" width=\"1920\" height=\"1080\"/>"
            + "<p>编辑器支持在光标位置插入图片、删除图片、导出 HTML 再预览。</p>";

    private MzRichEditorView editorView;
    private MzRichShowView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editorView = findViewById(R.id.rich_editor);
        previewView = findViewById(R.id.rich_preview);
        Button insertImage = findViewById(R.id.btn_insert_image);
        Button preview = findViewById(R.id.btn_preview);
        Button clear = findViewById(R.id.btn_clear);

        editorView.setHtml(SAMPLE_HTML);
        editorView.setOnImageClickListener(imageClickListener("编辑器图片"));
        editorView.setOnImageDeleteListener(new OnMzImageDeleteListener() {
            @Override
            public void onImageDelete(String imageUrl, int position) {
                Toast.makeText(MainActivity.this, "删除图片：" + position, Toast.LENGTH_SHORT).show();
            }
        });

        renderPreview(SAMPLE_HTML);

        insertImage.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                editorView.insertImage(IMAGE_2);
            }
        });
        preview.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                renderPreview(editorView.getHtml());
            }
        });
        clear.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                editorView.clearAll();
                renderPreview("");
            }
        });
    }

    private void renderPreview(String html) {
        MzRichText.with(this)
                .html(html)
                .imageClickListener(imageClickListener("预览图片"))
                .urlClickListener(new OnMzUrlClickListener() {
                    @Override
                    public void onUrlClick(String url) {
                        Toast.makeText(MainActivity.this, "点击链接：" + url, Toast.LENGTH_SHORT).show();
                    }
                })
                .into(previewView);
    }

    private OnMzImageClickListener imageClickListener(final String prefix) {
        return new OnMzImageClickListener() {
            @Override
            public void onImageClick(ImageView imageView, List<String> imageUrls, int position) {
                Toast.makeText(MainActivity.this, prefix + " " + (position + 1) + "/" + imageUrls.size(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}
