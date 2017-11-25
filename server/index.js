var constants = require('./constants');
var app = require('express')();
var server = require('http').Server(app);
var websocket = require('socket.io')(server);
var mongojs = require('mongojs');

var ObjectID = mongojs.ObjectID;
var db = mongojs('mongodb://localhost:27017/local');

var rooms = {};

websocket.on('connection', (socket) => {
    console.log('connecting', socket.id);
    socket.on(constants.TOUCH_EVENT, (json) => onTouch(socket, json));
    socket.on(constants.CLEAR_EVENT, () => onClear(socket));
    socket.on(constants.JOIN_ROOM_EVENT, (json) => onJoin(socket, json));
    socket.on(constants.SAVE_CANVAS_EVENT, (json) => onSave(socket, json));
    socket.on('disconnect', () => leaveRoom(socket));
});

function onTouch(socket, json) {
    if (!socket.roomName) {
        console.log('Error: onTouch on socket without room');
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
    joinRoom(socket, roomName);
    db.rooms
        .findOne({ name: roomName }, 
        (err, room) => {
            if (err != null) {
                console.log('Error: ', err)
                return;
            }
            console.log('Found room', roomName, !!room);
            sendRoomData(socket, room);
        });
}

function onSave(socket, json) {
    if (!socket.roomName) {
        console.log('Error: onSave with no room name');
        return;
    }

    var encoded = json[constants.CANVAS_DATA];
    console.log('saving room data', encoded.length)
    db.rooms.update(
        { name: socket.roomName },
        { $set: { data: encoded } },
        { upsert: true },
        (err) => {
            if (err != null)
                console.log('Error: failed to save encoded canvas data');
    });
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
}

function joinRoom(socket, roomName) {
    console.log('joining room', roomName);
    var room = rooms[roomName];
    socket.join(roomName);
    socket.roomName = roomName;
    if (!rooms[roomName])
        rooms[roomName] = {};
    rooms[roomName][socket.id] = socket;
}
 
function leaveRoom(socket) {
    console.log('disconnecting');
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