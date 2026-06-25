package com.mz.richtext.expansion;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import com.mz.richtext.R;

/**
 * 默认的链接Activity
 */
public class DefaultLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_link_default);
        TextView textView = findViewById (R.id.link_text);
        textView.setText (getIntent ().getStringExtra (Constants.LINK_ACTIVITY_PARAM_NAME));
    }
}
