# MzRichText

一个轻量级 Android 图文混排库。核心思路是把内容拆成有序块：文字用 `TextView`，图片用 `ImageView`，再按顺序添加到容器里，避免把图片塞进 `TextView` 的 `ImageSpan` 后出现宽度、行高、异步刷新等问题。

## 引入

```gradle
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation 'io.github.mzicode:mz-richtext:0.1.0'
}
```

## 能力

- 展示：`MzRichShowView` 渲染文字和图片，支持图片点击、链接点击。
- 编辑：`MzRichEditorView` 支持文字编辑、光标处插入图片、删除图片、导出 HTML。
- 滚动：展示控件不内置滚动，适合 RecyclerView/列表；整页展示时外层套 `ScrollView` 或 `NestedScrollView`。
- 图片加载：通过 `MzImageLoader` 抽象图片加载，不强绑定 Glide/Picasso/Coil。

## 快速使用

### 1. 展示图文

XML：

```xml
<com.mz.richtext.MzRichShowView
    android:id="@+id/rich_show"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

Java：

```java
MzRichShowView showView = findViewById(R.id.rich_show);

MzRichText.with(this)
        .html(html)
        .marginsDp(6, 8)
        .textSelectable(false)
        .imageClickListener((imageView, imageUrls, position) -> {
            // 打开大图预览
        })
        .urlClickListener(url -> {
            // 打开链接
        })
        .into(showView);
```

如果是文章详情这种整页展示，外层自己加滚动容器：

```xml
<ScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.mz.richtext.MzRichShowView
        android:id="@+id/rich_show"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</ScrollView>
```

### 2. 编辑图文

XML：

```xml
<com.mz.richtext.MzRichEditorView
    android:id="@+id/rich_editor"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Java：

```java
MzRichEditorView editorView = findViewById(R.id.rich_editor);

editorView.setHtml(html);
editorView.insertImage(imageUrl);

String resultHtml = editorView.getHtml();
List<MzRichBlock> blocks = editorView.getBlocks();
```

`MzRichEditorView` 自己继承 `ScrollView`，更适合单独的编辑页面；如果嵌在复杂滚动父容器里，建议只保留一个滚动层。

### 3. 自定义图片加载

库里带了一个零依赖的 `DefaultMzImageLoader`。正式项目建议接入自己的图片库：

```java
showView.setImageLoader(new MzImageLoader() {
    @Override
    public void load(Context context, String source, ImageView imageView, MzImageLoadCallback callback) {
        // 用 Glide/Picasso/Coil/自研加载器加载图片
        // 加载成功后调用 callback.onSuccess(width, height)
        // 加载失败后调用 callback.onFailure(error)
    }

    @Override
    public void clear(ImageView imageView) {
        // 取消请求并清理 ImageView
    }
});
```

编辑器同样支持：

```java
editorView.setImageLoader(imageLoader);
```

## 支持的 HTML

当前解析器主要支持常见文本和图片：

```html
<p>第一段文字</p>
<img src="https://example.com/a.jpg" width="800" height="600" />
<p>第二段文字</p>
```

图片地址会按 `src`、`data-original`、`data-src` 顺序读取，宽高支持 `width`、`height` 和简单的 `style="width:800px;height:600px"`。
