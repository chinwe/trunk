#!/usr/bin/env python

from flask import Flask

app = Flask(__name__)

@app.route('/index/<user>', methods=['POST', 'GET', "PUT", 'DELETE'])
def index(user):
    return "hello %s" % user

if __name__ == "__main__":
    app.run(debug=True)
