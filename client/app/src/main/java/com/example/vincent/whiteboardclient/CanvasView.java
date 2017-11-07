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

    private List<Path> paths;
    private List<Integer> paints;
    private Paint paint;
    private Paint transparent;
    private int currentPaintType;

    private SocketEventListener socketListener;
    public CanvasView(Context context, AttributeSet set) {
        super(context, set);

        setBackgroundColor(Color.WHITE);

        paths = new ArrayList<>();
        paints = new ArrayList<>();
        paint = constructPaint(Color.BLACK, 10f);
        transparent = constructPaint(Color.WHITE, 20f);
        currentPaintType = PEN_TYPE;
    }

    public void setSocketEventListener(SocketEventListener listener) {
        this.socketListener = listener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasMove()) {
            return;
        }
        for (int i = 0; i < paths.size(); i ++)
            canvas.drawPath(paths.get(i), paints.get(i) == PEN_TYPE ? paint : transparent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        socketListener.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return startPath(eventX, eventY);
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
            paints.clear();
        }
        invalidate();
    }

    public boolean startPath(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        paths.add(path);
        paints.add(currentPaintType);
        return true;
    }

    public boolean movePath(float x, float y) {
        if (!paths.isEmpty()) {
            paths.get(paths.size() - 1).lineTo(x, y);
        }
        return true;
    }

    private boolean hasMove() {
        return paths != null && !paths.isEmpty() && paints != null && !paints.isEmpty();
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