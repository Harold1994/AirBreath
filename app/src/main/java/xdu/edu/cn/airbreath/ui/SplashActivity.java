package xdu.edu.cn.airbreath.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import xdu.edu.cn.airbreath.MainActivity;
import xdu.edu.cn.airbreath.R;

public class SplashActivity extends AppCompatActivity {

    private TextView tv_app;
    private int animtime = 2000;
    private AnimationSet set;
    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();

    }

    private void initView() {
        tv_app = (TextView) findViewById(R.id.tv_splash);
        set = new AnimationSet(true);
        set.setDuration(animtime);
        set.setFillAfter(true);
        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1);
        scale.setDuration(animtime);
        set.addAnimation(scale);

        TranslateAnimation translate = new TranslateAnimation(0, 0, 0, -200);
        translate.setDuration(animtime);
        set.addAnimation(translate);
        tv_app.startAnimation(set);

        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler = new MyHandler();
                mHandler.sendEmptyMessageDelayed(1001,500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                    break;
            }
        }
    }
}

