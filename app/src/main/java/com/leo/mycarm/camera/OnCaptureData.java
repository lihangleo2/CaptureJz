package com.leo.mycarm.camera;


public interface OnCaptureData {
    public void onCapture(boolean success, byte[] data);
}