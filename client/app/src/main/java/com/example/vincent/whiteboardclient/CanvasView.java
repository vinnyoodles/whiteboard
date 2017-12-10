package com.example.vincent.whiteboardclient;

/**
 * Created by vincent on 11/3/17.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CanvasView extends View {
    static final int PEN_TYPE = 1;
    static final int ERASER_TYPE = 2;

    public List<CanvasPath> paths;
    public List<CanvasPath> landscapePaths;
    public Bitmap immutableBitmap;
    public Bitmap bitmap;
    private Canvas localCanvas;
    private Paint paint;
    private Paint transparent;
    private int currentPaintType;
    private int currentRotation;

    private SocketEventEmitter socketEmitter;
    public CanvasView(Context context, AttributeSet set) {
        super(context, set);

        setBackgroundColor(Color.WHITE);

        paths = new ArrayList<>();
        landscapePaths = new ArrayList<>();
        paint = constructPaint(Color.BLACK, 10f);
        transparent = constructPaint(Color.WHITE, 25f);
        currentPaintType = PEN_TYPE;
    }

    public void setSocketEventListener(SocketEventEmitter emitter) {
        this.socketEmitter = emitter;
    }

    public void setRotation(int rotation) {
        currentRotation = rotation;
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

        for (int i = 0; i < paths.size(); i ++) {
            CanvasPath path = paths.get(i);
            CanvasPath landscapePath = landscapePaths.get(i);
            localCanvas.drawPath(path.rotation != currentRotation ? landscapePath : path, path.paint == PEN_TYPE ? paint : transparent);
            canvas.drawPath(path.rotation != currentRotation ? landscapePath : path, path.paint == PEN_TYPE ? paint : transparent);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        socketEmitter.sendTouchEvent(event, currentPaintType);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return startPath(eventX, eventY, currentPaintType);
            case MotionEvent.ACTION_MOVE:
                movePath(eventX, eventY);
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
        paths.clear();
        landscapePaths.clear();
        immutableBitmap.recycle();
        invalidate();
    }

    public boolean startPath(float x, float y, int paintType) {
        CanvasPath path = new CanvasPath(paintType, currentRotation);
        CanvasPath landscapePath = new CanvasPath(paintType, currentRotation);

        path.moveTo(x, y);
        landscapePath.moveTo(y, x);
        paths.add(path);
        landscapePaths.add(landscapePath);
        return true;
    }

    public boolean movePath(float x, float y) {
        if (!paths.isEmpty()) {
            paths.get(paths.size() - 1).lineTo(x, y);
            landscapePaths.get(landscapePaths.size() - 1).lineTo(y, x);
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