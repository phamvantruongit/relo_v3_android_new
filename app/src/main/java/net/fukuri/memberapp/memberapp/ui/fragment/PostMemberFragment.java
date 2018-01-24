package net.fukuri.memberapp.memberapp.ui.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.IOException;
import java.net.URLEncoder;

import framework.phvtUtils.AppLog;
import net.fukuri.memberapp.memberapp.BuildConfig;
import net.fukuri.memberapp.memberapp.R;
import net.fukuri.memberapp.memberapp.ReloApp;
import net.fukuri.memberapp.memberapp.api.ApiClientJP;
import net.fukuri.memberapp.memberapp.api.ApiInterface;
import net.fukuri.memberapp.memberapp.model.MessageEvent;
import net.fukuri.memberapp.memberapp.ui.BaseFragmentToolbarBottombar;
import net.fukuri.memberapp.memberapp.ui.webview.MyWebViewClient;
import net.fukuri.memberapp.memberapp.util.Constant;
import net.fukuri.memberapp.memberapp.util.LoginSharedPreference;
import net.fukuri.memberapp.memberapp.util.Utils;
import net.fukuri.memberapp.memberapp.util.ase.EvenBusLoadWebMembersite;
import net.fukuri.memberapp.memberapp.views.MyWebview;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by tqk666 on 12/28/17.
 */

public class PostMemberFragment extends BaseFragmentToolbarBottombar {

    private MyWebview mWebView;
    private int checkWebview;
    private FrameLayout fragmentContainer;
    private ProgressBar horizontalProgress;
    Handler handler;
    public static final int LOAD_URL_WEB =1;
    public static final int MULTIPLE_PERMISSIONS = 10;
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION};

    public static PostMemberFragment newInstance(String key, String url, int keyCheckWebview){
        PostMemberFragment memberFragment = new PostMemberFragment();
        Bundle bundle = new Bundle();
        bundle.putString(key,url);
        bundle.putInt(Constant.KEY_CHECK_WEBVIEW, keyCheckWebview);
        memberFragment.setArguments(bundle);
        return memberFragment;
    }

    @Override
    public void onViewCreated(View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ReloApp)getActivity().getApplication()).trackingAnalytics(Constant.GA_MEMBER_SCREEN);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK&&event.getAction() == KeyEvent.ACTION_UP){
                    EventBus.getDefault().post(new MessageEvent(Constant.TOP_COUPON));
                    return true;

                }
                return false;
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what==LOAD_URL_WEB) {
                    if (!checkPermissions()) {
                        requestPermission();
                    }else{
                        loadGetUrl();
                    }
                }
                return false;
            }
        });

        checkWebview = getArguments().getInt(Constant.KEY_CHECK_WEBVIEW, Constant.MEMBER_COUPON);
        mWebView = (MyWebview) view.findViewById(R.id.wvCoupon);
        horizontalProgress = (ProgressBar) view.findViewById(R.id.horizontalProgress);
        fragmentContainer = (FrameLayout)getActivity().findViewById(R.id.container_member_fragment);
        setupWebView();
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void setupToolbar() {
        tvMenuTitle.setText(R.string.member_site_title);
        tvMenuSubTitle.setText(R.string.title_member);
        ivMenuRight.setVisibility(View.VISIBLE);
        ivMenuRight.setImageResource(R.drawable.icon_close);
        ivMenuRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dismiss();
                fragmentContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void setupBottombar() {
        switch (checkWebview){
            case Constant.MEMBER_COUPON:
                lnBottom.setVisibility(View.VISIBLE);
                imvBackBottomBar.setVisibility(View.VISIBLE);
                imvForwardBottomBar.setVisibility(View.VISIBLE);


                //Test
                imvBrowserBottomBar.setVisibility(View.VISIBLE);
                llBrowser.setEnabled(true);

                imvReloadBottomBar.setVisibility(View.VISIBLE);
                break;
        }
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.goBack();
                imvBackBottomBar.setEnabled(false);
                llBack.setEnabled(false);
            }
        });
        llForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.goForward();
                imvForwardBottomBar.setEnabled(false);
                llForward.setEnabled(false);
            }
        });
        llBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LoginSharedPreference loginSharedPreference = LoginSharedPreference.getInstance(getActivity());
                if(loginSharedPreference!=null){
                    try {
                        Utils.showDialogBrowser(getActivity(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent internetIntent = new Intent(Intent.ACTION_VIEW);
                                Uri uri = Uri.parse(Constant.URL_MEMBER_BROWSER)
                                        .buildUpon()
                                        .appendQueryParameter("APPU", loginSharedPreference.getKEY_APPU())
                                        .appendQueryParameter("APPP", loginSharedPreference.getKEY_APPP())
                                        .build();
                                internetIntent.setData(uri);
                                getActivity().startActivity(internetIntent);
//                                dismiss();
                                fragmentContainer.setVisibility(View.GONE);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        llReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.loadUrl( "javascript:window.location.reload( true )" );

            }
        });

        imvBackBottomBar.setEnabled(mWebView.canGoBack());
        imvForwardBottomBar.setEnabled(mWebView.canGoForward());
        llBack.setEnabled(mWebView.canGoBack());
        llForward.setEnabled(mWebView.canGoForward());

    }

    @Override
    public int getRootLayoutId() {
        return R.layout.fragment_webview;
    }

    @Override
    protected void getMandatoryViews(View root, Bundle savedInstanceState) {

    }

    @Override
    protected void registerEventHandlers() {

    }

    private boolean checkPermissions() {
        int findLoca = ContextCompat.checkSelfPermission(getActivity(), permissions[0]);
        return findLoca == PackageManager.PERMISSION_GRANTED;

    }
    private void requestPermission() {
        requestPermissions(permissions, MULTIPLE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:
                if (grantResults.length > 0) {
                    boolean location = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (location) {
                        Toast.makeText(getActivity(), R.string.premission_accepted, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), R.string.premissionaccepted_no_accepted, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.premission_error, Toast.LENGTH_SHORT).show();
                }
                loadGetUrl();
                break;

        }
    }

    private void setupWebView() {
        mWebView.setWebViewClient(new MyWebViewClient(getActivity()) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(isVisible()){
                    imvBackBottomBar.setEnabled(mWebView.canGoBack());
                    imvForwardBottomBar.setEnabled(mWebView.canGoForward());
                    llBack.setEnabled(mWebView.canGoBack());
                    llForward.setEnabled(mWebView.canGoForward());
                }

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

            }
        });
        mWebView.setOnKeyListener(new View.OnKeyListener(){

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
//                    dismiss();
                    fragmentContainer.setVisibility(View.GONE);
                }
                return false;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    horizontalProgress.setVisibility(View.GONE);
                } else {
                    horizontalProgress.setVisibility(View.VISIBLE);
                    horizontalProgress.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if(Constant.TITLE_LOGOUT.equalsIgnoreCase(title)){
                    AppLog.log("Force logout: "+title);
                    Utils.forceLogout(getActivity());
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                mWebView.loadUrl(view.getUrl());
                return true;
            }
        });

        if (!checkPermissions()) {
            requestPermission();
        }else{
            loadGetUrl();
        }
    }

    private void loadGetUrl(){
        String  userID = "";
        String  pass = "";
        ApiInterface apiInterface = ApiClientJP.getClient().create(ApiInterface.class);
        LoginSharedPreference loginSharedPreference = LoginSharedPreference.getInstance(getActivity());
        userID = loginSharedPreference.getUserName();
        pass = loginSharedPreference.getPass();
        apiInterface.memberAuthHTML(userID, pass).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if(response!=null && response.body()!=null){
                        Document document = Jsoup.parse(response.body().string());
                        Element divChildren = document.select("html").first();
                        for (int i = 0; i < divChildren.childNodes().size(); i++) {
                            Node child = divChildren.childNode(i);
                            if (child.nodeName().equals("#comment")) {
                                String valueAuth = child.toString();
                                int valueHandleLogin = BuildConfig.DEBUG? 0:1;
                                if(Utils.parserInt(valueAuth.substring(valueAuth.indexOf("<STS>")+5,valueAuth.indexOf("</STS>")))==valueHandleLogin){
                                    LoginSharedPreference loginSharedPreference = LoginSharedPreference.getInstance(getActivity());
                                    loginSharedPreference.setKEY_APPU(valueAuth.substring(valueAuth.indexOf("<APPU>")+6,valueAuth.indexOf("</APPU>")));
                                    loginSharedPreference.setKEY_APPP(valueAuth.substring(valueAuth.indexOf("<APPP>")+6,valueAuth.indexOf("</APPP>")));
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    loadUrlWeb();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                AppLog.log("Err: "+t.toString());
                loadUrlWeb();
            }
        });

    }
    public void loadUrlWeb(){
        LoginSharedPreference loginSharedPreference = LoginSharedPreference.getInstance(getActivity());
        StringBuffer url=new StringBuffer(Constant.URL_MEMBER_BROWSER);
        url.append("?APPU="+ URLEncoder.encode(loginSharedPreference.getKEY_APPU()));
        url.append("&APPP="+URLEncoder.encode(loginSharedPreference.getKEY_APPP()));
        mWebView.loadUrl(url.toString());
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mWebView.stopLoading();
    }


    @Override
    public void onPause() {
        super.onPause();
        /*if (fragmentContainer.getVisibility() == View.VISIBLE){
            fragmentContainer.setVisibility(View.GONE);
            //((MainTabActivity)getActivity()).resetCurrentTab();
        }*/
    }

    @Subscribe
    public void onEvent(EvenBusLoadWebMembersite event) {
        handler.sendEmptyMessage(LOAD_URL_WEB);
        AppLog.log("Web loaded");
    }
}