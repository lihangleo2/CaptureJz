package com.leo.mycarm.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.leo.mycarm.Constants;
import com.leo.mycarm.ImageUtil;
import com.leo.mycarm.R;
import com.leo.mycarm.TimeUtils;
import com.leo.mycarm.camera.CameraController;
import com.leo.mycarm.camera.OnCaptureData;
import com.leo.mycarm.faceai.EventUtil;
import com.leo.mycarm.faceai.FaceView;
import com.leo.mycarm.faceai.GoogleFaceDetect;
import com.leo.mycarm.gpufilter.SlideGpuFilterGroup;
import com.leo.mycarm.gpufilter.helper.MagicFilterType;
import com.leo.mycarm.widget.CameraView;
import com.leo.mycarm.widget.CircularProgressView;
import com.leo.mycarm.widget.FocusImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cj on 2017/7/25.
 * desc 视频录制
 * 主要包括 音视频录制、断点续录、对焦等功能
 */

public class RecordedActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener, SlideGpuFilterGroup.OnFilterChangeListener, OnCaptureData {

    private CameraView mCameraView;
    private CircularProgressView mCapture;
    private FocusImageView mFocus;
    private ImageView mBeautyBtn;
    private ImageView mFilterBtn;
    private ImageView mCameraChange;
    private static final int maxTime = 20000;//最长录制20s
    private boolean pausing = false;
    private boolean recordFlag = false;//是否正在录制

    private int WIDTH = 720, HEIGHT = 1280;


    private long timeStep = 50;//进度条刷新的时间
    long timeCount = 0;//用于记录录制时间
    private boolean autoPausing = false;
    ExecutorService executorService;


    /**
     * 人脸识别相关
     */
    GoogleFaceDetect googleFaceDetect = null;
    private MainHandler mMainHandler = null;

    private FaceView faceView;

    private ImageView imageTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorde);
        executorService = Executors.newSingleThreadExecutor();
        initView();

        googleFaceDetect = new GoogleFaceDetect(RecordedActivity.this);
        mMainHandler = new MainHandler(faceView, googleFaceDetect);
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
        imageTest = (ImageView) findViewById(R.id.imageTest);
    }

    private void initView() {
        faceView = (FaceView) findViewById(R.id.face_view);

        mCameraView = (CameraView) findViewById(R.id.camera_view);
        mCapture = (CircularProgressView) findViewById(R.id.mCapture);
        mFocus = (FocusImageView) findViewById(R.id.focusImageView);
        mBeautyBtn = (ImageView) findViewById(R.id.btn_camera_beauty);
        mFilterBtn = (ImageView) findViewById(R.id.btn_camera_filter);
        mCameraChange = (ImageView) findViewById(R.id.btn_camera_switch);


        mBeautyBtn.setOnClickListener(this);
        faceView.setOnTouchListener(this);
        mCameraView.setOnFilterChangeListener(this);
        mCameraChange.setOnClickListener(this);
        mCapture.setTotal(maxTime);
        mCapture.setOnClickListener(this);
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



    @Override
    public void onBackPressed() {
        if (recordFlag) {
            recordFlag = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
        Toast.makeText(this, R.string.change_filter, Toast.LENGTH_SHORT).show();
        if (recordFlag && autoPausing) {
            mCameraView.resume(true);
            autoPausing = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recordFlag && !pausing) {
            mCameraView.pause(true);
            autoPausing = true;
        }
        mCameraView.onPause();
    }

    @Override
    public void onFilterChange(final MagicFilterType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == MagicFilterType.NONE) {
                    Toast.makeText(RecordedActivity.this, "当前没有设置滤镜--" + type, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RecordedActivity.this, "当前滤镜切换为--" + type, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera_switch:
                mCameraView.switchCamera();
                if (mCameraView.getCameraId() == 1) {
                    //前置摄像头 使用美颜
                    mCameraView.changeBeautyLevel(1);
                } else {
                    //后置摄像头不使用美颜
                    mCameraView.changeBeautyLevel(0);
                }
                mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
                break;
            case R.id.mCapture:
                if (!recordFlag) {
                    executorService.execute(recordRunnable);
                } else if (!pausing) {
                    mCameraView.pause(false);
                    pausing = true;
                } else {
                    mCameraView.resume(false);
                    pausing = false;
                }
                break;
            case R.id.btn_camera_beauty:
                CameraController.getInstance().tackPicture(RecordedActivity.this);
//                if (mCameraView.getCameraId() == 0){
//                    Toast.makeText(this, "后置摄像头 不使用美白磨皮功能", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                new AlertDialog.Builder(RecordedActivity.this)
//                        .setSingleChoiceItems(new String[]{"关闭", "1", "2", "3", "4", "5"}, mCameraView.getBeautyLevel(),
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        mCameraView.changeBeautyLevel(which);
//                                        dialog.dismiss();
//                                    }
//                                })
//                        .setNegativeButton("取消", null)
//                        .show();
                break;
        }
    }

    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            recordFlag = true;
            pausing = false;
            autoPausing = false;
            timeCount = 0;
            long time = System.currentTimeMillis();
            String savePath = Constants.getPath("record/", time + ".mp4");

            try {
                mCameraView.setSavePath(savePath);
                mCameraView.startRecord();
                while (timeCount <= maxTime && recordFlag) {
                    if (pausing || autoPausing) {
                        continue;
                    }
                    mCapture.setProcess((int) timeCount);
                    Thread.sleep(timeStep);
                    timeCount += timeStep;
                }
                recordFlag = false;
                mCameraView.stopRecord();
                if (timeCount < 2000) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RecordedActivity.this, "录像时间太短", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                    recordComplete(savePath);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void recordComplete(final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCapture.setProcess(0);
                Toast.makeText(RecordedActivity.this, "文件保存路径：" + path, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCapture(boolean success, byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        savePicture(data);
    }


    private class MainHandler extends Handler {
        private final WeakReference<FaceView> mFaceViewWeakReference;
        private final WeakReference<GoogleFaceDetect> mGoogleFaceDetectWeakReference;

        public MainHandler(FaceView faceView, GoogleFaceDetect googleFaceDetect) {
            mFaceViewWeakReference = new WeakReference<>(faceView);
            mGoogleFaceDetectWeakReference = new WeakReference<>(googleFaceDetect);
            mGoogleFaceDetectWeakReference.get().setHandler(MainHandler.this);
        }


        private int whatFlag = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EventUtil.UPDATE_FACE_RECT:
                    Camera.Face[] faces = (Camera.Face[]) msg.obj;
                    mFaceViewWeakReference.get().setFaces(faces);
                    if (faces.length == 0) {
                        whatFlag = 0;
                        faceView.clearFaces();
                        Log.e("如果没用就尴尬了", "33");
                    } else {
                        Log.e("如果没用就尴尬了", "44");
                        whatFlag++;

                        Point point = faces[0].mouth;
                        Point point_right = faces[0].rightEye;
                        Point point_left = faces[0].leftEye;
                        Rect rect = faces[0].rect;
                        Log.e("我看看这是什么东西啊嘴巴", point.x + "==================" + point.y);
                        Log.e("我看看这是什么东西啊右眼", point_right.x + "==================" + point_right.y);
                        Log.e("我看看这是什么东西啊左眼", point_left.x + "==================" + point_left.y);
                        Log.e("我看看这是什么东西啊aaaaaa", rect.top + "==================" + rect.left + "================" + rect.bottom + "===================" + rect.right);

                        if (whatFlag >= 35) {
                            Log.e("走了多少次了呢", "可以拍照上传了!!!");
//                            CameraInterface.getInstance().tackPicture(CameraAPIActivity.this);
//                            CameraInterface.getInstance().doStopCamera();
                            //预览的显示
//                            CameraInterface.getInstance().tackPictureLeoAr(CameraAPIActivity.this);

                        } else {
                            Log.e("走了多少次了呢", "请对准脸部");
                        }
                    }


                    break;
                case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
                    Log.e("如果没用就尴尬了", "11");
                    Camera.Parameters params = CameraController.getInstance().getCameraParams();
                    if (params != null && params.getMaxNumDetectedFaces() > 0) {
                        if (mFaceViewWeakReference.get() != null) {
                            mFaceViewWeakReference.get().clearFaces();
                        }
                        CameraController.getInstance().getCameraDevice().setFaceDetectionListener(mGoogleFaceDetectWeakReference.get());
                        CameraController.getInstance().getCameraDevice().stopFaceDetection();
                        CameraController.getInstance().getCameraDevice().startFaceDetection();
                        Log.e("如果没用就尴尬了", "22");
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }


    /**
     * 保存图片
     *
     * @param data
     * @return
     */
    private String savePicture(byte[] data) {

        File imgFileDir = getImageDir();
        if (!imgFileDir.exists() && !imgFileDir.mkdirs()) {
            return null;
        }
//		文件路径路径
//        String imgFilePath = imgFileDir.getPath() + File.separator + this.generateFileName();
        String imgFilePath = getFilesDir().getAbsolutePath().toString() + "/" + TimeUtils.getDateToStringLeo(System.currentTimeMillis() + "") + "_atmancarm.jpg";
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);


        Bitmap rotatedBitmap = null;
        if (null != b) {
            if (CameraController.getInstance().getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                rotatedBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
            } else if (CameraController.getInstance().getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotatedBitmap = ImageUtil.getRotateBitmap(b, -90.0f);
            }
        }
        imageTest.setImageBitmap(rotatedBitmap);
        Log.e("没有跑到这里来吗", "1111111111111111111111111");


//        File imgFile = new File(imgFilePath);
//        FileOutputStream fos = null;
//        BufferedOutputStream bos = null;
//        try {
//            fos = new FileOutputStream(imgFile);
//            bos = new BufferedOutputStream(fos);
//            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//        } catch (Exception error) {
//            return null;
//        } finally {
//            try {
//                if (fos != null) {
//                    fos.flush();
//                    fos.close();
//                }
//                if (bos != null) {
//                    bos.flush();
//                    bos.close();
//                }
//            } catch (IOException e) {
//            }
//
//        }

        return imgFilePath;
    }


    /**
     * @return
     */
    private File getImageDir() {
        String path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
}
