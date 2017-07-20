package jp.relo.cluboff.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import framework.phvtActivity.BaseActivity;
import framework.phvtUtils.StringUtil;
import jp.relo.cluboff.R;
import jp.relo.cluboff.ReloApp;
import jp.relo.cluboff.api.MyCallBack;
import jp.relo.cluboff.model.LoginReponse;
import jp.relo.cluboff.model.LoginRequest;
import jp.relo.cluboff.util.ConstansSharedPerence;
import jp.relo.cluboff.util.Constant;
import jp.relo.cluboff.util.LoginSharedPreference;
import jp.relo.cluboff.util.Utils;

/**
 * Created by HuyTran on 3/21/17.
 */

public class SplashScreenActivity extends BaseActivity {

    private static final int DELAY_TIME_SPLASH_ACTIVITY = 2000;
    public static boolean checkOpenedThisScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                checkOpenedThisScreen=false;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoLogin();
            }
        }, DELAY_TIME_SPLASH_ACTIVITY);
    }


    //TODO make redirection after splash screen gone
    private void goNextScreen() {
        checkOpenedThisScreen = true;
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void goMainScreen() {
        checkOpenedThisScreen = true;
        startActivity(new Intent(this, MainTabActivity.class));
        finish();
    }
    private void autoLogin(){
        LoginRequest loginRequest = LoginSharedPreference.getInstance(this).get(ConstansSharedPerence.TAG_LOGIN_INPUT,LoginRequest.class);
        if(loginRequest==null){
            goNextScreen();
        }else{
            String kaiinno = loginRequest.getKaiinno();
            String emailad = loginRequest.getEmailad();
            String brandid = loginRequest.getBrandid();
            boolean isNetworkAvailable = Utils.isNetworkAvailable(this);
            if(StringUtil.isEmpty(kaiinno)||StringUtil.isEmpty(emailad)||StringUtil.isEmpty(brandid)||!isNetworkAvailable){
                goNextScreen();
            }else{
                addSubscription(apiInterfaceJP.logon(kaiinno,emailad,brandid), new MyCallBack<LoginReponse>() {
                    @Override
                    public void onSuccess(LoginReponse model) {
                        if(model!=null){
                            if(Constant.HTTPOKJP.equals((model.getHeader().getStatus()))){
                                int brandid=0;
                                //TODO Save value reponse
                                goMainScreen();
                                setGoogleAnalytic(brandid);

                            }else{
                                goNextScreen();
                            }
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        goNextScreen();
                    }

                    @Override
                    public void onFinish() {
                    }
                });
            }
        }


    }



    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_splash_screen;
    }

    @Override
    protected void getMandatoryViews(Bundle savedInstanceState) {
    }

    @Override
    protected void registerEventHandlers() {
    }
    public void setGoogleAnalytic(int brandid){
        ReloApp reloApp = (ReloApp) getApplication();
        reloApp.trackingWithAnalyticGoogleServices(Constant.GA_CATALOGY,Constant.GA_ACTION,Constant.GA_DIMENSION_VALUE,brandid);
    }
}