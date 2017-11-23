package com.example.vincent.whiteboardclient;

/**
 * Created by vincent on 11/3/17.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CanvasView extends View {
    static final int PEN_TYPE = 1;
    static final int ERASER_TYPE = 2;

    public List<CanvasPath> paths;
    public List<CanvasPath> landscapePaths;
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

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasMove())
            return;

        for (int i = 0; i < paths.size(); i ++) {
            CanvasPath path = paths.get(i);
            CanvasPath landscapePath = landscapePaths.get(i);
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
        if (hasMove()) {
            paths.clear();
            landscapePaths.clear();
        }
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

    private boolean hasMove() {
        return paths != null && !paths.isEmpty();
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