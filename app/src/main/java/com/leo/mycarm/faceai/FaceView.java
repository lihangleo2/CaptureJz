package com.leo.mycarm.faceai;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.leo.mycarm.camera.CameraController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lchad on 2017/7/20.
 */
public class FaceView extends android.support.v7.widget.AppCompatImageView {
    private Paint mLinePaint;
    private Face[] mFaces;
    private Matrix mMatrix = new Matrix();
    private RectF mRect = new RectF();


    private RectF mRectEye = new RectF();
    private RectF mRectEyeLeft = new RectF();
    private RectF mRectMouth = new RectF();


    private Drawable mLeftEyeDraw = null;//左眼图片
    private Drawable mRightEyeDraw = null;//右眼图片


    //记录可点击区域的坐标
    private List<Rect> mClickableRecord = new ArrayList<>();

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mLeftEyeDraw = getResources().getDrawable(R.mipmap.ic_eye_left);
//        mRightEyeDraw = getResources().getDrawable(R.mipmap.ic_eye_right);

        initPaint();
        for (int i = 0; i < 10; i++) {
            mClickableRecord.add(new Rect(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE));
        }
    }


    public void setFaces(Face[] faces) {
        this.mFaces = faces;
        if (faces.length < 1) {
            return;
        }
        Point rightEye = faces[0].rightEye;
        Log.i("这里到底有害是没有", rightEye.x + "=========" + rightEye.y);

        invalidate();
    }

    public void clearFaces() {
        mFaces = null;
        invalidate();
    }

    private void resetClickableRect() {
        for (Rect rect : mClickableRecord) {
            rect.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFaces == null || mFaces.length < 1) {
            return;
        }
        boolean isMirror = false;
        int cameraId = CameraController.getInstance().getCameraId();
        if (cameraId == CameraInfo.CAMERA_FACING_BACK) {
            isMirror = false;
        } else if (cameraId == CameraInfo.CAMERA_FACING_FRONT) {
            isMirror = true;
        }
        Util.prepareMatrix(mMatrix, isMirror, 90, getWidth(), getHeight());
        canvas.save();
        mMatrix.postRotate(0);
        canvas.rotate(-0);

        resetClickableRect();

        for (int i = 0; i < mFaces.length; i++) {
            Face mFace = mFaces[i];
            mRect.set(mFace.rect);
            mMatrix.mapRect(mRect);
            canvas.drawRoundRect(mRect, 15, 15, mLinePaint);
            Log.e("这里是正确的坐标吗", mRect.top + "==========" + mRect.left + "==============" + mRect.bottom + "=============" + mRect.right);

            /***
             * ************************************************************************
             */


//            float[] leftEye = {mFace.leftEye.x, mFace.leftEye.y};
//            mMatrix.mapPoints(leftEye);//计算出在父布局的真是坐标
//            canvas.drawCircle(leftEye[0], leftEye[1], 10, mLinePaint);
//
//            Log.i("怎么判断头部方向啊", "左眼=======" + leftEye[0] + "=============" + leftEye[1]);
//            float[] rightEye = {mFace.rightEye.x, mFace.rightEye.y};
//            mMatrix.mapPoints(rightEye);//计算出在父布局的真是坐标
//            canvas.drawCircle(rightEye[0], rightEye[1], 10, mLinePaint);
//
//            Log.i("怎么判断头部方向啊", "右眼===========" + rightEye[0] + "=============" + rightEye[1]);
//
//            float[] mouth = {mFace.mouth.x, mFace.mouth.y};
//            mMatrix.mapPoints(mouth);//计算出在父布局的真是坐标
//            canvas.drawCircle(mouth[0], mouth[1], 10, mLinePaint);
//            Log.i("怎么判断头部方向啊", "嘴巴=================" + mouth[0] + "=============" + mouth[1]);
//
//
//            /**
//             * 两眼之间的距离
//             */
//            int xCha = Math.round(leftEye[0] - rightEye[0]);
//            int yCha = Math.round(leftEye[1] - rightEye[1]);
//            int yanju = (int) Math.sqrt(xCha * xCha + yCha * yCha);
//            Log.i("看看差值就知道了", "两眼之间的距离------- >" + yanju);
//            /**
//             * 右眼到嘴巴的距离
//             */
//            int xCha_right = Math.round(rightEye[0] - mouth[0]);
//            int yCha_right = Math.round(rightEye[1] - mouth[1]);
//            int mRight = (int) Math.sqrt(xCha_right * xCha_right + yCha_right * yCha_right);
//            Log.i("看看差值就知道了", "右眼到嘴巴的距离+++++++ >" + mRight);
//            /**
//             * 左眼到嘴巴的距离
//             */
//            int xCha_left = Math.round(leftEye[0] - mouth[0]);
//            int yCha_left = Math.round(leftEye[1] - mouth[1]);
//            int mLeft = (int) Math.sqrt(xCha_left * xCha_left + yCha_left * yCha_left);
//            Log.i("看看差值就知道了", "左眼到嘴巴的距离======= >" + mLeft);

            /***
             * ************************************************************************
             */


            //放上眼睛的图片的
            //识别的Rect 系数，使装饰图片根据人脸与摄像头的距离放大或者缩小

//            float dx = mRect.bottom - mRect.top;
//            float[] leftEye = {mFace.leftEye.x, mFace.leftEye.y};
//            mMatrix.mapPoints(leftEye);//计算出在父布局的真是坐标
//            float h = dx * 0.1f;
//            float w = dx * 0.15f;
//            mLeftEyeDraw.setBounds(Math.round(leftEye[0]-w), Math.round(leftEye[1]-h),
//                    Math.round(leftEye[0]+w), Math.round(leftEye[1]+h));
//            mLeftEyeDraw.draw(canvas);
//
//            float[] rightEye = {mFace.rightEye.x, mFace.rightEye.y};
//            float ww = dx * 0.15f;
//            float hh = dx * 0.1f;
//            mMatrix.mapPoints(rightEye);//计算出在父布局的真是坐标
//            mRightEyeDraw.setBounds(Math.round(rightEye[0]-ww), Math.round(rightEye[1]-hh),
//                    Math.round(rightEye[0]+ww), Math.round(rightEye[1]+hh));
//            mRightEyeDraw.draw(canvas);


        }


        canvas.restore();
        super.onDraw(canvas);
    }

    private void initPaint() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int color = Color.rgb(255, 255, 255);
        mLinePaint.setColor(color);
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(4f);
        mLinePaint.setAlpha(180);
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, false);
    }

    /**
     * 捕捉点击事件，并通过回调接口传出
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                handlerClickEvent((int) event.getX(), (int) event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 如果有多张脸的矩形区域相互交叉呢
     */
    private void handlerClickEvent(int x, int y) {
        Log.e("手指点击的xy坐标", x + "===========================" + y);
        //单击之后,有效的脸部区域
        List<Rect> valueFaceRect = new ArrayList<>();
        for (Rect rect : mClickableRecord) {
            if (rect.contains(x, y)) {
                valueFaceRect.add(new Rect(rect));
            }
        }
        if (mSingleTapListener != null && valueFaceRect.size() > 0) {
            mSingleTapListener.onSingleTap(x, y, valueFaceRect);
        }
    }

    public interface SingleTapListener {
        void onSingleTap(float x, float y, List<Rect> rects);
    }

    private SingleTapListener mSingleTapListener;

    public void setSingleTapListener(SingleTapListener singleTapListener) {
        mSingleTapListener = singleTapListener;
    }
}
