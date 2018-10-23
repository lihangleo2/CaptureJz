package com.leo.mycarm.widget.mosaicview;

import android.os.Environment;

import java.io.File;

public class AACommon {
	// 流量统计使用的变量
	public static final String ZLL = "zll";
	public static final String G3LL = "g3ll";
	public static final String MYLL = "myll";
	public static String MyKey;
	public static String MySeed;
	public static String[] colorsStrings = new String[] { "#b46d3b", "#5ecddf",
			"#43a7eb", "#e9b043", "#44aff9", "#5bc3d3", "#66617b", "#42ac67",
			"#e86153", "#43b56b", "#bd713b", "#f56455", "#f6b944" };

	// 照片大小
	public static final int qualityNum = 70;
	public static final int photoWidth = 320;
	/**
	 * 拍照值为21
	 */
	public static final int TakePhoto = 21;
	/**
	 * 删除照片值为22
	 */
	public static final int DeletePhoto = 22;

	/**
	 * 根目录
	 * 
	 * @return
	 */
	public static String getRootPath() {
		String path = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + "PiaoYuAnfiles";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	/**
	 * 拍照存储路径
	 * 
	 * @return
	 */
	public static String getPhotoPath() {
		String path = getRootPath() + File.separator + "photo";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	/**
	 * 照片路径
	 * 
	 * @return
	 */
	public static String getPathPhoto(String photoName) {
		return getPhotoPath() + File.separator + photoName;
	}

	/**
	 * 照片本地上传头像路径
	 * 
	 * @return
	 */
	public static String getPathTxPhoto() {
		return getPhotoPath() + File.separator + "update_tx.jpg";
	}

	/**
	 * 照片1路径
	 * 
	 * @return
	 */
	public static String getPathPhoto1() {
		return getPhotoPath() + File.separator + "photo1.jpg";
	}

	/**
	 * 照片2路径
	 * 
	 * @return
	 */
	public static String getPathPhoto2() {
		return getPhotoPath() + File.separator + "photo2.jpg";
	}

	/**
	 * 照片3路径
	 * 
	 * @return
	 */
	public static String getPathPhoto3() {
		return getPhotoPath() + File.separator + "photo3.jpg";
	}

	/**
	 * 录音存储路径
	 * 
	 * @return
	 */
	public static String getVoicePath() {
		String path = getRootPath() + File.separator + "voice";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	/**
	 * 录音路径
	 * 
	 * @return
	 */
	public static String getPathVoice(int mark) {
		return getVoicePath() + File.separator + "voice" + mark + ".mp3";
	}

	/**
	 * 录音路径
	 * 
	 * @return
	 */
	public static String getPathVoice(String mark) {
		return getVoicePath() + File.separator + "voice" + mark + ".mp3";
	}

	/**
	 * apk下载路径
	 * 
	 * @return
	 */
	public static String getApkPath() {
		String path = getRootPath() + File.separator + "APK";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	/**
	 * 缓存文件路径
	 * 
	 * @return
	 */
	public static String getCacheFilesPath() {
		String path = getRootPath() + File.separator + "cachefiles";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	/**
	 * 缓存文件路径
	 * 
	 * @return
	 */
	public static String getUseFilesPath() {
		String path = getRootPath() + File.separator + "usefiles";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

}
