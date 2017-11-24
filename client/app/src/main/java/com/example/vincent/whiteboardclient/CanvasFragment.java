package com.example.vincent.whiteboardclient;

import android.app.Fragment;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by vincent on 11/5/17.
 */

public class CanvasFragment extends Fragment implements SocketEventEmitter, View.OnClickListener {
    /* View Variables */
    private FragmentCallback cb;
    private CanvasView canvasView;
    private FloatingActionButton clearButton;
    private FloatingActionButton penButton;
    private FloatingActionButton eraserButton;
    private double width;
    private double height;
    private List<CanvasPath> paths;
    private List<CanvasPath> landscapePaths;
    private boolean hasListeners = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        addListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_canvas, parent, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        paths = canvasView.paths;
        landscapePaths = canvasView.landscapePaths;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            width = bundle.getDouble(Constants.WIDTH);
            height = bundle.getDouble(Constants.HEIGHT);
        }
        // TODO: draw paths relative to screen orientation.
        canvasView = (CanvasView) view.findViewById(R.id.canvas);
        if (paths != null) canvasView.paths = paths;
        if (landscapePaths != null) canvasView.landscapePaths = landscapePaths;
        canvasView.invalidate();

        clearButton = (FloatingActionButton) view.findViewById(R.id.clear_button);
        penButton = (FloatingActionButton) view.findViewById(R.id.pen_button);
        eraserButton = (FloatingActionButton) view.findViewById(R.id.eraser_button);

        clearButton.setOnClickListener(this);
        penButton.setOnClickListener(this);
        eraserButton.setOnClickListener(this);
        canvasView.setSocketEventListener(this);

        cb.onFragmentViewCreated();
    }

    public void setRotation(int rotation) {
        canvasView.setRotation(rotation);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == penButton.getId()) {
            Toast.makeText(getActivity().getApplicationContext(), "pen", Toast.LENGTH_SHORT).show();
            canvasView.setType(CanvasView.PEN_TYPE);
        } else if (view.getId() == eraserButton.getId()) {
            Toast.makeText(getActivity().getApplicationContext(), "eraser", Toast.LENGTH_SHORT).show();
            canvasView.setType(CanvasView.ERASER_TYPE);
        } else if (view.getId() == clearButton.getId()) {
            // Emit the clear event to the socket.
            cb.getSocketInstance().emit(Constants.CLEAR_EVENT);
            canvasView.clear();
        }
    }

    /**
     * Emit local event to socket.
     */
    public void sendTouchEvent(MotionEvent event, int paintType) {
        String eventType;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                eventType = Constants.TOUCH_DOWN_EVENT;
                break;
            case MotionEvent.ACTION_MOVE:
                eventType = Constants.TOUCH_MOVE_EVENT;
                break;
            default:
                return; // Ignore irrelevant events.
        }

        JSONObject json = new JSONObject();
        try {
            json.put(Constants.X_COORDINATE, ((double) event.getX() / width));
            json.put(Constants.Y_COORDINATE, ((double) event.getY() / height));
            json.put(Constants.EVENT_TYPE, eventType);
            json.put(Constants.PAINT_TYPE, paintType);
        } catch (org.json.JSONException e) {
            Log.e("json", e.getLocalizedMessage());
        }
        cb.getSocketInstance().emit(Constants.TOUCH_EVENT, json);
    }

    public void setCallback(FragmentCallback cb) {
        this.cb = cb;
    }

    /* Helper Functions */

    public void addListeners() {
        if (hasListeners)
            return;
        hasListeners = true;
        Socket socket = cb.getSocketInstance();
        socket.on(Constants.TOUCH_EVENT, onReceivedTouchEvent);
        socket.on(Constants.CLEAR_EVENT, onClearEvent);
    }

    public void removeListeners() {
        if (!hasListeners)
            return;
        hasListeners = false;
        Socket socket = cb.getSocketInstance();
        socket.off(Constants.TOUCH_EVENT, onReceivedTouchEvent);
        socket.off(Constants.CLEAR_EVENT, onClearEvent);
    }

    /* Socket Listeners */

    private Emitter.Listener onReceivedTouchEvent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = (JSONObject) args[0];
                    try {
                        float x = (float) (json.getDouble(Constants.X_COORDINATE) * width);
                        float y = (float) (json.getDouble(Constants.Y_COORDINATE) * height);
                        int paintType = json.getInt(Constants.PAINT_TYPE);

                        switch (json.getString(Constants.EVENT_TYPE)) {
                            case Constants.TOUCH_DOWN_EVENT:
                                canvasView.startPath(x, y, paintType);
                                break;
                            case Constants.TOUCH_MOVE_EVENT:
                                canvasView.movePath(x, y);
                                break;
                            default:
                                return;
                        }
                        canvasView.invalidate();
                    } catch (org.json.JSONException e) {
                        Log.e("json", e.getLocalizedMessage());
                    }
                }
            });
        }
    };

    private Emitter.Listener onClearEvent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    canvasView.clear();
                }
            });
        }
    };
}