#!/usr/bin/env python

from flask import Flask
from flask import redirect

app = Flask(__name__)

@app.route('/')
def index():
	return '<h1>Hello World!</h1> <footer>power by flask</footer>'

@app.route('/user/<name>')
def user(name):
	return '<h1>Hello, %s!</h1>' % name

@app.route('/admin')
def admin():
	return redirect("/user/admin")

@app.route('/baidu')
def baidu():
	return redirect("https://www.baidu.com")

if __name__ == "__main__":
	app.run(debug=True)
