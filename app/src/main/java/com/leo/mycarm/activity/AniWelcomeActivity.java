package com.leo.mycarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.jaredrummler.android.widget.AnimatedSvgView;
import com.leo.mycarm.R;
import com.leo.mycarm.utils.ModelSVG;

/**
 * Created by Leo on 2018/9/20.
 */

public class AniWelcomeActivity extends Activity {

    AnimatedSvgView mSvgView;
    TextView text_app;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
                    startActivity(new Intent(AniWelcomeActivity.this, CaptureJzActivity.class));
                    finish();
                    overridePendingTransition(R.anim.scale_test, R.anim.scale_test2);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animwelc);
        mSvgView = findViewById(R.id.animated_svg_view);
        text_app = findViewById(R.id.text_app);


        setSvg(ModelSVG.values()[4]);
    }


    private void setSvg(ModelSVG modelSvg) {
        mSvgView.setGlyphStrings(modelSvg.glyphs);
        mSvgView.setFillColors(modelSvg.colors);
        mSvgView.setViewportSize(modelSvg.width, modelSvg.height);
        mSvgView.setTraceResidueColor(0x32000000);
        mSvgView.setTraceColors(modelSvg.colors);
        mSvgView.rebuildGlyphData();
        mSvgView.start();
        mSvgView.setOnStateChangeListener(new AnimatedSvgView.OnStateChangeListener() {
            @Override
            public void onStateChange(@AnimatedSvgView.State int state) {
                if (state == 2) {
                    text_app.setVisibility(View.VISIBLE);
                    Animation animator_deng = AnimationUtils.loadAnimation(AniWelcomeActivity.this, R.anim.anim_deng);
                    text_app.setAnimation(animator_deng);
                    animator_deng.start();
                    mHandler.sendEmptyMessageDelayed(11, 2000);
                }

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
