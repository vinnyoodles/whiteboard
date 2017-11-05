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
    private List<Path> paths;
    private List<Paint> paints;
    private int currentColor;

    private SocketEventListener socketListener;
    public CanvasView(Context context, AttributeSet set) {
        super(context, set);

        paints = new ArrayList<>();
        paths = new ArrayList<>();
        currentColor = Color.BLACK;
    }

    public void setSocketEventListener(SocketEventListener listener) {
        this.socketListener = listener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasMove()) {
            return;
        }
        for (int i = 0; i < paints.size(); i ++) {
            canvas.drawPath(paths.get(i), paints.get(i));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        socketListener.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return start(eventX, eventY);
            case MotionEvent.ACTION_MOVE:
                move(eventX, eventY);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setPaint(int color) {
        currentColor = color;
    }

    public void undo() {
        if (hasMove()) {
            paths.remove(paths.size() - 1);
            paints.remove(paints.size() - 1);
        }
        invalidate();
    }

    public void clear() {
        if (hasMove()) {
            paths.clear();
            paints.clear();
        }
        invalidate();
    }

    private boolean start(float x, float y) {
        Path path = new Path();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10f);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        path.moveTo(x, y);
        paths.add(path);
        paints.add(paint);
        return true;
    }

    private boolean move(float x, float y) {
        if (!paths.isEmpty()) {
            // If there is at least one path,
            // then move the last path to the new x, y coordinate.
            paths.get(paths.size() - 1).lineTo(x, y);
        }
        return true;
    }

    private boolean hasMove() {
        return paths != null && paints != null && !paths.isEmpty() && !paints.isEmpty();
    }
}