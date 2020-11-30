from flask import Flask, render_template, request, redirect, url_for, jsonify
# from flask import Flask, render_template, redirect
from pymongo import MongoClient
import json
import os
from flask_pymongo import PyMongo
from bson.objectid import ObjectId

# config system
app = Flask(__name__)

# secret key is used to make the client-side sessions secure
app.config.update(dict(SECRET_KEY='yoursecretkey'))


client = MongoClient('mongodb+srv://myadmin:johnPassword@cluster0.0ot59.mongodb.net/TugOfLogicDB?retryWrites=true&w=majority')
db = client.TugOfLogicDB  # TugOfLogicDB is the name of the database

@app.route('/', methods = ['GET'])
def  getSessions():
     return 'Hello World'

# Debtors in game
@app.route('/getPlayers')
def show_data():
    theData = db.players.find({})
    return render_template("index.html",playerInfo=theData,collectionNames = db.collection_names())

# create and then add debtor to game
@app.route('/createPlayer', methods = ['POST'])
def add_player():
    player = db.players
    name = request.json['name']
    player_id = player.insert({'name': name})
    new_player = player.find_one({'_id': player_id })
    output = {'name' : new_player['name']}
    return jsonify({'result' : output})

# Create templte Statement to test the app
if db.statement.find().count() <= 0:
    print("statements Not found, creating....")
    db.statement.insert_one({'content': 'Schools provide a safe simulation of the real world', 'position': 'false', 'status': 0})
    db.statement.insert_one({'content': 'Schools put immense pressure on growing children.', 'position': 'true', 'status': 0})
    db.statement.insert_one({'content': 'Managing hundreds of children force generalizations on students.', 'position': 'true', 'status': 0})
    db.statement.insert_one({'content': 'Soft skills are learnt from a school environment', 'position': 'false', 'status': 0})
    db.statement.insert_one({'content': 'Schools are incentivised to only have good grades, regardless of actual status.', 'position': 'true', 'status': 0})

# read all statements cureent in play
def readStatements():
    statements = db.statement.find()

    if os.path.exists('./statementList.json'):
        with open('./statementList.json', 'r') as file:
            jsonStatments = json.load(file)
        with open('./statementList.json', 'w') as file:
            jsonStatments.append(statements)
            json.dump(jsonStatments, file)
    else:
        with open('./statementList.json', 'r') as file:
            jsonStatments = json.load(file)
        with open('./statementList.json', 'w') as file:
            jsonStatments.append(statements)
            json.dump(jsonStatments, file)
    return statements

# read all statements supporting a certain position
@app.route('/GetPositionStatements',methods=['POST'])
def getPositionStatement():

    jsonPosition = request.get_json
    position = jsonPosition
    statements = db.statement.find({'position': "true"})
    data = []
    for i in statements:
        # data.append(i)
        data.append({'content': i['content'],
                    'position': i['position'],
                    'status': i['status']})

    if os.path.exists('./statementPosList.json'):
        with open('./statementPosList.json', 'w') as testfile:
            json.dump(data, testfile)

    reader = open("statementPosList.json", "r")
    return reader.read()


# host creates a Session for a game group
@app.route('/SessionCreate',methods=['POST'])  # POST only
def addSessionJSON():
    # POST data: first_name & last_name
    data = request.get_json()     #data is from application/json form data

    if db.sessions.find().count() <= 0:
        print("No Sessions Not found, creating....")
        db.sessions.insert_one(data)
    else:
        db.sessions.insert_one(data)

    return '<h1>Operation complete. Data {} added.</h1>'.format(data)

# get all sessions
@app.route('/GetSessions')
def getSessionJSON():
    # Get sessions form DB
    sessions = db.sessions.find()
    sessionList = []
    for i in sessions:
        sessionList.append({'MainClaim': i['MainClaim'],
                            'Judge': i['Judge'] })
    #open / crateJSON file
    if os.path.exists('./SessionLobbies.json'):
        with open('./SessionLobbies.json', 'w+') as file:
            json.dump(sessionList, file)
    else:
        with open('./SessionLobbies.json', 'w+') as file:
            json.dump(sessionList, file)

    reader = open("SessionLobbies.json", "r")
    #return the String
    return reader.read()


# Get all statements in play
@app.route('/GetStatements', methods=['GET', 'POST'])
def main():

    # read all data
    statements = db.statement.find()
    docs = statements
    data = []
    for i in docs:
        # data.append(i)
        data.append({'content': i['content'],
                    'position': i['position'],
                    'status': i['status']})

    if os.path.exists('./statementList.json'):
        with open('./statementList.json', 'w') as testfile:
            json.dump(data, testfile)

    reader = open("statementList.json", "r")
    # return render_template('list.html',data=data)
    return reader.read()


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
