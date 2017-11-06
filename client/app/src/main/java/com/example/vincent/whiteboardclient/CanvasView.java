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

    private List<Path> paths;
    private List<Path> socketPaths;
    private Paint paint;

    private SocketEventListener socketListener;
    public CanvasView(Context context, AttributeSet set) {
        super(context, set);

        paths = new ArrayList<>();
        socketPaths = new ArrayList<>();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setSocketEventListener(SocketEventListener listener) {
        this.socketListener = listener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasMove()) {
            return;
        }
        for (Path p : paths)
            canvas.drawPath(p, paint);
        for (Path p : socketPaths)
            canvas.drawPath(p, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        socketListener.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return startPath(eventX, eventY, false /* local path */);
            case MotionEvent.ACTION_MOVE:
                movePath(eventX, eventY, false /* local path */);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setType(int type) {
        switch (type) {
            case PEN_TYPE:
                break;
            case ERASER_TYPE:
                break;
        }
    }

    public void clear() {
        if (hasMove()) {
            paths.clear();
        }
        invalidate();
    }

    public boolean startPath(float x, float y, boolean isSocket) {
        Path path = new Path();
        path.moveTo(x, y);
        (isSocket ? socketPaths : paths).add(path);
        return true;
    }

    public boolean movePath(float x, float y, boolean isSocket) {
        List<Path> currentPaths = isSocket ? socketPaths : paths;
        if (!currentPaths.isEmpty()) {
            currentPaths.get(currentPaths.size() - 1).lineTo(x, y);
        }
        return true;
    }

    private boolean hasMove() {
        return paths != null && !paths.isEmpty();
    }
}