var events = require('events');

var eventEmitter = new events.EventEmitter();

//自定义事件
eventEmitter.on('some_event', function() {
    console.log('some_event trigged.');
});

setTimeout(function() {
    eventEmitter.emit('some_event');
}, 1000);

//事件传参
eventEmitter.on('eventWithParam', function(arg1, arg2) {
    console.log('eventWithParam trigged.', arg1, arg2);
});

eventEmitter.emit('eventWithParam', 'arg1 parameter', 'arg2 parameter');
