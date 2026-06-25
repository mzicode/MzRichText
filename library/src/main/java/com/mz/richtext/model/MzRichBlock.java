package com.mz.richtext.model;

import java.util.HashMap;
import java.util.Map;

public class MzRichBlock {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;

    public int type;
    public String text;
    public String imageUrl;
    public int imageWidth;
    public int imageHeight;
    public Map<String, String> attrs;

    public static MzRichBlock text(String text) {
        MzRichBlock block = new MzRichBlock();
        block.type = TYPE_TEXT;
        block.text = text;
        return block;
    }

    public static MzRichBlock image(String imageUrl, int width, int height) {
        MzRichBlock block = new MzRichBlock();
        block.type = TYPE_IMAGE;
        block.imageUrl = imageUrl;
        block.imageWidth = width;
        block.imageHeight = height;
        return block;
    }

    public Map<String, String> ensureAttrs() {
        if (attrs == null) {
            attrs = new HashMap<String, String>();
        }
        return attrs;
    }
}
