# MzRichText / Sponge

一个轻量级 Android 图文混排库，核心思路是不再把图片塞进 `TextView` 的 `ImageSpan`，而是把内容拆成有序块：文字用 `TextView`，图片用 `ImageView`，再按顺序添加到容器里。

当前版本保留原 Sponge 代码，同时新增推荐使用的 `com.mz.richtext` 包。

## 能力

- 预览：`MzRichShowView` 渲染文字和图片，支持图片点击、链接点击。
- 编辑：`MzRichEditorView` 支持文字编辑、光标处插入图片、删除图片、导出 HTML。
- 滚动：预览控件不内置滚动，适合 RecyclerView/列表；整页预览时外层套 `ScrollView` 或 `NestedScrollView`。
- 图片加载：通过 `MzImageLoader` 抽象图片加载，不强绑定 Glide/Picasso/Coil。
- 轻量：第一版只处理常用的文字 + 图片，不包含 WebView、LaTeX、表格、视频等重能力。

## 快速使用

### 1. 预览图文

XML：

```xml
<com.mz.richtext.MzRichShowView
    android:id="@+id/rich_preview"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

Java：

```java
MzRichShowView previewView = findViewById(R.id.rich_preview);

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
        .into(previewView);
```

如果是文章详情这种整页预览，外层自己加滚动容器：

```xml
<ScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.mz.richtext.MzRichShowView
        android:id="@+id/rich_preview"
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

库里带了一个零依赖的 `DefaultMzImageLoader`，用于 demo 和简单场景。正式项目建议接入自己的图片库：

```java
previewView.setImageLoader(new MzImageLoader() {
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

## Demo

Demo 在 `app` 模块里，包含：

- 初始化 HTML 到编辑器和预览区。
- 点击按钮插入图片。
- 从编辑器导出 HTML 并刷新预览。
- 点击/删除图片回调。

运行编译：

```bash
./gradlew :app:assembleDebug
```

## 设计取舍

- 不用 `ImageSpan`：避免 `TextView` 宽度计算、图片异步刷新、左右滑动和行高异常等问题。
- 预览不内置滚动：列表复用时更安全，也避免嵌套滑动冲突。
- 编辑器内置滚动：编辑通常是整页表单，内置滚动可以直接使用。
- 不强绑图片库：核心库只定义接口，使用方可以接入项目已有图片加载方案。

## 后续可扩展

- XML 属性配置文字大小、颜色、间距、图片圆角。
- 图片占位图、加载失败图。
- 本地图片选择/上传后的 URL 替换。
- 更多 block 类型：标题、引用、分割线、视频、附件。
