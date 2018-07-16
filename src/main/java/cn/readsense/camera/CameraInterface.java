package cn.readsense.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by dou on 2017/11/6.
 */

public class CameraInterface {

    private static final String TAG = "CameraInterface";

    private Camera camera = null;
    private Camera.Parameters parameters;
    private int preview_width, preview_height;
    private int facing;

    private boolean isWithBufferCallback = false;//是否使用了带缓冲区的回调
    private boolean isWithCallback = false;//是否使用了带缓冲区的回调
    boolean isPreviewing;

    public CameraInterface() {
    }

    /**
     * Camera 连接打开
     */
    public void openCamera(int cameraFacing) {
        facing = cameraFacing;
        if (camera != null) {
            stopPreview();
            releaseCamera();
        }
        camera = Camera.open(cameraFacing);
        parameters = camera.getParameters();
    }

    public void setParamFocusMode(String mode) {
        if (isSupportFocusMode(mode))
            parameters.setFocusMode(mode);
    }

    public void setParamPreviewSize(int width, int height) {
        preview_width = width;
        preview_height = height;
        parameters.setPreviewSize(width, height);
    }

    public void setParamEnd() {
        if (camera != null && parameters != null)
            camera.setParameters(parameters);
    }

    public void setDisplayOrientation(Context context, int result) {
        if (result % 90 != 0)
            result = getCameraDisplayOrientation(context, facing);
        if (camera != null)
            camera.setDisplayOrientation(result);
    }

    public void setDisplayOrientation(Context context) {
        setDisplayOrientation(context, -1);
    }

    public void setPreviewDisplay(SurfaceHolder holder) {
        try {
            if (camera != null)
                camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPreviewCallback(Camera.PreviewCallback callback) {
        isWithCallback = true;
        camera.setPreviewCallback(callback);
    }

    public void removePreviewCallback() {
        isWithCallback = false;
        camera.setPreviewCallback(null);
    }

    public void addPreviewCallbackWithBuffer(Camera.PreviewCallback callback) {
        isWithBufferCallback = true;
        camera.addCallbackBuffer(new byte[preview_width * preview_height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
        camera.setPreviewCallbackWithBuffer(callback);
    }

    public void removePreviewCallbackWithBuffer() {
        if(camera!=null) {
            isWithBufferCallback = false;
            camera.setPreviewCallbackWithBuffer(null);
        }
    }


    public void startPreview() {
        if (camera != null) {
            camera.startPreview();
            isPreviewing = true;
        }
    }

    public void stopPreview() {
        if (camera != null) {
            if (isWithBufferCallback)
                removePreviewCallbackWithBuffer();
            if (isWithCallback)
                removePreviewCallback();
            camera.stopPreview();
            isPreviewing = false;
        }
    }


    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }


    public void tackPicture(Camera.PictureCallback mJpegPictureCallback) {
        if (isPreviewing && (camera != null)) {
            camera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    private int getCameraDisplayOrientation(Context context, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        short degrees = 0;
        switch (rotation) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 270;
        }

        int result;
        if (info.facing == 1) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    boolean hasCameraDevice(Context ctx) {
        return ctx.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    boolean hasCameraFacing(int facing) {
        int number_of_camera = Camera.getNumberOfCameras();
        for (int i = 0; i < number_of_camera; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == facing)
                return true;
        }
        return false;
    }

    boolean hasSupportSize(int width, int height) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for (int i = 0; i < previewSizes.size(); i++) {
            Camera.Size size = previewSizes.get(i);
            Log.i(TAG, "previewSizes:width = " + size.width + " height = " + size.height);
            if (size.width == width && size.height == height) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupportFocusMode(String mode) {
        List<String> modes = parameters.getSupportedFocusModes();
        return modes.contains(mode);
    }

    public void printSupportPreviewSize() {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for (int i = 0; i < previewSizes.size(); i++) {
            Camera.Size size = previewSizes.get(i);
            Log.i(TAG, "previewSizes:width = " + size.width + " height = " + size.height);
        }

    }

    public void printSupportPictureSize() {
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Camera.Size size = pictureSizes.get(i);
            Log.i(TAG, "pictureSizes:width = " + size.width
                    + " height = " + size.height);
        }
    }

    public void printSupportFocusMode() {
        List<String> focusModes = parameters.getSupportedFocusModes();
        for (String mode : focusModes) {
            Log.i(TAG, "focusModes--" + mode);
        }
    }

}
