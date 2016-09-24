var buf = new Buffer(32);

//fill
buf.fill();

//write
len = buf.write("A node.js Buffer Demo");

console.log("Write byte count:" + len);


//read
console.log(buf.toString('utf-8'));
console.log(buf.toString('ascii', 0, 8));

//toJson
console.log(buf.toJSON());

//concat
var buf1 = new Buffer("A ha");

var buf2 = Buffer.concat([buf, buf1]);

console.log(buf2.toString());

//compare
console.log(buf.compare(buf1));

//copy
var buf3 = new Buffer(32);
buf.copy(buf3);

console.log(buf3.toString());

//slice
var buf4 = buf3.slice(2, 9);

console.log(buf4.toString());

//length
console.log(buf4.length);


