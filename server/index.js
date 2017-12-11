var _ = require('underscore');
var constants = require('./constants');
var app = require('express')();
var server = require('http').Server(app);
var websocket = require('socket.io')(server);
var mongojs = require('mongojs');

var ObjectID = mongojs.ObjectID;
var db = mongojs('mongodb://localhost:27017/local');

var clients = {};

var throttledSave = _.throttle(onSave, 100 /*at most once per 100ms*/);

websocket.on('connection', (socket) => {
    socket.on(constants.TOUCH_EVENT, (json) => onTouch(socket, json));
    socket.on(constants.CLEAR_EVENT, () => onClear(socket));
    socket.on(constants.JOIN_ROOM_EVENT, (user) => onJoin(socket, user));
    socket.on(constants.SAVE_CANVAS_EVENT, throttledSave);
    socket.on(constants.LOCATION_EVENT, (location) => onLocation(socket, location));
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
                console.log('mongoDB Error:', err)
                return;
            }
            sendRoomData(socket, room);
        });
}

function onSave(encoded) {
    db.rooms.update(
        { name: 'room_name' },
        { $set: { data: encoded } },
        { upsert: true },
        (err) => {
            if (err != null)
                console.log('Error: failed to save encoded canvas data');
    });
}

function onLocation(socket, location) {
    if (!location) {
        console.log('Error: onLocation with no location');
        return;
    }

    if (!clients[socket.id]) 
        clients[socket.id] = {};

    // Update the location for the respective socket in the room object.
    clients[socket.id].location = location;
    sendLocationData();
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
    sendLocationData();
}

function sendLocationData() {
    var names = [];
    var locations = [];
    _.each(clients, (client, _) => {
        if (!client) return;
        names.push(client.username ? client.username : 'Unknown');
        locations.push(client.location ? client.location : 'Unknown');
    })
    var json = {};
    json[constants.CLIENT_NAMES] = names;
    json[constants.CLIENT_LOCATIONS] = locations;
    // Emit to everyone including sender.
    websocket.emit(constants.EMIT_LOCATION_EVENT, json);
}

function leaveRoom(socket) {
    clients[socket.id] = undefined;
}

server.listen(3000, () => console.log('listening on *:3000'));
