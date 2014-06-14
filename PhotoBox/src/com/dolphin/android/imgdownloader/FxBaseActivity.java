package com.dolphin.android.imgdownloader;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Administrator on 2/13/14.
 */
public class FxBaseActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
