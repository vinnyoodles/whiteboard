package com.vinnyoodles.vincent.whiteboardclient;

/**
 * Created by vincent on 11/3/17.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CanvasView extends View {
    static final int PEN_TYPE = 1;
    static final int ERASER_TYPE = 2;

    public List<CanvasPath> localPaths;
    public List<CanvasPath> globalPaths;
    public Bitmap immutableBitmap;
    public Bitmap bitmap;
    private Canvas localCanvas;
    private Paint paint;
    private Paint transparent;
    private int currentPaintType;
    private CanvasFragment fragment;

    private SocketEventEmitter socketEmitter;
    public CanvasView(Context context, AttributeSet set) {
        super(context, set);

        setBackgroundColor(Color.WHITE);

        localPaths = new ArrayList<>();
        globalPaths = new ArrayList<>();
        paint = constructPaint(Color.BLACK, 10f);
        transparent = constructPaint(Color.WHITE, 25f);
        currentPaintType = PEN_TYPE;
    }

    public void setSocketEventListener(SocketEventEmitter emitter) {
        this.socketEmitter = emitter;
    }

    public void loadBitmap(Bitmap bmp) {
        this.bitmap = bmp;
        localCanvas = new Canvas(this.bitmap);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (immutableBitmap != null && !immutableBitmap.isRecycled()) {
            localCanvas.drawBitmap(immutableBitmap, 0, 0, paint);
            canvas.drawBitmap(immutableBitmap, 0, 0, paint);
        }

        for (CanvasPath p : localPaths)
            drawPath(p, canvas);

        for (CanvasPath p : globalPaths)
            drawPath(p, canvas);
    }

    public void loadFragment(CanvasFragment fragment) {
        this.fragment = fragment;
    }

    private void drawPath(CanvasPath p, Canvas canvas) {
        Path path = getResources().getConfiguration().orientation == getResources().getConfiguration().ORIENTATION_LANDSCAPE ? p.landscape : p.portrait;
        Paint curPaint = p.paint == PEN_TYPE ? paint : transparent;
        localCanvas.drawPath(path, curPaint);
        canvas.drawPath(path, curPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        socketEmitter.sendTouchEvent(event, currentPaintType);
        this.fragment.saveBitmap();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return startPath(eventX, eventY, currentPaintType, localPaths);
            case MotionEvent.ACTION_MOVE:
                movePath(eventX, eventY, localPaths);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setType(int type) {
        currentPaintType = type;
    }

    public void clear() {
        localPaths.clear();
        globalPaths.clear();
        if (immutableBitmap != null)
            immutableBitmap.recycle();
        invalidate();
    }

    public boolean startPath(float x, float y, int paintType, List<CanvasPath> list) {
        int currentRotation = getResources().getConfiguration().orientation;
        CanvasPath path = new CanvasPath(paintType);

        if (currentRotation == getResources().getConfiguration().ORIENTATION_PORTRAIT)
            path.moveTo(x, y);
        else
            path.moveTo(y, x);
        list.add(path);
        return true;
    }

    public boolean movePath(float x, float y, List<CanvasPath> list) {
        if (!list.isEmpty()) {
            CanvasPath path = list.get(list.size() - 1);
            if (getResources().getConfiguration().orientation == getResources().getConfiguration().ORIENTATION_PORTRAIT)
                path.lineTo(x, y);
            else
                path.lineTo(y, x);
        }
        return true;
    }

    private Paint constructPaint(int color, float width) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(width);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        return p;
    }
}