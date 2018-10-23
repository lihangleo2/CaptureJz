package com.leo.mycarm.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.leo.mycarm.R;
import com.leo.mycarm.TimeUtils;
import com.leo.mycarm.adapter.FilterAdapter;
import com.leo.mycarm.gpufilter.helper.MagicFilterType;
import com.leo.mycarm.media.MediaPlayerWrapper;
import com.leo.mycarm.media.VideoInfo;
import com.leo.mycarm.mediacodec.VideoClipper;
import com.leo.mycarm.utils.ToastUtils;
import com.leo.mycarm.utils.UIUtil;
import com.leo.mycarm.widget.LoadingDialog;
import com.leo.mycarm.widget.VideoPreviewView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;


/**
 * Created by cj on 2017/10/16.
 * desc: 循环播放选择的视频的页面，可以对视频设置水印和美白效果
 */

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayerWrapper.IMediaCallback, View.OnTouchListener, FilterAdapter.onFilterChangeListener {


    private VideoPreviewView mVideoView;
    private String mPath;
    private boolean resumed;
    private boolean isDestroy;
    private boolean isPlaying = false;

    int startPoint;

    private String outputPath;
    static final int VIDEO_PREPARE = 0;
    static final int VIDEO_START = 1;
    static final int VIDEO_UPDATE = 2;
    static final int VIDEO_PAUSE = 3;
    static final int VIDEO_CUT_FINISH = 4;
    static final int VIDEO_SAVE = 5;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEO_PREPARE:
                    Executors.newSingleThreadExecutor().execute(update);
                    break;
                case VIDEO_START:
                    isPlaying = true;
                    break;
                case VIDEO_UPDATE:
                  /*  int curDuration = mVideoView.getCurDuration();
                    if (curDuration > startPoint + clipDur) {
                        mVideoView.seekTo(startPoint);
                        mVideoView.start();
                    }*/
                    break;
                case VIDEO_PAUSE:
                    isPlaying = false;
                    break;
                case VIDEO_CUT_FINISH:
                    //保存在系统相册里面
                    Uri localUri = Uri.fromFile(new File(outputPath));
                    Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                    sendBroadcast(localIntent);
                    endLoading();
                    ToastUtils.showToast("保存成功");
                    finish();
                    //TODO　已经渲染完毕了　
                    break;

                case VIDEO_SAVE:
                    //保存在系统相册里面
                    Uri localUriSave = Uri.fromFile(new File(mPath));
                    Intent localIntentSave = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUriSave);
                    sendBroadcast(localIntentSave);
                    endLoading();
                    ToastUtils.showToast("保存成功");
                    finish();
                    break;
            }
        }
    };
    private MagicFilterType filterType = MagicFilterType.NONE;


    //自己加的滤镜
    private RecyclerView mFilterListView;
    private FilterAdapter mAdapter;

    private RelativeLayout layout_filter_tab;

    private final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.SUNRISE,
            MagicFilterType.SUNSET,
            MagicFilterType.WHITECAT,
            MagicFilterType.BLACKCAT,
            MagicFilterType.SKINWHITEN,
            MagicFilterType.HEALTHY,
            MagicFilterType.SWEETS,
            MagicFilterType.ROMANCE,
            MagicFilterType.SAKURA,
            MagicFilterType.WARM,
            MagicFilterType.ANTIQUE,
            MagicFilterType.NOSTALGIA,
            MagicFilterType.CALM,
            MagicFilterType.LATTE,
            MagicFilterType.TENDER,
    };

    private LoadingDialog loading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        initView();
        initData();

        RelativeLayout.LayoutParams cardreParams = (RelativeLayout.LayoutParams) layout_filter_tab.getLayoutParams();
        cardreParams.height = (UIUtil.getHeight() - UIUtil.dip2px(PreviewActivity.this, 104)) * 7 / 24;
    }

    private void initView() {
        layout_filter_tab = findViewById(R.id.layout_filter_tab);
        mFilterListView = (RecyclerView) findViewById(R.id.filter_listView);

        mVideoView = (VideoPreviewView) findViewById(R.id.videoView);
        TextView confirm = (TextView) findViewById(R.id.bar_txt_right);
        RelativeLayout close = (RelativeLayout) findViewById(R.id.bar_btn_left);

        confirm.setOnClickListener(this);
        close.setOnClickListener(this);
        mVideoView.setOnTouchListener(this);
        setLoadingCancelable(false);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterAdapter(this, types);
        mFilterListView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(this);

    }


    private void initData() {
        Intent intent = getIntent();
        //选择的视频的本地播放地址
        mPath = intent.getStringExtra("path");
        ArrayList<String> srcList = new ArrayList<>();
        srcList.add(mPath);
        mVideoView.setVideoPath(srcList);
        mVideoView.setIMediaCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumed) {
            mVideoView.start();
        }
        resumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        isDestroy = true;
        mVideoView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!isLoading()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bar_btn_left:
                if (isLoading()) {
                    endLoading();
                }
                finish();
                break;

            case R.id.bar_txt_right:
                if (isLoading()) {
                    return;
                }
                if (filterType == MagicFilterType.NONE) {
                    //保存在系统相册里面
                    mVideoView.pause();
                    mHandler.sendEmptyMessage(VIDEO_SAVE);
                    return;
                }


                mVideoView.pause();
                showLoading("视频处理中", false);
                VideoClipper clipper = new VideoClipper();
                clipper.setInputVideoPath(mPath);
                outputPath = getFilesDir().getAbsolutePath().toString() + "/" + TimeUtils.getDateToStringLeo(System.currentTimeMillis() + "") + "_clip.mp4";
//                outputPath = Constants.getPath("video/clip/", System.currentTimeMillis() + ".mp4");

                clipper.setFilterType(filterType);
                clipper.setOutputVideoPath(outputPath);
                clipper.setOnVideoCutFinishListener(new VideoClipper.OnVideoCutFinishListener() {
                    @Override
                    public void onFinish() {
                        mHandler.sendEmptyMessage(VIDEO_CUT_FINISH);
                    }
                });

                try {
                    clipper.clipVideo(0, mVideoView.getVideoDuration() * 1000);
                } catch (IOException e) {
                    e.printStackTrace();

                }


                break;

        }
    }

    @Override
    public void onVideoPrepare() {
        mHandler.sendEmptyMessage(VIDEO_PREPARE);
    }

    @Override
    public void onVideoStart() {
        mHandler.sendEmptyMessage(VIDEO_START);
    }

    @Override
    public void onVideoPause() {
        mHandler.sendEmptyMessage(VIDEO_PAUSE);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mVideoView.seekTo(startPoint);
        mVideoView.start();
    }

    @Override
    public void onVideoChanged(VideoInfo info) {

    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            while (!isDestroy) {
                if (!isPlaying) {
                    try {
                        Thread.currentThread().sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                mHandler.sendEmptyMessage(VIDEO_UPDATE);
                try {
                    Thread.currentThread().sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mVideoView.onTouch(event);
        return true;
    }

    @Override
    public void onFilterChanged(MagicFilterType filterType) {
        this.filterType = filterType;
        mVideoView.setLvLeo(filterType);
    }


    /**
     * 隐藏loading
     */
    public void endLoading() {
        if (null != loading) {
            loading.dismiss();
        }
    }

    /**
     * 是否响应back键
     *
     * @param cancelable true：响应，false：不响应
     */
    public void setLoadingCancelable(boolean cancelable) {
        if (null != loading) {
            loading.setCancelable(cancelable);
        }
    }


    public boolean isLoading() {
        return loading != null && loading.isShowing();
    }


    /**
     * @param tips
     * @param cancelable 是否可取消  false 不可以  true 可以
     */
    public void showLoading(final String tips, final boolean cancelable) {
        if (isDestroyed()){
            return;
        }
        endLoading();
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loading = new LoadingDialog(this, tips, cancelable);
            loading.show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loading = new LoadingDialog(PreviewActivity.this, tips, cancelable);
                    loading.show();
                }
            });
        }
    }
}
