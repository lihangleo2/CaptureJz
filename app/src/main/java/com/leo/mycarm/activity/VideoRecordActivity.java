package com.leo.mycarm.activity;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.mycarm.Constants;
import com.leo.mycarm.R;
import com.leo.mycarm.widget.CameraView;
import com.leo.mycarm.widget.FocusImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Leo on 2018/8/3.
 */

public class VideoRecordActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {
    private ImageView image_Video;
    private ProgressBar progress_bar;
    private CameraView mCameraView;
    private FocusImageView mFocus;

    private ImageView btn_camera_switch;

    ExecutorService executorService;
    private String savePath;
    private boolean recordFlag = false;//是否正在录制

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
                    handler.removeCallbacks(mRunnable);
                    break;
                case 12:
//                    onSelect(path, "");
                    Toast.makeText(VideoRecordActivity.this, "文件保存路径：" + path, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private Runnable mRunnable;
    private int progress;
    private TextView text_time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videorecord);
        executorService = Executors.newSingleThreadExecutor();

        image_Video = (ImageView) findViewById(R.id.image_Video);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        mCameraView = (CameraView) findViewById(R.id.mCameraView);
        btn_camera_switch = (ImageView) findViewById(R.id.btn_camera_switch);
        btn_camera_switch.setOnClickListener(this);
        mCameraView.setOnTouchListener(this);
        mFocus = (FocusImageView) findViewById(R.id.mFocus);
        text_time = (TextView) findViewById(R.id.text_time);
        image_Video.setOnClickListener(this);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                progress++;
                if (progress == 1000) {
                    //达到顶值了
                    handler.sendEmptyMessage(11);
                }
                progress_bar.setProgress(progress);
                text_time.setText(progress / 50 + "S");
                handler.postDelayed(this, 20);
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_Video:
                //这是状态的
                if (image_Video.isSelected()) {
                    image_Video.setSelected(false);


                    if (progress < 100) {
                        mCameraView.pause(false);
                        Toast.makeText(VideoRecordActivity.this, "录像时间太短", Toast.LENGTH_SHORT).show();
                    } else {
                        mCameraView.stopRecord();
                        recordFlag = false;
                        recordComplete(savePath);
                    }

                    handler.removeCallbacks(mRunnable);


                } else {
                    image_Video.setSelected(true);

                    if (!recordFlag) {
                        handler.post(mRunnable);
                        long time = System.currentTimeMillis();
                        savePath = Constants.getPath("record/", time + ".mp4");
                        Log.e("我这里是什么",savePath+"============1111111111111");
                        mCameraView.setSavePath(savePath);
                        executorService.execute(recordRunnable);
                    } else {
                        mCameraView.resume(false);
                        handler.post(mRunnable);
                    }
                }

                break;

            case R.id.btn_camera_switch:
                mCameraView.switchCamera();
                if (mCameraView.getCameraId() == 1) {
                    //前置摄像头 使用美颜
                    mCameraView.changeBeautyLevel(1);
                } else {
                    //后置摄像头不使用美颜
                    mCameraView.changeBeautyLevel(0);
                }
                break;
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mCameraView.onTouch(event);
        if (mCameraView.getCameraId() == 1) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float sRawX = event.getRawX();
                float sRawY = event.getRawY();
                float rawY = sRawY * Constants.screenWidth / Constants.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (Constants.screenWidth - temp) * Constants.screenHeight / Constants.screenWidth;

                Point point = new Point((int) rawX, (int) rawY);
                mCameraView.onFocus(point, callback);
                mFocus.startFocus(new Point((int) sRawX, (int) sRawY));
        }
        return true;
    }


    Camera.AutoFocusCallback callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            Log.e("hero", "----onAutoFocus====" + success);
            if (success) {
                mFocus.onFocusSuccess();
            } else {
                //聚焦失败显示的图片
                mFocus.onFocusFailed();

            }
        }
    };


    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            recordFlag = true;
            try {
                mCameraView.startRecord();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    String path;

    private void recordComplete(final String path) {
        this.path = path;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress = 0;
                progress_bar.setProgress(0);
                text_time.setText("0S");

//                //保存在系统相册里面
//                Uri localUri = Uri.fromFile(new File(path));
//                Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
//                sendBroadcast(localIntent);


//                handler.sendEmptyMessageDelayed(12,3000);

//                onSelect(path,"");
                Toast.makeText(VideoRecordActivity.this, "文件保存路径：" + path, Toast.LENGTH_SHORT).show();
                Log.e("我这里是什么",path+"============222222222222222222");

//                Intent intent=new Intent(VideoRecordActivity.this,PreviewActivity.class);
//                intent.putExtra("path",path);
//                startActivity(intent);

            }
        });
    }


//    public void onSelect(final String path, String cover) {
//        //处理音频，视频
//        int videoTrack=-1;
//        int audioTrack=-1;
//        MediaExtractor extractor=new MediaExtractor();
//        try {
//            extractor.setDataSource(path);
//            for (int i = 0; i < extractor.getTrackCount(); i++) {
//                MediaFormat format = extractor.getTrackFormat(i);
//                if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
//                    videoTrack=i;
//                    String videoMime = format.getString(MediaFormat.KEY_MIME);
//                    if(!MediaFormat.MIMETYPE_VIDEO_AVC.equals(videoMime) ){
//                        Toast.makeText(this,"视频格式不支持", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    continue;
//                }
//                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
//                    audioTrack=i;
//                    String audioMime = format.getString(MediaFormat.KEY_MIME);
//                    if(!MediaFormat.MIMETYPE_AUDIO_AAC.equals(audioMime)){
//                        Toast.makeText(this,"视频格式不支持", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    continue;
//                }
//            }
//            extractor.release();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this,"视频格式不支持", Toast.LENGTH_SHORT).show();
//            extractor.release();
//            return;
//        }
//        if(videoTrack==-1||audioTrack==-1){
//            Toast.makeText(this,"视频格式不支持", Toast.LENGTH_SHORT).show();
//            return;
//        }
////        if (pageType == TYPE_BACK_PATH){
////            Intent intent = getIntent();
////            intent.putExtra("path",path);
////            setResult(0,intent);
////            finish();
////            return;
////        }
//        AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
//        mDialog.setMessage("去分离音频还是添加滤镜");
//        mDialog.setPositiveButton("加滤镜", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //跳转预览界面
//                if(!TextUtils.isEmpty(path)){
//                    Intent intent=new Intent(VideoRecordActivity.this,PreviewActivity.class);
//                    intent.putExtra("path",path);
//                    startActivity(intent);
//                    dialog.dismiss();
//                }
//            }
//        });
//        mDialog.setNegativeButton("分离音频", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if(!TextUtils.isEmpty(path)){
//                    Intent intent=new Intent(VideoRecordActivity.this,AudioPreviewActivity.class);
//                    intent.putExtra("path",path);
//                    startActivity(intent);
//                    dialog.dismiss();
//                }
//            }
//        });
//        mDialog.show();
//    }


}
