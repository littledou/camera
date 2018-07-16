package cn.readsense.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by dou on 2017/11/6.
 */

public class PreviewView extends SurfaceView implements SurfaceHolder.Callback {


    private SurfaceHolder mSurfaceHolder;
    CameraInterface cameraInterface;

    public PreviewView(Context context, CameraInterface cameraInterface) {
        super(context);
        mSurfaceHolder = getHolder();
        this.cameraInterface = cameraInterface;
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cameraInterface.setPreviewDisplay(mSurfaceHolder);
        cameraInterface.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraInterface.stopPreview();
        cameraInterface.releaseCamera();
    }
}
