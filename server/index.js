var constants = require('./constants');
var app = require('express')();
var server = require('http').Server(app);
var websocket = require('socket.io')(server);
server.listen(3000, () => console.log('listening on *:3000'));

var TOUCH_EVENT = 'socket_touch_event';

// Mapping objects to easily map sockets and users.
var clients = {};
var users = {};

// This represents a unique chatroom.
// For this example purpose, there is only one chatroom;
var chatId = 1;

websocket.on('connection', (socket) => {
    clients[socket.id] = socket;
    socket.on(constants.TOUCH_EVENT, onTouchEvent);
});

function onTouchEvent(json) {
    var x = json[constants.X_COORDINATE];
    var y = json[constants.Y_COORDINATE];
    var type = json[constants.EVENT_TYPE];

    // Emit back after shift by 50.
    json[constants.X_COORDINATE] += 50;
    json[constants.Y_COORDINATE] += 50;
    websocket.emit(TOUCH_EVENT, json);
}

var stdin = process.openStdin();
stdin.addListener('data', function(data) {
    var json = {};
    var input = data.toString().split(' ');
    json[constants.EVENT_TYPE] = input[0];
    json[constants.X_COORDINATE] = input[1];
    json[constants.Y_COORDINATE] = input[2];

    websocket.emit(TOUCH_EVENT, json);
});