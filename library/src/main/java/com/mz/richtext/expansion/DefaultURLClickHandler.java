package com.mz.richtext.expansion;

import android.content.Context;
import android.content.Intent;

import com.mz.richtext.api.URLClickHandler;

/**
 * 默认的链接点击处理
 */
public class DefaultURLClickHandler implements URLClickHandler {

    private Context mContext;

    public DefaultURLClickHandler(Context context) {
        mContext = context;
    }

    @Override
    public void clickURL(String url) {
        Intent intent = new Intent (mContext, DefaultLinkActivity.class);
        intent.putExtra (Constants.LINK_ACTIVITY_PARAM_NAME, url);
        mContext.startActivity (intent);
    }
}
