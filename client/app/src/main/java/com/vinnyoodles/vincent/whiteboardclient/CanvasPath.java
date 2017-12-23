package com.vinnyoodles.vincent.whiteboardclient;

import android.graphics.Path;

/**
 * Created by vincent on 11/7/17.
 */

public class CanvasPath {
    public int paint;
    public Path portrait;
    public Path landscape;

    public CanvasPath(int paint) {
        this.paint = paint;
        portrait = new Path();
        landscape = new Path();
    }

    public void moveTo(float x, float y) {
        portrait.moveTo(x, y);
        landscape.moveTo(y, x);
    }

    public void lineTo(float x, float y) {
        portrait.lineTo(x, y);
        landscape.lineTo(y, x);
    }
}
