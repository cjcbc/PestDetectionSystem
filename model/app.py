from flask import Flask,request,jsonify

app = Flask(__name__)

@app.route('/test', methods=['GET'])
def hello_world():
    return 'Hello, World!'

@app.route('/calculate',methods=['POST'])
def calculate():
    data = request.json
    a = data.get('a',0)
    b = data.get('b',0)
    operation = data.get('operation','add')

    if operation == 'add':
        result = a+b
    elif operation == 'subtract' or operation == 'sub':
        result = a-b
    elif operation == 'multiply' or operation == 'mul':
        result = a*b
    elif operation == 'divide' or operation == 'div':
        result = a/b if b != 0 else 'Error: Division by zero'
    else:
        result = 'Error: Invalid operation'

    return jsonify({
        "code" : 200,
        "message":"success",
        "result" : result,
        "operation": operation,
    })

app.run()