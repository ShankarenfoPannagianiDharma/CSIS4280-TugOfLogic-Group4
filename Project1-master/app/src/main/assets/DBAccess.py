from flask import Flask, render_template, request, redirect, url_for, jsonify
import json
import os

app = Flask(__name__)   #app is an instance of FLASK

@app.route('/SessionCreate',methods=['POST'])  # POST only
def addSessionJSON():
    # POST data: first_name & last_name
    data = request.get_json();     #data is from application/json form data
    if os.path.exists('./SessionLobbies.json'):
        # append to json file
        with open("SessionLobbies.json", "r+") as file:  
            jsonDictionary = json.load(file)
            jsonDictionary.append(data)
            file.seek(0)
            json.dump(jsonDictionary, file)
    else:   #Create file if not exists
        jsonList = []
        jsonList.append(data)
        with open("SessionLobbies.json","w+") as file:    
            json.dump(jsonList, file)
    
    return '<h1>Operation complete. Data {} added.</h1>'.format(data)

@app.route('/GetSessions')
def getSessionJSON():
    #open JSON file
    reader = open("SessionLobbies.json", "r")
    #return the String
    return reader.read()
    

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)   # host='0.0.0.0' means whatever your public ip assigned will be used