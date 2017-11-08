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
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CanvasView extends View {
    static final int PEN_TYPE = 1;
    static final int ERASER_TYPE = 2;

    public List<CanvasPath> paths;
    private Paint paint;
    private Paint transparent;
    private int currentPaintType;

    private SocketEventEmitter socketEmitter;
    public CanvasView(Context context, AttributeSet set) {
        super(context, set);

        setBackgroundColor(Color.WHITE);

        paths = new ArrayList<>();
        paint = constructPaint(Color.BLACK, 10f);
        transparent = constructPaint(Color.WHITE, 25f);
        currentPaintType = PEN_TYPE;
    }

    public void setSocketEventListener(SocketEventEmitter emitter) {
        this.socketEmitter = emitter;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasMove()) {
            return;
        }
        for (CanvasPath p : paths)
            canvas.drawPath(p, p.paint == PEN_TYPE ? paint : transparent);
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
        }
        invalidate();
    }

    public boolean startPath(float x, float y, int paintType) {
        CanvasPath path = new CanvasPath(paintType);
        path.moveTo(x, y);
        paths.add(path);
        return true;
    }

    public boolean movePath(float x, float y) {
        if (!paths.isEmpty()) {
            paths.get(paths.size() - 1).lineTo(x, y);
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