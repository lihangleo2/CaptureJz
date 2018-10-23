package com.leo.mycarm.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.mycarm.R;
import com.leo.mycarm.TimeUtils;
import com.leo.mycarm.adapter.FilterAdapter;
import com.leo.mycarm.gpufilter.helper.MagicFilterType;
import com.leo.mycarm.interpagek.OnMotionEventListener;
import com.leo.mycarm.utils.ToastUtils;
import com.leo.mycarm.utils.UIUtil;
import com.leo.mycarm.widget.LoadingDialog;
import com.leo.mycarm.widget.mosaicview.MosaicView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;
import jp.co.cyberagent.android.gpuimage.GPUImageView;


/**
 * Created by Leo on 2018/8/7.
 */

public class PicEditorActivity extends AppCompatActivity implements OnMotionEventListener, FilterAdapter.onFilterChangeListener
        , GPUImageView.OnPictureSavedListener, View.OnClickListener {
    private final int upload_type = 99;
    private final int picface_type = 100;
    private String strUrl;
    @BindView(R.id.bar_btn_left)
    RelativeLayout bar_btn_left;
    @BindView(R.id.image_clear)
    ImageView image_clear;


    @BindView(R.id.relative_bottom)
    RelativeLayout relative_bottom;//底部的区域
    @BindView(R.id.image_mask_1)
    ImageView image_mask_1;
    @BindView(R.id.relative_mask_1)
    RelativeLayout relative_mask_1;//解决小圆点点击不到
    @BindView(R.id.image_mask_2)
    ImageView image_mask_2;
    @BindView(R.id.image_mask_3)
    ImageView image_mask_3;
    @BindView(R.id.image_mask_4)
    ImageView image_mask_4;
    @BindView(R.id.image_mask_5)
    ImageView image_mask_5;
    private ArrayList<ImageView> imags = new ArrayList<>();


    private String path;//图片路径
    private String filePath;


    @BindView(R.id.mosaicview)
    MosaicView mosaicview;
    //GPUIMage
    @BindView(R.id.gpuImageView)
    GPUImageView gpuImageView;

    @BindView(R.id.relative_lvjing)
    RelativeLayout relative_lvjing;//滤镜
    @BindView(R.id.text_lvjing)
    TextView text_lvjing;
    @BindView(R.id.text_mask)
    TextView text_mask;

    //滤镜列表
    @BindView(R.id.filter_listView)
    RecyclerView filter_listView;
    private FilterAdapter mAdapter;

    private LoadingDialog loadingDialog;
    @BindView(R.id.bar_txt_right)
    TextView bar_txt_right;
    private int isFromSwitch = -1;
    private Unbinder mUnbinder;

    private RxPermissions rxPermissions;


    private final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.SUNRISE,
            MagicFilterType.SUNSET,
//            MagicFilterType.WHITECAT,
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piceditor);
        mUnbinder = ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);
        initListener();

        loadingDialog = new LoadingDialog(PicEditorActivity.this, "图片处理中..", false);
        RelativeLayout.LayoutParams cardreParams = (RelativeLayout.LayoutParams) relative_bottom.getLayoutParams();
        cardreParams.height = (UIUtil.getHeight() - UIUtil.dip2px(PicEditorActivity.this, 104)) * 7 / 24;
        imags.add(image_mask_1);
        imags.add(image_mask_2);
        imags.add(image_mask_3);
        imags.add(image_mask_4);
        imags.add(image_mask_5);

        path = getIntent().getStringExtra("path");
        mosaicview.setOnMotionEventListener(this);
        mosaicview.setSrcPath(path);
        image_mask_3.setSelected(true);
        gpuImageView.setImage(new File(path));
        text_lvjing.setSelected(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        filter_listView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterAdapter(this, types);
        filter_listView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    public void initListener() {
        image_clear.setOnClickListener(this);
        relative_mask_1.setOnClickListener(this);
        image_mask_2.setOnClickListener(this);
        image_mask_3.setOnClickListener(this);
        image_mask_4.setOnClickListener(this);
        image_mask_5.setOnClickListener(this);
        bar_btn_left.setOnClickListener(this);
        text_mask.setOnClickListener(this);
        text_lvjing.setOnClickListener(this);
        bar_txt_right.setOnClickListener(this);

    }


    /**
     * 小米，功能正常
     *
     * @param activity
     */
    public static void Xiaomi(Activity activity) {
        try { // MIUI 8 9
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", activity.getPackageName());
            activity.startActivityForResult(localIntent, 99);
//            activity.startActivity(localIntent);
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", activity.getPackageName());
                activity.startActivityForResult(localIntent, 99);
//                activity.startActivity(localIntent);
            } catch (Exception e1) { // 否则跳转到应用详情
                openAppDetailSetting(activity);
//                activity.startActivityForResult(getAppDetailSettingIntent(activity), PERMISSION_SETTING_FOR_RESULT);
                //这里有个问题，进入活动后需要再跳一级活动，就检测不到返回结果
//                activity.startActivity(getAppDetailSettingIntent());
            }
        }
    }


    /**
     * 获取应用详情页面
     *
     * @return
     */
    private static Intent getAppDetailSettingIntent(Activity activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        return localIntent;
    }

    public static void openAppDetailSetting(Activity activity) {
        activity.startActivityForResult(getAppDetailSettingIntent(activity), 99);
    }


    public void saveToImage() {
        if (gpuImageView.getVisibility() == View.VISIBLE) {
            if (filterType == MagicFilterType.NONE) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                //保存图片到本地
                saveImageToGallery(PicEditorActivity.this, bitmap);

            } else {

                if (UIUtil.getDeviceBrand().contains("Xiaomi")) {//小米手机 要打开系统设置
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.System.canWrite(this)) {
                            isFromSwitch = 2;
                            saveImage();
                        } else {
                            Toast.makeText(PicEditorActivity.this, "请打开修改系统设置权限~", Toast.LENGTH_LONG).show();
                            Xiaomi(PicEditorActivity.this);
                        }
                    }

                } else {
                    isFromSwitch = 2;
                    saveImage();
                }

            }
        } else {
            if (!TextUtils.isEmpty(mosaicview.save())) {
                filePath = mosaicview.save();
            } else {
                filePath = path;
            }
            //保存图片到本地
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            saveImageToGallery(PicEditorActivity.this, bitmap);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bar_txt_right:
                if (ActivityCompat.checkSelfPermission(PicEditorActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(PicEditorActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        ) {
                    rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {

                            if (aBoolean) {
                                saveToImage();
                            } else {
                                Toast.makeText(PicEditorActivity.this, "请打开相关权限保持正常使用！", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                } else {
                    saveToImage();
                }

                break;
            case R.id.relative_mask_1:
                selectWhat(0);
                mosaicview.setmPathWidth(20);
                break;
            case R.id.image_mask_2:
                selectWhat(1);
                mosaicview.setmPathWidth(30);
                break;
            case R.id.image_mask_3:
                selectWhat(2);
                mosaicview.setmPathWidth(40);

                break;
            case R.id.image_mask_4:
                selectWhat(3);
                mosaicview.setmPathWidth(50);
                break;
            case R.id.image_mask_5:
                selectWhat(4);
                mosaicview.setmPathWidth(70);
                break;
            case R.id.image_clear:
                mosaicview.clear();
                break;
            case R.id.bar_btn_left:
                finish();
                break;

            case R.id.text_lvjing:
                //滤镜的点击
                text_lvjing.setSelected(true);
                text_mask.setSelected(false);
                relative_lvjing.setVisibility(View.VISIBLE);
                gpuImageView.setVisibility(View.VISIBLE);
                mosaicview.setVisibility(View.GONE);
                break;
            case R.id.text_mask:
                //马赛克点击

                if (UIUtil.getDeviceBrand().contains("Xiaomi")) {//小米手机 要打开系统设置
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.System.canWrite(this)) {
                            isFromSwitch = 1;
                            saveImage();
                        } else {
                            Toast.makeText(PicEditorActivity.this, "请打开修改系统设置权限~", Toast.LENGTH_LONG).show();
                            Xiaomi(PicEditorActivity.this);
                        }
                    }

                } else {
                    isFromSwitch = 1;
                    saveImage();
                }


                break;
        }
    }


    private void saveImage() {
        loadingDialog.show();
        String fileName = TimeUtils.getDateToStringLeo(System.currentTimeMillis() + "") + "_atmangpu.jpg";

//        String fileName = getFilesDir().getAbsolutePath().toString() + "/" + TimeUtils.getDateToStringLeo(System.currentTimeMillis() + "") + "_atmangpu.jpg";
        gpuImageView.saveToPictures("GPUImage", fileName, this);
    }

    public void selectWhat(int index) {

        for (int i = 0; i < imags.size(); i++) {
            if (index == i) {
                imags.get(i).setSelected(true);
            } else {
                imags.get(i).setSelected(false);
            }
        }

    }

    @Override
    public void onEventListener(int x, int y) {

    }

    private MagicFilterType filterType = MagicFilterType.NONE;

    @Override
    public void onFilterChanged(MagicFilterType filterType) {
        this.filterType = filterType;
        switch (filterType) {
            case WHITECAT:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicWhiteCatFilter());
                break;
            case BLACKCAT:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicBlackCatFilter());
                break;
            case SKINWHITEN:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicSkinWhitenFilter());
                break;
            case ROMANCE:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicRomanceFilter());
                break;
            case SAKURA:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicSakuraFilter());
                break;

            case ANTIQUE:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicAntiqueFilter());
                break;
            case CALM:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicCalmFilter());
                break;

            case HEALTHY:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicHealthyFilter());
                break;
            case LATTE:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicLatteFilter());
                break;
            case WARM:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicWarmFilter());
                break;
            case TENDER:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicTenderFilter());
                break;
            case SWEETS:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicSweetsFilter());
                break;
            case NOSTALGIA:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicNostalgiaFilter());
                break;

            case SUNRISE:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicSunriseFilter());
                break;
            case SUNSET:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.MagicSunsetFilter());
                break;
            default:
                gpuImageView.setFilter(new com.leo.mycarm.picfilter.NoneFilter());
                break;

        }
    }

    @Override
    public void onPictureSaved(Uri uri) {
        loadingDialog.dismiss();
        if (isFromSwitch == 1) {
            filePath = getRealFilePath(PicEditorActivity.this, uri);
            mosaicview.setSrcPath(getRealFilePath(PicEditorActivity.this, uri));
            text_lvjing.setSelected(false);
            text_mask.setSelected(true);
            relative_lvjing.setVisibility(View.GONE);
            gpuImageView.setVisibility(View.GONE);
            mosaicview.setVisibility(View.VISIBLE);
        } else {
            filePath = getRealFilePath(PicEditorActivity.this, uri);
            //保存图片到本地
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            saveImageToGallery(PicEditorActivity.this, bitmap);
        }
    }


    //通过uri获取图片路径
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    /**
     * 保存到系统相册
     *
     * @param context
     * @param bmp
     */
    public void saveImageToGallery(final Context context, final Bitmap bmp) {
        // TODO: 2017/2/20 android6.0权限申请https://github.com/anthonycr/Grant

        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "atman");
        if (!appDir.exists()) {
            appDir.mkdir();
        }


        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            ToastUtils.showToast("保存成功");
            finish();
        } catch (FileNotFoundException e) {
            ToastUtils.showToast("保存失败");
            e.printStackTrace();
        }

        // 最后通知图库更新
//        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }


}
