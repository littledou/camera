package cn.readsense.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.SurfaceView;

public class DrawUtil {

    public static void drawRect(SurfaceView outputView, float[] rect, Paint paint) {

        Canvas canvas = outputView.getHolder().lockCanvas();
        if (canvas == null) return;
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        if (rect != null)
            try {
                canvas.drawRect(rect[0], rect[1], rect[2], rect[3], paint);
            } catch (Exception e) {
                e.printStackTrace();
            }

        outputView.getHolder().unlockCanvasAndPost(canvas);
    }
}
