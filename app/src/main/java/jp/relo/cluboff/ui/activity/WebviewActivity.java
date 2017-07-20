package jp.relo.cluboff.ui.activity;

import android.os.Bundle;

import java.io.File;

import framework.phvtActivity.BaseActivity;
import jp.relo.cluboff.R;
import jp.relo.cluboff.ui.fragment.WebViewFragment;

/**
 * Created by quynguyen on 3/23/17.
 */

public class WebviewActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void events() {

    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_webview;
    }

    @Override
    protected void getMandatoryViews(Bundle savedInstanceState) {
        replaceFragment(R.id.frContainerWebview, WebViewFragment.class.getName(),false,getIntent().getExtras(),null);
    }

    @Override
    protected void registerEventHandlers() {
        events();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}