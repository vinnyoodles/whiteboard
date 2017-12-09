/**
 * This contains all the constants for the whiteboard app.
 * This must be kept in sync with the clientside file containing the constants.
 * client/app/src/main/java/com/example/vincent/whiteboardclient/Constants.java
 */
module.exports = {
    X_COORDINATE: 'x_coordinate',
    Y_COORDINATE: 'y_coordinate',
    TOUCH_EVENT: 'socket_touch_event',
    CLEAR_EVENT: 'socket_clear_event',
    EVENT_TYPE: 'action_event_type',
    PAINT_TYPE: 'canvas_paint_type',
    TOUCH_DOWN_EVENT: 'action_touch_down_event',
    TOUCH_MOVE_EVENT: 'action_touch_move_event',
    JOIN_ROOM_EVENT: 'join_room_event',
    ROOM_NAME_KEY: 'room_name_key',
    USER_NAME_KEY: 'user_name_key',
    NUMBER_OF_CLIENTS: 'number_of_clients',
    CANVAS_DATA: 'canvas_data',
    ROOM_METADATA_EVENT: 'room_metadata_event',
    SAVE_CANVAS_EVENT: 'save_canvas_event'
};
