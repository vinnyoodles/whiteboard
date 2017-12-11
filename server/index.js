var _ = require('underscore');
var constants = require('./constants');
var app = require('express')();
var server = require('http').Server(app);
var websocket = require('socket.io')(server);
var mongojs = require('mongojs');

var ObjectID = mongojs.ObjectID;
var db = mongojs('mongodb://localhost:27017/local');

var clients = {};

websocket.on('connection', (socket) => {
    socket.on(constants.TOUCH_EVENT, (json) => onTouch(socket, json));
    socket.on(constants.CLEAR_EVENT, () => onClear(socket));
    socket.on(constants.JOIN_ROOM_EVENT, (json) => onJoin(socket, json));
    socket.on(constants.SAVE_CANVAS_EVENT, (json) => onSave(socket, json));
    socket.on(constants.LOCATION_EVENT, (json) => onLocation(socket, json));
    socket.on(constants.AUDIO_STREAM, (buffer, bytes) => onAudio(socket, buffer, bytes));
    socket.on('disconnect', () => leaveRoom(socket));
});

function onTouch(socket, json) {
    socket.broadcast.emit(constants.TOUCH_EVENT, json);
}

function onClear(socket) {
    socket.broadcast.emit(constants.CLEAR_EVENT);
}

function onJoin(socket, json) {
    socket.username = json[constants.USER_NAME_KEY];
    if (!clients[socket.id]) clients[socket.id] = {};
    clients[socket.id].username = socket.username;
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

function onSave(socket, json) {
    var encoded = json[constants.CANVAS_DATA];
    db.rooms.update(
        { name: 'room_name' },
        { $set: { data: encoded } },
        { upsert: true },
        (err) => {
            if (err != null)
                console.log('Error: failed to save encoded canvas data');
    });
}

function onLocation(socket, json) {
    var location = json[constants.LOCATION_KEY];
    if (!location) {
        console.log('Error: onLocation with no location');
        return;
    }

    if (!clients[socket.id]) 
        clients[socket.id] = {};

    // Update the location for the respective socket in the room object.
    clients[socket.id].location = location;
    sendLocationData(socket);
}

function onAudio(socket, buffer, bytes) {
    // Simple relay of buffer to all other clients
    socket.broadcast.emit(constants.AUDIO_STREAM, buffer, bytes);
}

function sendRoomData(socket, room) {
    var json = {};
    json[constants.NUMBER_OF_CLIENTS] = Object.keys(clients).length;
    if (room != null)
       json[constants.CANVAS_DATA] = room.data;
    socket.emit(constants.ROOM_METADATA_EVENT, json);
    sendLocationData(socket, room);
}

function sendLocationData(socket) {
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
