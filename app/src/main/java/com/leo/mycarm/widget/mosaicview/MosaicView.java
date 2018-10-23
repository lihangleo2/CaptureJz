package com.leo.mycarm.widget.mosaicview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.leo.mycarm.interpagek.OnMotionEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 实现逻辑 ：1、bmCoverLayer为填充的背景，在这个背景上涂鸦取被涂鸦的背景(涂鸦与背景的交集)产生bmMosaicLayer
 * 2、再将bmMosaicLayer叠加到原图bmBaseLayer上，产生结果输出为文件
 *
 * @author xiaoqun
 */
public class MosaicView extends ViewGroup {
    public static final String TAG = "MosaicView";

    /**
     * 效果枚举类型
     *
     * @author xiaoqun
     */
    public static enum Effect {
        GRID, COLOR, BLUR,
    }

    ;

    /**
     * 效果枚举类型
     *
     * @author xiaoqun
     */
    public static enum Mode {
        GRID, PATH,
    }

    // default image inner padding, in dip pixels，图片距离上下左右的间距
    private static final int INNER_PADDING = 0;

    /**
     * default grid width, in dip pixels,马赛克色块大小
     */
    private static final int GRID_WIDTH = 7;

    /**
     * 图片实际宽度
     */
    private int mImageWidth;

    public int getmImageWidth() {
        return mImageWidth;
    }

    /**
     * 图片实际高度
     */
    private int mImageHeight;

    public int getmImageHeight() {
        return mImageHeight;
    }

    /**
     * 要处理的图片的bitmap
     */
    private Bitmap bmBaseLayer;

    public Bitmap getBmBaseLayer() {
        return bmBaseLayer;
    }

    /**
     * 根据效果设置马赛克、某种颜色、模糊效果的bitmap
     */
    private Bitmap bmCoverLayer;
    /**
     * 涂画的bitmap,通过在空白的Canvas上画bmCoverLayer和涂鸦路径来形成一个图片，
     * 最终结果通过原图片bmBaseLayer和bmMosaicLayer合成
     */
    private Bitmap bmMosaicLayer;

    //leo 马赛克图层  优化 避免每次move都会生成
    private Bitmap bmTouchLayer;

    public Bitmap getBmMosaicLayer() {
        return bmMosaicLayer;
    }

    /**
     * 手势涂鸦时先记录按下坐标
     */
    private Point startPoint;
    /**
     * 马赛克色块大小
     */
    private int mGridWidth = 0;
    /**
     * 涂鸦画笔大小
     *
     * @param width
     */
    private int mPathWidth = 0;

    public int getmPathWidth() {
        return mPathWidth;
    }


    /**
     * 设置涂鸦画笔大小
     *
     * @param //width
     */
    public void setmPathWidth(int mPathWidth) {
//        float ratio = (mImageRect.right - mImageRect.left)
//                / (float) mImageWidth;
        mPathWidth = (int) (mPathWidth / leoRio);
//        mPathWidth = mPathWidth * mImageWidth / UIUtil.getWidth();
        this.mPathWidth = mPathWidth;

    }

    /**
     * 矩形选择边框大小
     */
    private int mStrokeWidth = 6;
    /**
     * 矩形选择边框颜色
     */
    private int mStrokeColor = 0xff2a5caa;

    public int getmStrokeColor() {
        return mStrokeColor;
    }

    /**
     * 设置矩形选择边框颜色
     *
     * @param mStrokeColor
     */
    public void setmStrokeColor(int mStrokeColor) {
        this.mStrokeColor = mStrokeColor;
    }

    /**
     * 图片路径
     */
    public String inPath;
    private String outPath;
    /**
     * 效果
     */
    private Effect mEffect;
    /**
     * 模式
     */
    private Mode mMode;
    /**
     * 图片在手机上显示的区域，是根据图片缩放后的形状
     */
    private Rect mImageRect;

    private Rect mImageRect_base;

    public Rect getmImageRect_base() {
        return mImageRect_base;
    }

    public Rect getmImageRect() {
        return mImageRect;
    }

    /**
     * 初始化时创建的画笔，用来绘制矩形选择效果
     */
    private Paint mPaint;
    /**
     * 矩形选择时，当前选择的矩形区域
     */
    private Rect mTouchRect;
    /**
     * 涂鸦的路径
     */
    private List<Rect> mTouchRects;

    private Path mTouchPath;
    /**
     * 擦除涂鸦部分的路径
     */
    private List<Rect> mEraseRects;
    /**
     * 涂鸦颜色(设置bmCoverLayer的填充颜色)
     */
    private int mMosaicColor;
    /**
     * 选择的图片距离上下左右的间距
     */
    private int mPadding;

    /**
     * 涂鸦的路径
     */
    private List<Path> mTouchPaths;
    /**
     * 涂鸦路径对应尺寸
     */
    SparseIntArray pathSizeMap;
    /**
     * 擦除涂鸦部分的路径
     */
    private List<Path> mErasePaths;
    /**
     * 设置是涂鸦还是擦除,为true时涂鸦，为false擦除涂鸦部分
     */
    private boolean mMosaic;


    private OnMotionEventListener onMotionEventListener;

    public void setOnMotionEventListener(OnMotionEventListener onMotionEventListener) {
        this.onMotionEventListener = onMotionEventListener;
    }

    /**
     * 构造方法
     *
     * @param context
     */
    public MosaicView(Context context) {
        super(context);
        initImage();
    }

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     */
    public MosaicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initImage();
    }


    /**
     * 实例化时，初始化变量，默认，效果：马赛克，模式:手势涂鸦
     */
    private void initImage() {
        mMosaic = true;

        mTouchRects = new ArrayList<Rect>();
        mEraseRects = new ArrayList<Rect>();

        mTouchPaths = new ArrayList<Path>();
        pathSizeMap = new SparseIntArray();
        mErasePaths = new ArrayList<Path>();

        mPadding = dp2px(INNER_PADDING);

//        mGridWidth = dp2px(GRID_WIDTH);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mStrokeColor);

        mImageRect = new Rect();
        mImageRect_base = new Rect();
        setWillNotDraw(false);

        mMode = Mode.PATH;
        mEffect = Effect.GRID;
    }

    /**
     * 设置要涂鸦的图片，初始化数据
     *
     * @param absPath
     */
    public void setSrcPath(String absPath) {


        File file = new File(absPath);
        if (file == null || !file.exists()) {
            Log.w(TAG, "invalid file path " + absPath);
            return;
        }

        reset_when_change_img();

        inPath = absPath;
        String fileName = file.getName();
        String parent = file.getParent();
//		int index = fileName.lastIndexOf(".");
//		String stem = fileName.substring(0, index);
//		String newStem = stem + "_mosaic";
//		fileName = fileName.replace(stem, newStem);
        fileName = fileName + "_mosaic";
        outPath = parent + "/" + fileName;

        BitmapUtil.Size size = BitmapUtil.getImageSize(inPath);
        mImageWidth = size.width;
        mImageHeight = size.height;


        bmBaseLayer = BitmapUtil.getImage(absPath);

//        bmCoverLayer = getCoverLayer();
        bmMosaicLayer = null;
        bmTouchLayer = null;
        // 设置控件的布局后，通知执行onLayout来重新布局
        requestLayout();
        // View重画，执行onDraw()方法，来重画界面
        invalidate();
    }

    private boolean isZoom = false;

    public boolean isZoom() {
        return isZoom;
    }

    /**
     * 设置是否缩放
     *
     * @param isZoom
     */
    public void setZoom(boolean isZoom) {
        this.isZoom = isZoom;
    }

    /**
     * 设置效果，马赛克、模糊、纯色
     *
     * @param effect
     */
    public void setEffect(Effect effect) {
        if (mEffect == effect) {
            Log.d(TAG, "duplicated effect " + effect);
            return;
        }

        this.mEffect = effect;
        if (bmCoverLayer != null) {
            bmCoverLayer.recycle();
        }

        bmCoverLayer = getCoverLayer();
        if (mMode == Mode.GRID) {
            updateGridMosaic();
        } else if (mMode == Mode.PATH) {
            updatePathMosaic();
        }

        // View重画，执行onDraw()方法，来重画界面
        invalidate();
    }

    /**
     * 设置模式,手势涂鸦、矩形选择
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        if (mMode == mode) {
            Log.d(TAG, "duplicated mode " + mode);
            return;
        }

        if (bmMosaicLayer != null) {
            bmMosaicLayer.recycle();
            bmMosaicLayer = null;
        }

        this.mMode = mode;

        // View重画，执行onDraw()方法，来重画界面
        invalidate();
    }

    /**
     * 获取效果设置背景图，马赛克、模糊、纯色
     *
     * @return
     */
    private Bitmap getCoverLayer() {
        Bitmap bitmap = null;
        if (mEffect == Effect.GRID) {
            bitmap = getGridMosaic();
        } else if (mEffect == Effect.COLOR) {
            bitmap = getColorMosaic();
        } else if (mEffect == Effect.BLUR) {
            bitmap = getBlurMosaic();
        }
        return bitmap;
    }

    /**
     * 创建与原图大小一样的图片，用设置的颜色填充
     *
     * @return
     */
    private Bitmap getColorMosaic() {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, mImageWidth, mImageHeight);
        Paint paint = new Paint();
        paint.setColor(mMosaicColor);
        canvas.drawRect(rect, paint);
        canvas.save();
        return bitmap;
    }

    /**
     * 创建与原图大小一样的图片，设置模糊效果
     *
     * @return
     */
    private Bitmap getBlurMosaic() {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return null;
        }

        if (bmBaseLayer == null) {
            return null;
        }
        Bitmap bitmap = BitmapUtil.blur(bmBaseLayer);
        return bitmap;
    }

    /**
     * 创建与原图大小一样的图片，用设置原图每个坐标的颜色块来填充，形成马赛克效果
     *
     * @return
     */
    private Bitmap getGridMosaic() {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // 根据马赛克色块大小计算填充空白画笔色块数量


        int horCount = (int) Math.ceil(mImageWidth / (float) mGridWidth);
        int verCount = (int) Math.ceil(mImageHeight / (float) mGridWidth);

        Paint paint = new Paint();
        paint.setAntiAlias(true);


        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int l = mGridWidth * horIndex;
                int t = mGridWidth * verIndex;
                int r = l + mGridWidth;
                if (r > mImageWidth) {
                    r = mImageWidth;
                }
                int b = t + mGridWidth;
                if (b > mImageHeight) {
                    b = mImageHeight;
                }
                int color = bmBaseLayer.getPixel(l, t);
                Rect rect = new Rect(l, t, r, b);
                paint.setColor(color);
                canvas.drawRect(rect, paint);
            }
        }
        canvas.save();
        return bitmap;
    }

    /**
     * 是否保存
     *
     * @return
     */
    public boolean isSaved() {
        return (bmCoverLayer == null);
    }

    public void setOutPath(String absPath) {
        this.outPath = absPath;
    }

    /**
     * 设置马赛克单元格大小
     *
     * @param width
     */
    public void setGridWidth(int width) {
        this.mGridWidth = dp2px(width);
    }

    public int getGridWidth() {
        return this.mGridWidth;
    }

    /**
     * 设置涂鸦颜色
     *
     * @param color
     */
    public void setMosaicColor(int color) {
        this.mMosaicColor = color;
    }

    /**
     * 设置矩形选择边框颜色
     *
     * @param color
     */
    public void setStrokeColor(int color) {
        this.mStrokeColor = color;
        mPaint.setColor(mStrokeColor);
    }

    public int getStrokeColor() {
        return this.mStrokeColor;
    }

    /**
     * 设置矩形选择边框宽度
     *
     * @param width
     */
    public void setStrokeWidth(int width) {
        this.mStrokeWidth = width;
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    public int getStrokeWidth() {
        return this.mStrokeWidth;
    }

    /**
     * 设置是否处于擦除模式
     *
     * @param erase
     */
    public void setErase(boolean erase) {
        this.mMosaic = !erase;
    }

    /**
     * 清空绘制路径或矩形，并清空涂鸦结果bmMosaicLayer
     */
    public void clear() {
        mTouchRects.clear();
        mEraseRects.clear();

        mTouchPaths.clear();
        pathSizeMap.clear();
        mErasePaths.clear();

        if (bmMosaicLayer != null) {
            bmMosaicLayer.recycle();
            bmMosaicLayer = null;
        }


        if (bmTouchLayer != null) {
            bmTouchLayer.recycle();
            bmTouchLayer = null;
        }


        // View重画，执行onDraw()方法，来重画界面
        invalidate();
    }

    /**
     * 设置或改变图片时重置所有数据
     *
     * @return
     */
    public boolean reset_when_change_img() {
        if (bmCoverLayer != null) {
            bmCoverLayer.recycle();
            bmCoverLayer = null;
        }
        if (bmBaseLayer != null) {
            bmBaseLayer.recycle();
            bmBaseLayer = null;
        }
        if (bmMosaicLayer != null) {
            bmMosaicLayer.recycle();
            bmMosaicLayer = null;
        }
        if (bmTouchLayer != null) {
            bmTouchLayer.recycle();
            bmTouchLayer = null;
        }

        mTouchRects.clear();
        mEraseRects.clear();

        mTouchPaths.clear();
        pathSizeMap.clear();
        mErasePaths.clear();
        return true;
    }

    /**
     * 保存图片，此方法没有新开线程
     *
     * @return
     */
    public String save() {
        if (bmMosaicLayer == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bmBaseLayer, 0, 0, null);
        canvas.drawBitmap(bmMosaicLayer, 0, 0, null);
        canvas.save();
        String path = AACommon.getUseFilesPath() + File.separator
                + AADate.getDateStrName() + ".jpg";
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "failed to write image content");
            return null;
        }


        //现在的Bug  保存图片不会清空马赛克图层
//        if (bmTouchLayer != null) {
//            bmTouchLayer.recycle();
//            bmTouchLayer = null;
//        }

//        if (bmMosaicLayer != null) {
//            bmMosaicLayer.recycle();
//            bmMosaicLayer = null;
//        }
        return path;
    }

    // 重写触摸事件
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        if (isZoom) {
            return true;
        }
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.d(TAG, "action " + action + " x " + x + " y " + y);
        if (mMode == Mode.GRID) {
            onGridEvent(action, x, y);
        } else if (mMode == Mode.PATH) {
            onPathEvent(action, x, y);
        }
        return true;
    }

    /**
     * 矩形选择涂鸦
     *
     * @param action
     * @param x
     * @param y
     */
    private void onGridEvent(int action, int x, int y) {
        // 创建选择的矩形区域
        if (x >= mImageRect.left && x <= mImageRect.right
                && y >= mImageRect.top && y <= mImageRect.bottom) {
            int left = x;
            int right = x;
            int top = y;
            int bottom = y;
            if (startPoint == null) {
                startPoint = new Point();
                startPoint.set(x, y);
                mTouchRect = new Rect();
            } else {
                left = startPoint.x < x ? startPoint.x : x;
                top = startPoint.y < y ? startPoint.y : y;
                right = x > startPoint.x ? x : startPoint.x;
                bottom = y > startPoint.y ? y : startPoint.y;
            }
            mTouchRect.set(left, top, right, bottom);
        }
        // 触摸抬起时保存选择的矩形
        if (action == MotionEvent.ACTION_UP) {
            if (mMosaic) {
                mTouchRects.add(mTouchRect);
            } else {
                mEraseRects.add(mTouchRect);
            }
            // 抬起时，设置矩形选择区域为空，重绘视图时，不再根据mTouchRect绘制选择效果
            mTouchRect = null;
            startPoint = null;
            updateGridMosaic();
        }

        // View重画，执行onDraw()方法，来重画界面
        invalidate();
    }

    /**
     * 手势轨迹涂鸦
     *
     * @param action
     * @param x
     * @param y
     */
    private void onPathEvent(int action, int x, int y) {


        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        if (x < mImageRect.left || x > mImageRect.right || y < mImageRect.top
                || y > mImageRect.bottom) {
            return;
        }
        // 根据mImageRect矩形(屏幕显示图片的形状)实际触摸的位置，计算图片被触摸的坐标
        float ratio = (mImageRect.right - mImageRect.left)
                / (float) mImageWidth;

        // 获取图片中x坐标，以整个图片为坐标系，以保证在原有图片上绘制(而非缩略图，以原图为Canvas处理图片)
        x = (int) ((x - mImageRect.left) / ratio);
        // 获取图片中y坐标，以整个图片为坐标系，以保证在原有图片上绘制(而非缩略图，以原图为Canvas处理图片)
        y = (int) ((y - mImageRect.top) / ratio);
        //这就是对于图片上的x,y轴。

        if (onMotionEventListener != null) {
            onMotionEventListener.onEventListener(x, y);
        }


        if (action == MotionEvent.ACTION_DOWN) {
            mTouchPath = new Path();
            mTouchPath.moveTo(x, y);
            if (mMosaic) {
                mTouchPaths.add(mTouchPath);
                pathSizeMap.put(mTouchPaths.size() - 1, mPathWidth);
            } else {
                mErasePaths.add(mTouchPath);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            // ACTION_DOWN后移动产生线条，再进行绘制
            if (mTouchPath != null) {//解决崩溃bug

                mTouchPath.lineTo(x, y);
                updatePathMosaic();

                // View重画，执行onDraw()方法，来重画界面
                invalidate();

            }
        }
    }


    /**
     * 绘制涂鸦路径
     */
    private void updatePathMosaic() {

        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        long time = System.currentTimeMillis();
//        if (bmMosaicLayer != null) {
//            bmMosaicLayer.recycle();
//        }

        if (bmMosaicLayer == null) {
            bmMosaicLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
                    Config.ARGB_8888);
        }

        if (bmTouchLayer == null) {
            bmTouchLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
                    Config.ARGB_8888);
        }


//        bmMosaicLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
//                Config.ARGB_8888);
//
//        Bitmap bmTouchLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
//                Config.ARGB_8888);


        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(10));
        paint.setStrokeWidth(mPathWidth);


        // 设置涂鸦时轨迹的颜色，这个颜色只做标记，实际显示的是这个轨迹与bmCoverLayer重叠的bmCoverLayer的部分
        paint.setColor(Color.GREEN);// 可以是任意颜色，效果一样
        Canvas canvas = new Canvas(bmTouchLayer);
        // 绘制触摸移动的路径
        for (int i = 0; i < mTouchPaths.size(); i++) {
            Path path = mTouchPaths.get(i);
            try {
                if (pathSizeMap != null) {
                    paint.setStrokeWidth(pathSizeMap.get(i));
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            canvas.drawPath(path, paint);
        }
        // 设置为透明画笔把涂鸦的擦除掉，当mMosaic为false时擦除涂鸦部分产生mErasePaths
        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        for (Path path : mErasePaths) {
            canvas.drawPath(path, paint);
        }
        // 设置画布为涂鸦结果的空bitmap,下面再这上面再添加背景bmCoverLayer，和取与bmTouchLayer相交的部分
        canvas.setBitmap(bmMosaicLayer);
        canvas.drawARGB(0, 0, 0, 0);
        // 首先将效果的bitmap放在画布上
        canvas.drawBitmap(bmCoverLayer, 0, 0, null);

        paint.reset();
        paint.setAntiAlias(true);
        // DST_IN取两层绘制交集。显示下层,对应的SRC_IN显示上层(即涂鸦的轨迹)
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        // 取两层绘制交集,显示下层,即显示bmTouchLayer中涂鸦的部分(其他部分透明，涂鸦部分与bmCoverLayer的交集)
        canvas.drawBitmap(bmTouchLayer, 0, 0, paint);
        paint.setXfermode(null);
        canvas.save();
        // 上面已经产生涂鸦结果bmCoverLayer，这里回收不用的bitmap
//        bmTouchLayer.recycle();
        Log.d(TAG, "updatePathMosaic " + (System.currentTimeMillis() - time));
    }

    /**
     * 涂鸦矩形选择区域
     */
    private void updateGridMosaic() {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        long time = System.currentTimeMillis();
        if (bmMosaicLayer != null) {
            bmMosaicLayer.recycle();
        }
        bmMosaicLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Config.ARGB_8888);
        //
        // 计算mImageRect和原图的缩放率，来在原图上绘制实际大小的矩形
        float ratio = (mImageRect.right - mImageRect.left)
                / (float) mImageWidth;


        Bitmap bmTouchLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Config.ARGB_8888);

        Canvas canvas = null;
        canvas = new Canvas(bmTouchLayer);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mStrokeColor);
        // 涂鸦矩形区域,根据缩放率绘制在原图上实际位置的矩形
        for (Rect rect : mTouchRects) {
            int left = (int) ((rect.left - mImageRect.left) / ratio);
            int right = (int) ((rect.right - mImageRect.left) / ratio);
            int top = (int) ((rect.top - mImageRect.top) / ratio);
            int bottom = (int) ((rect.bottom - mImageRect.top) / ratio);
            canvas.drawRect(left, top, right, bottom, paint);
        }

        paint.setColor(Color.TRANSPARENT);
        // 清除所选矩形区域像素，即把选择区域变为透明CLEAR，mEraseRects为擦除模式产生的操作
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        for (Rect rect : mEraseRects) {
            int left = (int) ((rect.left - mImageRect.left) / ratio);
            int right = (int) ((rect.right - mImageRect.left) / ratio);
            int top = (int) ((rect.top - mImageRect.top) / ratio);
            int bottom = (int) ((rect.bottom - mImageRect.top) / ratio);
            canvas.drawRect(left, top, right, bottom, paint);
        }

        canvas.setBitmap(bmMosaicLayer);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawBitmap(bmCoverLayer, 0, 0, null);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(bmTouchLayer, 0, 0, paint);
        paint.setXfermode(null);
        canvas.save();

        bmTouchLayer.recycle();
        Log.d(TAG, "updateGridMosaic " + (System.currentTimeMillis() - time));
    }

    // 重绘视图，调用invalidate()
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw canvas " + canvas + " mTouchRect " + mTouchRect);
        // 将原图绘制到视图上
        if (bmBaseLayer != null) {
            canvas.drawBitmap(bmBaseLayer, null, mImageRect, null);
        }
        // 将涂鸦结果绘制到视图上
        if (bmMosaicLayer != null) {
            canvas.drawBitmap(bmMosaicLayer, null, mImageRect, null);
        }
        // 讲矩形选择效果绘制到视图上，矩形选择松开后，mTouchRect设为null,这时重画界面，选择效果消失
        if (mTouchRect != null) {
            canvas.drawRect(mTouchRect, mPaint);
        }
    }

    private float leoRio;

    // 重新布局，调用requestLayout()
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        int contentWidth = right - left;
        int contentHeight = bottom - top;
        int viewWidth = contentWidth - mPadding * 2;
        int viewHeight = contentHeight - mPadding * 2;
        // 图片缩放比例
        float widthRatio = viewWidth / ((float) mImageWidth);
        float heightRatio = viewHeight / ((float) mImageHeight);
        // 使用宽高中最小缩放比例
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        leoRio = ratio;
        // 计算出图片宽高
        int realWidth = (int) (mImageWidth * ratio);
        int realHeight = (int) (mImageHeight * ratio);
        // 计算出图片位置
        int imageLeft = (contentWidth - realWidth) / 2;
        int imageTop = (contentHeight - realHeight) / 2;
        int imageRight = imageLeft + realWidth;
        int imageBottom = imageTop + realHeight;
        // 设置图片的位置，用矩形对象mImageRect表示
        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
        mImageRect_base.set(mImageRect);

        if (mGridWidth == 0) {//这里因为bmCoverLayer = getCoverLayer(); 获取马赛克绘制层。所以要先按比例马赛克大小。
            // 然后再获取。由于会一直设置画笔粗细。所以先初始化马赛克大小为0，判断如果是0时候则初始化
            mGridWidth = dp2px(GRID_WIDTH);
            mGridWidth = (int) (mGridWidth / leoRio);
        }

        if (bmCoverLayer==null){
            bmCoverLayer = getCoverLayer();
        }

        if (mPathWidth == 0) {
            mPathWidth = (int) (40 / leoRio);
        }
    }

    /**
     * 将自己设置的dip参数转换为px
     *
     * @param dip
     * @return
     */
    private int dp2px(int dip) {
        Context context = this.getContext();
        Resources resources = context.getResources();
        int px = Math
                .round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dip, resources.getDisplayMetrics()));
        return px;
    }
}
