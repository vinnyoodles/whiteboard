var _ = require('underscore');
var express = require('express');
var http = require('http');
var socketio = require('socket.io');
var mongojs = require('mongojs');

var constants = require('./constants');

var app = express();
var server = http.Server(app);
var websocket = socketio(server);
var ObjectID = mongojs.ObjectID;
var db = mongojs(process.env.MONGODB_URI || 'mongodb://localhost:27017/local');

var clients = {};

var throttledSave = _.throttle(onSave, 100 /*at most once per 100ms*/);

websocket.on('connection', (socket) => {
    socket.on(constants.TOUCH_EVENT, (json) => onTouch(socket, json));
    socket.on(constants.CLEAR_EVENT, () => onClear(socket));
    socket.on(constants.JOIN_ROOM_EVENT, (user) => onJoin(socket, user));
    socket.on(constants.SAVE_CANVAS_EVENT, throttledSave);
    socket.on(constants.AUDIO_STREAM, (buffer, bytes) => onAudio(socket, buffer, bytes));
    socket.on('disconnect', () => leaveRoom(socket));
});

function onTouch(socket, json) {
    socket.broadcast.emit(constants.TOUCH_EVENT, json);
}

function onClear(socket) {
    socket.broadcast.emit(constants.CLEAR_EVENT);
}

function onJoin(socket, user) {
    socket.username = user;
    if (!clients[socket.id]) clients[socket.id] = {};
    clients[socket.id].username = user;
    db.rooms
        .findOne({ name: 'room_name' },
        (err, room) => {
            if (err != null) {
                console.log('Mongodb err:', err);
                return;
            }
            sendRoomData(socket, room);
        });
}

function onSave(encoded) {
    db.rooms.update(
        { name: 'room_name' },
        { $set: { data: encoded, updated_at: new Date() } },
        { upsert: true },
        (err) => {
            if (err != null)
                console.log('Error: failed to save encoded canvas data');
    });
}

function onAudio(socket, buffer, bytes) {
    // Simple relay of buffer to all other clients
    socket.broadcast.emit(constants.AUDIO_STREAM, buffer, bytes);
}

function sendRoomData(socket, room) {
    var json = {};
    json[constants.NUMBER_OF_CLIENTS] = _.reduce(clients, (m, c) => m + (!!c ? 1 : 0), 0);
    if (room != null)
       json[constants.CANVAS_DATA] = room.data;
    socket.emit(constants.ROOM_METADATA_EVENT, json);
}

function leaveRoom(socket) {
    clients[socket.id] = undefined;
}

app.get('/', (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    db.rooms.findOne({ name: 'room_name' }, (err, room) => {
            if (err == null) {
                res.send(JSON.stringify({ clients, room }));
            } else {
                res.send(JSON.stringify({ clients }));
            }
        });
});

server.listen(process.env.PORT || 3000, () => console.log('listening on *:3000'));
