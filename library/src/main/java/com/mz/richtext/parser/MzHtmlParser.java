package com.mz.richtext.parser;

import android.text.TextUtils;

import com.mz.richtext.model.MzRichBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MzHtmlParser {
    private static final Pattern IMG_TAG_PATTERN = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_ATTR_PATTERN = Pattern.compile("([a-zA-Z0-9_:-]+)\\s*=\\s*([\\\"'])(.*?)\\2", Pattern.CASE_INSENSITIVE);
    private static final Pattern STYLE_SIZE_PATTERN = Pattern.compile("%s\\s*:\\s*(\\d+)(?:px)?", Pattern.CASE_INSENSITIVE);

    private MzHtmlParser() {
    }

    public static List<MzRichBlock> parse(String html) {
        List<MzRichBlock> blocks = new ArrayList<MzRichBlock>();
        if (TextUtils.isEmpty(html)) {
            return blocks;
        }

        Matcher matcher = IMG_TAG_PATTERN.matcher(html);
        int lastEnd = 0;
        while (matcher.find()) {
            addTextBlock(blocks, html.substring(lastEnd, matcher.start()));
            MzRichBlock imageBlock = parseImageBlock(matcher.group());
            if (imageBlock != null) {
                blocks.add(imageBlock);
            }
            lastEnd = matcher.end();
        }
        addTextBlock(blocks, html.substring(lastEnd));
        return blocks;
    }

    public static String toHtml(List<MzRichBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (MzRichBlock block : blocks) {
            if (block == null) {
                continue;
            }
            if (block.type == MzRichBlock.TYPE_TEXT) {
                if (!TextUtils.isEmpty(block.text)) {
                    builder.append("<p>").append(block.text).append("</p>");
                }
            } else if (block.type == MzRichBlock.TYPE_IMAGE && !TextUtils.isEmpty(block.imageUrl)) {
                builder.append("<img src=\"").append(escapeAttr(block.imageUrl)).append("\"");
                if (block.imageWidth > 0) {
                    builder.append(" width=\"").append(block.imageWidth).append("\"");
                }
                if (block.imageHeight > 0) {
                    builder.append(" height=\"").append(block.imageHeight).append("\"");
                }
                builder.append("/>");
            }
        }
        return builder.toString();
    }

    private static void addTextBlock(List<MzRichBlock> blocks, String html) {
        if (TextUtils.isEmpty(html)) {
            return;
        }
        String text = html.trim();
        if (text.length() == 0) {
            return;
        }
        blocks.add(MzRichBlock.text(text));
    }

    private static MzRichBlock parseImageBlock(String imgTag) {
        Matcher matcher = IMG_ATTR_PATTERN.matcher(imgTag);
        String src = null;
        int width = 0;
        int height = 0;
        String style = null;
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(3);
            if ("src".equalsIgnoreCase(key) || "data-original".equalsIgnoreCase(key) || "data-src".equalsIgnoreCase(key)) {
                if (TextUtils.isEmpty(src)) {
                    src = value;
                }
            } else if ("width".equalsIgnoreCase(key)) {
                width = parseSize(value);
            } else if ("height".equalsIgnoreCase(key)) {
                height = parseSize(value);
            } else if ("style".equalsIgnoreCase(key)) {
                style = value;
            }
        }
        if (!TextUtils.isEmpty(style)) {
            if (width <= 0) {
                width = parseStyleSize(style, "width");
            }
            if (height <= 0) {
                height = parseStyleSize(style, "height");
            }
        }
        return TextUtils.isEmpty(src) ? null : MzRichBlock.image(src, width, height);
    }

    private static int parseStyleSize(String style, String name) {
        Pattern pattern = Pattern.compile(String.format(STYLE_SIZE_PATTERN.pattern(), name), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(style);
        if (!matcher.find()) {
            return 0;
        }
        return parseSize(matcher.group(1));
    }

    private static int parseSize(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.replace("px", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String escapeAttr(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("\"", "&quot;");
    }
}
