var events = require('events');

var eventEmitter = new events.EventEmitter();

eventEmitter.on('some_event', function() {
    console.log('some_event trigged.');
});

setTimeout(function() {
    eventEmitter.emit('some_event');
}, 1000);
