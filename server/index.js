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
    // Relay touch events to other clients.
    socket.on(constants.TOUCH_EVENT, (json) => {
        socket.broadcast.emit(constants.TOUCH_EVENT, json);
    });
});
