var http = require('http');
var hellohandle = require('./modules/hellohandle')

http.createServer( function (request, response) {
    response.writeHead(200, { 'Content-Type' : 'text/html; charset=utf-8'});
    if(request.url != "/favicon.ico") {

        hellohandle['hello'](response)
        hellohandle.hello(response)

        console.log("http")
        response.end('Hello World');
    }
}).listen(8888);

console.log('Server running at http://127.0.0.1:8888/');
