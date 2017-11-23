package com.example.vincent.whiteboardclient;

import android.graphics.Path;

/**
 * Created by vincent on 11/7/17.
 */

public class CanvasPath extends Path {
    public int paint;
    public int rotation;

    public CanvasPath(int paint, int rotation) {
        super();
        this.paint = paint;
        this.rotation = rotation;
    }
}
