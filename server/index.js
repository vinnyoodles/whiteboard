var constants = require('./constants');
var app = require('express')();
var server = require('http').Server(app);
var websocket = require('socket.io')(server);
var mongojs = require('mongojs');

var ObjectID = mongojs.ObjectID;
var db = mongojs('mongodb://localhost:27017/local');

var rooms = {};

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
    if (!socket.roomName) {
        console.log('Error: onTouch with socket without room');
        return;
    }
    socket.broadcast.to(socket.roomName).emit(constants.TOUCH_EVENT, json);
}

function onClear(socket) {
    if (!socket.roomName) {
        console.log('Error: onClear on socket without room');
        return;
    }
    socket.broadcast.to(socket.roomName).emit(constants.CLEAR_EVENT);
}

function onJoin(socket, json) {
    var roomName = json[constants.ROOM_NAME_KEY];
    socket.username = json[constants.USER_NAME_KEY];
    joinRoom(socket, roomName);
    db.rooms
        .findOne({ name: roomName },
        (err, room) => {
            if (err != null) {
                console.log('Error:', err)
                return;
            }
            sendRoomData(socket, room);
        });
}

function onSave(socket, json) {
    if (!socket.roomName) {
        console.log('Error: onSave with no room name');
        return;
    }

    var encoded = json[constants.CANVAS_DATA];
    db.rooms.update(
        { name: socket.roomName },
        { $set: { data: encoded } },
        { upsert: true },
        (err) => {
            if (err != null)
                console.log('Error: failed to save encoded canvas data');
    });
}

function onLocation(socket, json) {
    if (!socket.roomName) {
        console.log('Error: onLocation with no room name');
        return;
    }

    var location = json[constants.LOCATION_KEY];
    if (!location) {
        console.log('Error: onLocation with no location');
        return;
    }

    if (!rooms[socket.roomName] || !rooms[socket.roomName][socket.id]) {
        console.log('Error: onLocation but user is not in a room');
        return;
    }

    // Update the location for the respective socket in the room object.
    rooms[socket.roomName][socket.id].location = location;
    sendLocationData(socket);
}

function onAudio(socket, buffer, bytes) {
    if (!socket.roomName || !rooms[socket.roomName]) {
        console.log('Error: onAudio invalid room');
        return;
    }
    // Simple relay of buffer to all other clients
    socket.broadcast.to(socket.roomName).emit(constants.AUDIO_STREAM, buffer);
}

function sendRoomData(socket, room) {
    var clients = rooms[socket.roomName];
    if (!clients) {
        console.log('Error: sendRoomData without room name');
        return;
    }
    var json = {};
    json[constants.NUMBER_OF_CLIENTS] = Object.keys(clients).length;
    if (room != null)
       json[constants.CANVAS_DATA] = room.data;
    socket.emit(constants.ROOM_METADATA_EVENT, json);
    sendLocationData(socket, room);
}

function sendLocationData(socket) {
    var clients = rooms[socket.roomName];
    if (!clients) {
        console.log('Error: sendLocationData without room name');
        return;
    }
    var names = [];
    var locations = [];
    for (var id of Object.keys(clients)) {
        names.push(clients[id].username ? clients[id].username : 'unknown');
        locations.push(clients[id].location ? clients[id].location : 'unknown');
    }
    var json = {};
    json[constants.CLIENT_NAMES] = names;
    json[constants.CLIENT_LOCATIONS] = locations;
    // Emit to everyone including sender.
    websocket.sockets.in(socket.roomName).emit(constants.EMIT_LOCATION_EVENT, json);
}

function joinRoom(socket, roomName) {
    socket.join(roomName);
    socket.roomName = roomName;
    if (!rooms[roomName])
        rooms[roomName] = {};
    rooms[roomName][socket.id] = socket;
}

function leaveRoom(socket) {
    var roomName = socket.roomName;
    if (!roomName)
        return;
    var room = rooms[roomName];
    socket.leave(roomName);
    socket.roomName = undefined;
    if (!room)
        return;
    room[socket.id] = undefined;
}

server.listen(3000, () => console.log('listening on *:3000'));
