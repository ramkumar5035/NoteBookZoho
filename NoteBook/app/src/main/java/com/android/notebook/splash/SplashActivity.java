package com.android.notebook.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.notebook.login.LoginActivity;
import com.android.notebook.R;
import com.android.notebook.base.BaseActivity;
import com.android.notebook.notes.NotesListActivity;

public class SplashActivity extends BaseActivity {
    private static final long SLEEP_DURATION = 2000l;
    public static String IS_FROM_SPLASH = "om.android.notebook.splash.IS_FROM_SPLASH";
    private boolean isDestroyed = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1 && !isDestroyed) {
                Intent intent;
                if (getUserPreference().isUserLoggedIn()) {
                    intent = new Intent(SplashActivity.this, NotesListActivity.class);
                    intent.putExtra(IS_FROM_SPLASH, true);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler.sendEmptyMessageDelayed(1, SLEEP_DURATION);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();
    }
}
