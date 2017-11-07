var constants = require('./constants');
var app = require('express')();
var server = require('http').Server(app);
var websocket = require('socket.io')(server);

websocket.on('connection', (socket) => {
    socket.on(constants.TOUCH_EVENT, (json) => onTouch(socket, json));
    socket.on(constants.CLEAR_EVENT, () => onClear(socket));
});

function onTouch(socket, json) {
    socket.broadcast.emit(constants.TOUCH_EVENT, json);
}

function onClear(socket) {
    socket.broadcast.emit(constants.CLEAR_EVENT);
}

server.listen(3000, () => console.log('listening on *:3000'));
