package com.example.vincent.whiteboardclient;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
    private FloatingActionButton listButton;
    private TextView listText;

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

        canvasView = (CanvasView) view.findViewById(R.id.canvas);
        if (paths != null) canvasView.paths = paths;
        if (landscapePaths != null) canvasView.landscapePaths = landscapePaths;

        canvasView.loadBitmap(Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888));

        clearButton = (FloatingActionButton) view.findViewById(R.id.clear_button);
        penButton = (FloatingActionButton) view.findViewById(R.id.pen_button);
        eraserButton = (FloatingActionButton) view.findViewById(R.id.eraser_button);
        listButton = (FloatingActionButton) view.findViewById(R.id.list_button);
        listText = (TextView) view.findViewById(R.id.list_text);
        clearButton.setOnClickListener(this);
        penButton.setOnClickListener(this);
        eraserButton.setOnClickListener(this);
        listButton.setOnClickListener(this);
        canvasView.setSocketEventListener(this);
        listText.setMovementMethod(new ScrollingMovementMethod());

        cb.onFragmentViewCreated();
    }

    public void setRotation(int rotation) {
        canvasView.setRotation(rotation);
    }

    @Override
    public void onClick(View view) {
        if (penButton != null && view.getId() == penButton.getId()) {
            canvasView.setType(CanvasView.PEN_TYPE);
        } else if (eraserButton != null && view.getId() == eraserButton.getId()) {
            canvasView.setType(CanvasView.ERASER_TYPE);
        } else if (clearButton != null && view.getId() == clearButton.getId()) {
            // Emit the clear event to the socket.
            cb.getSocketInstance().emit(Constants.CLEAR_EVENT);
            canvasView.loadBitmap(Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888));
            canvasView.clear();

            saveBitmap();
        } else if (listButton != null && view.getId() == listButton.getId()) {
            listText.setVisibility(listText.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Emit canvas to save in server.
     */
    public void saveBitmap() {
        if (canvasView.bitmap == null) {
            Log.d("Error", "Saving null bitmap");
            return;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        canvasView.bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        byte[] arr = output.toByteArray();
        String encoded = Base64.encodeToString(arr, Base64.DEFAULT);
        cb.getSocketInstance().emit(Constants.SAVE_CANVAS_EVENT, encoded);
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
        socket.on(Constants.ROOM_METADATA_EVENT, onMetadataReceived);
        socket.on(Constants.EMIT_LOCATION_EVENT, onLocationDataReceived);
        socket.on(Constants.AUDIO_STREAM, onAudioReceived);
    }

    public void removeListeners() {
        if (!hasListeners)
            return;
        hasListeners = false;
        Socket socket = cb.getSocketInstance();
        socket.off(Constants.TOUCH_EVENT, onReceivedTouchEvent);
        socket.off(Constants.CLEAR_EVENT, onClearEvent);
        socket.off(Constants.ROOM_METADATA_EVENT, onMetadataReceived);
        socket.off(Constants.EMIT_LOCATION_EVENT, onLocationDataReceived);
        socket.off(Constants.AUDIO_STREAM, onAudioReceived);
    }

    private void updateList(JSONArray names, JSONArray locations) {
        if (listText == null || names == null || locations == null)
            return;
        StringBuilder b = new StringBuilder();
        int index = 0;
        int length = names.length();
        b.append("Room Details:\n");
        try {
            while (index < length) {
                String name = names.getString(index);
                String location = locations.getString(index);
                index++;

                b.append(String.format("%s: %s\n\n", name, location));
            }
            listText.setText(b.toString());
        } catch (JSONException e) {
            Log.d("json", "failed to read name/location json array");
        }
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

    private Emitter.Listener onMetadataReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = (JSONObject) args[0];
                    try {
                        int numOfClients = json.getInt(Constants.NUMBER_OF_CLIENTS);
                        String message;
                        if (numOfClients <= 1) {
                            message = "You are alone in this room";
                        } else {
                            message = "Active user(s): " + (numOfClients - 1);
                        }
                        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        String encoded = json.getString(Constants.CANVAS_DATA);
                        if (encoded != null) {
                            byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
                            canvasView.immutableBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            canvasView.invalidate();
                        }

                    } catch (org.json.JSONException e) {
                        Log.e("json", e.getLocalizedMessage());
                    }

                }
            });
        }
    };

    private Emitter.Listener onLocationDataReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = (JSONObject) args[0];
                    try {
                        JSONArray jsonNames = json.getJSONArray(Constants.CLIENT_NAMES);
                        JSONArray jsonLocations = json.getJSONArray(Constants.CLIENT_LOCATIONS);
                        if (jsonNames == null || jsonLocations == null || jsonNames.length() != jsonLocations.length()) {
                            return;
                        }
                        updateList(jsonNames, jsonLocations);

                    } catch (org.json.JSONException e) {
                        Log.e("json", e.getLocalizedMessage());
                    }

                }
            });
        }
    };

    private Emitter.Listener onAudioReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args.length < 2)
                        return;
                    byte[] buffer = (byte[]) args[0];
                    int length = (int) args[1];
                    int format = AudioFormat.CHANNEL_OUT_MONO;
                    int encoding = AudioFormat.ENCODING_PCM_16BIT;
                    int maxJitter = AudioTrack.getMinBufferSize(Constants.AUDIO_SAMPLE_RATE, format, encoding);
                    AudioTrack track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, Constants.AUDIO_SAMPLE_RATE, format,
                            encoding, maxJitter, AudioTrack.MODE_STREAM);
                    if (track.getPlayState() != 1 || track.getState() != 1)
                        return;

                    track.play();
                    track.write(buffer, 0, length);
                    track.stop();
                    track.release();
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