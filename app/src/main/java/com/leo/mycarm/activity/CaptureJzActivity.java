package com.leo.mycarm.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.leo.mycarm.widget.CameraView;
import com.leo.mycarm.widget.FocusImageView;
import com.leo.mycarm.widget.VideoControlView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import io.reactivex.functions.Consumer;

/**
 * 2018/10/22
 * by leo。你的相机首页
 */


public class CaptureJzActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, OnCaptureData {
    private CameraView mCameraView;
    private FocusImageView mFocus;
    private RelativeLayout relativeLayout_switch;//翻转相机
    private RelativeLayout relative_beauty;//美颜
    private int currentBeauty = 1;//目前美颜级别
    private RelativeLayout relative_flash;//闪光灯
    private int currentFlashMode = 3;
    private int flag = 0;
    private ImageView image_flash;


    private RxPermissions rxPermissions;


    /**
     * 人脸识别相关
     */
    GoogleFaceDetect googleFaceDetect = null;
    private CaptureJzActivity.MainHandler mMainHandler = null;

    private FaceView faceView;
    private VideoControlView videoControlView;

    private String savePath;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
                    if (!TextUtils.isEmpty(picPath)) {
                        Intent intent = new Intent(CaptureJzActivity.this, PicEditorActivity.class);
                        intent.putExtra("path", picPath);
                        startActivity(intent);
                        videoControlView.setIsCan(true);
                    }
                    break;

                case 12:
                    Intent intent = new Intent(CaptureJzActivity.this, PreviewActivity.class);
                    intent.putExtra("path", savePath);
                    startActivity(intent);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capturejz);
        videoControlView = findViewById(R.id.control_view);
        rxPermissions = new RxPermissions(this);
        initView();
        videoControlView.setOnRecordListener(new VideoControlView.OnRecordListener() {
            @Override
            public void onShortClick() {
                videoControlView.setIsCan(false);
                CameraController.getInstance().tackPicture(CaptureJzActivity.this);
            }

            @Override
            public void OnRecordStartClick() {

                long time = System.currentTimeMillis();
                savePath = Constants.getPath("record/", time + ".mp4");
                mCameraView.setSavePath(savePath);
                mCameraView.startRecord();

            }

            @Override
            public void OnFinish(int resultCode) {
                switch (resultCode) {
                    case 0:
                        mCameraView.stopRecord();
                        Toast.makeText(CaptureJzActivity.this, "录制时间过短", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        mCameraView.stopRecord();
                        mHandler.sendEmptyMessageDelayed(12,500);
                        break;
                }

            }
        });

        relative_beauty.setSelected(true);//初始美颜选中

        googleFaceDetect = new GoogleFaceDetect(CaptureJzActivity.this);
        mMainHandler = new CaptureJzActivity.MainHandler(faceView, googleFaceDetect);
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
        checkAndPermiss();
        picPath = "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }


    public void initView() {
        faceView = (FaceView) findViewById(R.id.face_view);
        relative_beauty = (RelativeLayout) findViewById(R.id.relative_beauty);
        relative_flash = (RelativeLayout) findViewById(R.id.relative_flash);
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        mFocus = (FocusImageView) findViewById(R.id.focusImageView);
        relativeLayout_switch = (RelativeLayout) findViewById(R.id.relative_switch);
        image_flash = (ImageView) findViewById(R.id.image_flash);
        faceView.setOnTouchListener(this);
        relative_flash.setOnClickListener(this);
        relativeLayout_switch.setOnClickListener(this);
        relative_beauty.setOnClickListener(this);
    }

    public boolean checkAndPermiss() {
        if (ActivityCompat.checkSelfPermission(CaptureJzActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(CaptureJzActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                ) {
            rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {

                    if (aBoolean) {
                        mCameraView.openCamera();
                    } else {
                        Toast.makeText(CaptureJzActivity.this, "请打开相关权限保持正常使用！", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_switch:
                if (!checkAndPermiss()) {
                    return;
                }

                mCameraView.switchCamera();
                if (mCameraView.getCameraId() == 1) {
                    //前置摄像头 使用美颜
                    mCameraView.changeBeautyLevel(currentBeauty);
                    relative_beauty.setVisibility(View.VISIBLE);
                    relative_flash.setVisibility(View.GONE);

                } else {
                    //后置摄像头不使用美颜
                    mCameraView.changeBeautyLevel(0);
                    relative_beauty.setVisibility(View.GONE);
                    relative_flash.setVisibility(View.VISIBLE);
                }
                mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
                break;

            case R.id.relative_beauty:
                if (relative_beauty.isSelected()) {
                    mCameraView.changeBeautyLevel(0);
                    currentBeauty = 0;
                    relative_beauty.setSelected(false);
                } else {
                    mCameraView.changeBeautyLevel(1);
                    currentBeauty = 1;
                    relative_beauty.setSelected(true);
                }
                break;
            case R.id.relative_flash://1是自动  2 是开启  3是关闭
                flag++;
                switch (flag % 3) {
                    case 0:
                        image_flash.setImageResource(R.mipmap.flash_off);
                        CameraController.getInstance().setFlashlight(3);
                        currentFlashMode = 3;
                        break;

                    case 1:
                        image_flash.setImageResource(R.mipmap.flash_on);
                        CameraController.getInstance().setFlashlight(2);
                        currentFlashMode = 2;
                        break;

                    case 2:

                        image_flash.setImageResource(R.mipmap.flash_a);
                        CameraController.getInstance().setFlashlight(1);
                        currentFlashMode = 1;
                        break;
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


    private class MainHandler extends Handler {
        private final WeakReference<FaceView> mFaceViewWeakReference;
        private final WeakReference<GoogleFaceDetect> mGoogleFaceDetectWeakReference;

        public MainHandler(FaceView faceView, GoogleFaceDetect googleFaceDetect) {
            mFaceViewWeakReference = new WeakReference<>(faceView);
            mGoogleFaceDetectWeakReference = new WeakReference<>(googleFaceDetect);
            mGoogleFaceDetectWeakReference.get().setHandler(CaptureJzActivity.MainHandler.this);
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


    private String picPath;

    @Override
    public void onCapture(boolean success, byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        picPath = savePicture(data);
        mHandler.sendEmptyMessageDelayed(11, 500);
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
        //文件路径路径//解决小米路径找不到
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
        Log.e("没有跑到这里来吗", "保存图片成功");
        File imgFile = new File(imgFilePath);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(imgFile);
            bos = new BufferedOutputStream(fos);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception error) {
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
            }

        }

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
