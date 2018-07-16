package cn.readsense.camera2;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Size;

import java.util.Arrays;
import java.util.List;

@RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Util {


    public static boolean hasFacing(Context context, int camera_facing) {
        Activity activity = (Activity) context;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            if (manager != null) {
                for (String cameraId : manager.getCameraIdList()) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == camera_facing) {
                        return true;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasPreviewSize(Context context, int width, int height, int camera_facing) {
        Activity activity = (Activity) context;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            if (manager != null) {
                for (String cameraId : manager.getCameraIdList()) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == camera_facing) {
                        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                        if (map == null) {
                            throw new Error("Camera StreamConfigurationMap is null, no prewsize");
                        }
                        final List<Size> previewSizes = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));

                        for (Size previewSize : previewSizes) {
                            if (previewSize.getWidth() == width && previewSize.getHeight() == height)
                                return true;
                        }
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
