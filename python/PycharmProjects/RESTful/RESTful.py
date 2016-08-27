from flask import Flask, request

app = Flask(__name__)

users = {
    "python" : "123456"
}

@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route("/login", methods=["POST", "GET"])
def login():
    pass

if __name__ == '__main__':
    app.run()
