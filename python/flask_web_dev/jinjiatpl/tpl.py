#!/usr/bin/env python

from flask import Flask, render_template

app = Flask(__name__)

@app.route('/user/<name>')
def user(name):
	return render_template('tpl.html', name=name)

@app.route("/var")
def vartpl():
	mydict = dict()
	mydict['key'] = 'value'
	mydict['int'] = 1
	mydict['list'] = [1, 2, 3]
	mylist = [1, 2, 3, 4, 5]
	index = 4
	
	return render_template('var.html', 
	mydict=mydict,
	mylist=mylist,
	index=index
	)

@app.route("/ctl")
def ctl():
	comments = ["python is good", "yes"]
	return render_template('ctl.html', comments=comments) 

if __name__ == "__main__":
	app.run(debug=True)
