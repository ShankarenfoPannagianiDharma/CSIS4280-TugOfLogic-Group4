
# sudo pip3 install Flask-PyMongo
# sudo pip3 install pymongo[srv]


from flask import Flask, render_template,  request, escape
from flask_pymongo import PyMongo


app = Flask(__name__)
app.config["MONGO_URI"] = "mongodb+srv://admin:root@csis4280cluster.7xiy1.mongodb.net/sample_mflix?retryWrites=true&w=majority" # replace the URI with your own connection
mongo = PyMongo(app)


@app.route('/')
def init():                            # this is a comment. You can create your own function name
    return '<h1> {} </h1>'.format(__name__)




@app.route('/data')
def show_data():
    theMovie = mongo.db.movies.find({}).limit(1)
    return render_template("data.html",movieInfo=theMovie,collectionNames = mongo.db.collection_names())


@app.route('/moredata')                  # http://3.237.92.38:5000/moredata
def show_alldata():
    theMovie = list(mongo.db.movies.find({}).limit(20))
    return render_template("data2.html",movieCollection=theMovie,collectionNames = mongo.db.collection_names())


if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)
