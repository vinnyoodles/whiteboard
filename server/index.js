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
    console.log(type, x + ', ' + y);
}

var stdin = process.openStdin();
stdin.addListener('data', function(d) {
    websocket.emit(TOUCH_EVENT, {
        message: d.toString().trim(),
        username: 'vincent'
    });
});