package cn.readsense.camera;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.readsense.permissions.PermissionListener;
import cn.readsense.permissions.PermissionsUtil;


/**
 * Created by dou on 2017/11/7.
 */

public class CameraView extends RelativeLayout {

    private static final String TAG = "CameraView";
    private Context context;

    private int PREVIEWWIDTH;
    private int PREVIEWHEIGHT;
    private int FACING;

    private final static String permission_camera = Manifest.permission.CAMERA;

    private byte buffer[];
    private byte temp[];
    private boolean isBufferready = false;
    private final Object Lock = new Object();
    private PreviewFrameCallback previewFrameCallback;
    CameraInterface cameraInterface;

    HandlerThread myHandlerThread;
    Handler handler;
    Handler handler_main;

    private SurfaceView draw_view;

    private Paint paint;

    public SurfaceView getDrawView() {
        return draw_view;
    }

    public void setDrawView() {
        draw_view = new SurfaceView(context);
        draw_view.setZOrderOnTop(true);
        draw_view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setBackgroundColor(Color.BLACK);
        cameraInterface = new CameraInterface();
    }


    public void showCameraView(int width, int height, int facing) {

        if (!cameraInterface.hasCameraDevice(context))//无摄像头设备
            throw new Error("device not found any camera device!");

//        if (!cameraInterface.hasCameraFacing(facing))//未找到指定方向摄像头
//            throw new Error(String.format(Locale.CHINA, "device not found camera device, CamreaId: %d!", facing));


        PREVIEWWIDTH = width;
        PREVIEWHEIGHT = height;
        FACING = facing;

        if (PermissionsUtil.hasPermission(context, permission_camera,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            setUpCamera();

        } else {
            PermissionsUtil.requestPermission(context, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permission) {
                    setUpCamera();
                }

                @Override
                public void permissionDenied(@NonNull String[] permission) {
                    Toast.makeText(context, "用户拒绝了访问摄像头", Toast.LENGTH_LONG).show();
                }
            }, permission_camera,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

    }

    private void setUpCamera() {
        cameraInterface.openCamera(FACING);

        if (!cameraInterface.hasSupportSize(PREVIEWWIDTH, PREVIEWHEIGHT))
            throw new Error("device camera " + FACING + " not found size " + PREVIEWWIDTH + "*" + PREVIEWHEIGHT);

        cameraInterface.setParamPreviewSize(PREVIEWWIDTH, PREVIEWHEIGHT);
        cameraInterface.setDisplayOrientation(context);
        cameraInterface.setParamEnd();
        settingSurfaceView();
    }

    private void settingSurfaceView() {
        if (getChildCount() != 0) removeAllViews();

        if (previewFrameCallback != null) {
            buffer = new byte[PREVIEWWIDTH * PREVIEWHEIGHT * 2];
            temp = new byte[PREVIEWWIDTH * PREVIEWHEIGHT * 2];

            cameraInterface.addPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {//数据预览回掉
                    camera.addCallbackBuffer(data);

                    synchronized (Lock) {
                        System.arraycopy(data, 0, buffer, 0, data.length);
                        isBufferready = true;
                    }
                    handler.sendEmptyMessage(0);
                }
            });

            //run data analyse

            myHandlerThread = new HandlerThread("handler-thread");
            //开启一个线程
            myHandlerThread.start();
            //在这个线程中创建一个handler对象
            handler = new Handler(myHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //这个方法是运行在 handler-thread 线程中的 ，可以执行耗时操作

                    synchronized (Lock) {
                        System.arraycopy(buffer, 0, temp, 0, buffer.length);
                        isBufferready = false;
                    }
                    if (previewFrameCallback != null) {
                        Object o = previewFrameCallback.analyseData(temp);
                        Message msg1 = new Message();
                        msg1.what = 0;
                        msg1.obj = o;
                        handler_main.sendMessage(msg1);
                    }
                }
            };
            handler_main = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (previewFrameCallback != null)
                        previewFrameCallback.analyseDataEnd(msg.obj);
                }
            };
        }
        addView(new PreviewView(context, cameraInterface));
        if (draw_view != null) {
            addView(draw_view);
        }
    }

    public void releaseCamera() {
        previewFrameCallback = null;
        if (handler != null)
            handler.removeMessages(0);
        if (handler_main != null)
            handler_main.removeMessages(0);
        if (myHandlerThread != null)
            myHandlerThread.quitSafely();
    }

    private void removeDataCallback() {
        this.previewFrameCallback = null;
        cameraInterface.removePreviewCallbackWithBuffer();
    }

    public void addPreviewFrameCallback(PreviewFrameCallback callback) {
        this.previewFrameCallback = callback;
    }

    public interface PreviewFrameCallback {
        Object analyseData(byte[] data);

        void analyseDataEnd(Object t);
    }

    public int moveCameraFacing() {
        removeDataCallback();
        FACING = (FACING == Camera.CameraInfo.CAMERA_FACING_BACK) ?
                Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;

        cameraInterface.removePreviewCallbackWithBuffer();
        cameraInterface.stopPreview();
        cameraInterface.releaseCamera();
        removeAllViews();
        showCameraView(PREVIEWWIDTH, PREVIEWHEIGHT, FACING);
        return FACING;
    }


}
