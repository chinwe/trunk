#!/usr/bin/env python

from flask import Flask, render_template

app = Flask(__name__)

@app.route('/user/<name>')
def user(name):
	return render_template('tpl.html', name=name)


if __name__ == "__main__":
	app.run(debug=True)
