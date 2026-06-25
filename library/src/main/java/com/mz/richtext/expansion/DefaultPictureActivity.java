package com.mz.richtext.expansion;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ImageView;

import com.mz.richtext.R;
import com.mz.richtext.Utils;

/**
 * 默认的图片显示Activity
 */
public class DefaultPictureActivity extends Activity {

    private ImageView mPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_picture_default);

        mPicture = findViewById (R.id.picture);
        String url = getIntent ().getStringExtra (Constants.PICTURE_ACTIVITY_PARAM_NAME);
        String fileName = Utils.hashKeyForDisk (url);
        mPicture.setImageBitmap (Utils.loadBitmapFromFile (this, fileName));
    }
}
