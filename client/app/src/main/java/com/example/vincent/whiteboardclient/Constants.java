package com.example.vincent.whiteboardclient;

/**
 * Created by vincent on 11/3/17.
 */

public class Constants {
    /* View Constants */
    public static final String X_COORDINATE = "x_coordinate";
    public static final String Y_COORDINATE = "y_coordinate";
    public static final String WIDTH = "view_width";
    public static final String HEIGHT = "view_height";
    public static final String RETAINED_CANVAS_FRAGMENT = "retained_canvas_fragment_id";
    public static final String RETAINED_ROOM_FRAGMENT = "retained_room_fragment_id";
    public static final String JOIN_ROOM_EVENT = "join_room_event";
    public static final String USER_NAME_KEY = "user_name_key";
    public static final String NUMBER_OF_CLIENTS = "number_of_clients";
    public static final String CANVAS_DATA = "canvas_data";
    public static final String ROOM_METADATA_EVENT = "room_metadata_event";
    public static final String SAVE_CANVAS_EVENT = "save_canvas_event";
    public static final String LOCATION_EVENT = "location_event";
    public static final String CLIENT_NAMES = "client_names";
    public static final String CLIENT_LOCATIONS = "client_locations";

    /* Event Constants */
    public static final String TOUCH_EVENT = "socket_touch_event";
    public static final String CLEAR_EVENT = "socket_clear_event";
    public static final String EVENT_TYPE = "action_event_type";
    public static final String PAINT_TYPE = "canvas_paint_type";
    public static final String TOUCH_DOWN_EVENT = "action_touch_down_event";
    public static final String TOUCH_MOVE_EVENT = "action_touch_move_event";
    public static final String EMIT_LOCATION_EVENT = "emit_location_event";
    public static final String AUDIO_STREAM = "audio_stream";
    public static final String REQUEST_LOCATION = "request_location";

    /* Networking Constants */
    public static final String SERVER_URL = "https://90e43cea.ngrok.io";

    /* Audio Constants */
    public static final int AUDIO_SAMPLE_RATE = 16000;
    public static final String IS_STREAMING_KEY = "is_streaming_key";
}