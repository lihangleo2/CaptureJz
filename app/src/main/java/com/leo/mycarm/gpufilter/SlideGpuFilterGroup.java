package com.leo.mycarm.gpufilter;

import android.opengl.GLES20;
import android.util.Log;
import android.widget.Scroller;

import com.leo.mycarm.MyApplication;
import com.leo.mycarm.gpufilter.basefilter.GPUImageFilter;
import com.leo.mycarm.gpufilter.helper.MagicFilterFactory;
import com.leo.mycarm.gpufilter.helper.MagicFilterType;
import com.leo.mycarm.utils.EasyGlUtils;



/**
 * Created by cj on 2017/7/20 0020.
 * 滑动切换滤镜的控制类
 */

public class SlideGpuFilterGroup {
    private MagicFilterType[] types = new MagicFilterType[]{
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
            MagicFilterType.TENDER
    };
    private GPUImageFilter curFilter;
    private GPUImageFilter nextFilter_1;
    private GPUImageFilter nextFilter_2;
    private GPUImageFilter nextFilter_3;
    private GPUImageFilter nextFilter_4;
    private GPUImageFilter nextFilter_5;
    private GPUImageFilter nextFilter_6;
    private GPUImageFilter nextFilter_7;
    private GPUImageFilter nextFilter_8;
    private GPUImageFilter nextFilter_9;
    private GPUImageFilter nextFilter_10;
    private GPUImageFilter nextFilter_11;
    private GPUImageFilter nextFilter_12;
    private GPUImageFilter nextFilter_13;
    private GPUImageFilter nextFilter_14;
    private GPUImageFilter nextFilter_15;
    private GPUImageFilter nextFilter_16;

    private int width, height;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    private int curIndex = 0;
    private Scroller scroller;
    private OnFilterChangeListener mListener;


    public void setCurIndex(MagicFilterType filterType) {

        switch (filterType) {
            case NONE:
                curFilter = nextFilter_1;
                break;

            case SUNRISE:
                curFilter = nextFilter_2;
                break;
            case SUNSET:
                curFilter = nextFilter_3;
                break;

            case WHITECAT:
                curFilter = nextFilter_4;
                break;

            case BLACKCAT:
                curFilter = nextFilter_5;
                break;
            case SKINWHITEN:
                curFilter = nextFilter_6;
                break;
            case HEALTHY:
                curFilter = nextFilter_7;
                break;
            case SWEETS:
                curFilter = nextFilter_8;
                break;
            case ROMANCE:
                curFilter = nextFilter_9;
                break;
            case SAKURA:
                curFilter = nextFilter_10;
                break;
            case WARM:
                curFilter = nextFilter_11;
                break;
            case ANTIQUE:
                curFilter = nextFilter_12;
                break;
            case NOSTALGIA:
                curFilter = nextFilter_13;
                break;
            case CALM:
                curFilter = nextFilter_14;
                break;
            case LATTE:
                curFilter = nextFilter_15;
                break;
            case TENDER:
                curFilter = nextFilter_16;
                break;


        }
    }

    public SlideGpuFilterGroup() {
        initFilter();
        scroller = new Scroller(MyApplication.getContext());
        Log.e("8月六日滤镜", "======SlideGpuFilterGroup");

    }

    private void initFilter() {
        curFilter = getFilter(getCurIndex());
        nextFilter_1 = getFilter(0);
        nextFilter_2 = getFilter(1);
        nextFilter_3 = getFilter(2);
        nextFilter_4 = getFilter(3);
        nextFilter_5 = getFilter(4);
        nextFilter_6 = getFilter(5);
        nextFilter_7 = getFilter(6);
        nextFilter_8 = getFilter(7);
        nextFilter_9 = getFilter(8);
        nextFilter_10 = getFilter(9);
        nextFilter_11 = getFilter(10);
        nextFilter_12 = getFilter(11);
        nextFilter_13 = getFilter(12);
        nextFilter_14 = getFilter(13);
        nextFilter_15 = getFilter(14);
        nextFilter_16 = getFilter(15);
        Log.e("8月六日滤镜", "======initFilter");

    }

    private GPUImageFilter getFilter(int index) {
        GPUImageFilter filter = MagicFilterFactory.initFilters(types[index]);
        if (filter == null) {
            filter = new GPUImageFilter();
        }
        Log.e("8月六日滤镜", "======getFilter");
        return filter;
    }

    public void init() {
        curFilter.init();
        nextFilter_1.init();
        nextFilter_2.init();
        nextFilter_3.init();
        nextFilter_4.init();
        nextFilter_5.init();
        nextFilter_6.init();
        nextFilter_7.init();
        nextFilter_8.init();
        nextFilter_9.init();
        nextFilter_10.init();
        nextFilter_11.init();
        nextFilter_12.init();
        nextFilter_13.init();
        nextFilter_14.init();
        nextFilter_15.init();
        nextFilter_16.init();
        Log.e("8月六日滤镜", "======init");
    }

    public void onSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glGenFramebuffers(1, fFrame, 0);
        EasyGlUtils.genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height);
        onFilterSizeChanged(width, height);
        Log.e("8月六日滤镜", "======onSizeChanged");

    }

    private void onFilterSizeChanged(int width, int height) {
        curFilter.onInputSizeChanged(width, height);
        curFilter.onDisplaySizeChanged(width, height);
        Log.e("8月六日滤镜", "======onFilterSizeChanged");


    }

    public int getOutputTexture() {
        Log.e("8月六日滤镜", "======getOutputTexture");
        return fTexture[0];
    }

    public void onDrawFrame(int textureId) {
        EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[0]);

//        if (isChange) {
        curFilter.onDrawFrame(textureId);
//            isChange = false;
//        }

        EasyGlUtils.unBindFrameBuffer();
        Log.e("8月六日滤镜", "======onDrawFrame");

    }


    private int getCurIndex() {
        return curIndex;
    }


    public void setOnFilterChangeListener(OnFilterChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnFilterChangeListener {
        void onFilterChange(MagicFilterType type);
    }
}
