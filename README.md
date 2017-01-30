![alt tag](https://github.com/CS279-2017/Automatic-Service-Dispatcher/blob/daily/sam_hurd/OilAndGas-PossibleLogov2.png)
# Geo - LOcated Workforce:
# Uber-style Labor Dispatch Using Remote Monitoring Data

The application processes sensor data to dispatch an appropriate user if something is wrong.

## Initial Setup

There are two main parts to this application - the Analysis Service and the Dispatcher Service.

### Dispatcher Setup

This part is written using [Django](https://www.djangoproject.com/), a python based web framework. This was written using python 2.7, so if your machine uses python 3, there is one line of code to change in the "delegate" method in `dispatcher/views.py`. Initial setup requires a few steps. Steps 3 and 4 may not be needed since `db.sqlite` is included. These can all be executed from the command line in the `djangodispatcher` folder:

1. `npm install && bower install` - installs dependencies
2. `"./node_modules/.bin/webpack" -d` - compiles the React JSX code based on configuration in "index.js" and "webpack.config.js"
3. `python manage.py makemigrations dispatcher` - stages database changes
4. `python manage.py migrate` - applies database changes
5. `python manage.py runserver` - starts the server

If you want to clear out all data from the database, run 
`python manage.py flush`.

Initializing data is done by starting the server and navigating to `/initialize`.

### Analysis Setup

Import the `Analysis` directory into IntelliJ. Navigate to this directory in the command line and execute two commands:

1. `gradle wrapper` - generates gradlew for the project
2. `./gradlew build && java -jar build/libs/gs-spring-boot-0.1.0.jar` - builds the project and runs the JAR file

## Running the Applications

Dispatcher: `python manage.py runserver`

Analysis: `./gradlew build && java -jar build/libs/gs-spring-boot-0.1.0.jar`

## Endpoints

The Dispatcher service runs on port 8000, and the Analysis service runs on port 8080

### Dispatcher

`/`: the home screen (differs based on user type)

`/api/currentuser`: returns information about the currently logged in user

`/api/activetasks`: returns the active tasks that are assigned to the logged in user

`/api/completedtasks`: returns the completed tasks that were assigned to the logged in user

`/api/finishtask`: POST request marks task sent in parameters as complete

`/api/delegate`: POST request with sensor data assigns user to task


`/api/allusers`: operator endpoint that returns user data

`/api/sensors`: operator endpoint that returns sensor (site) locations

`/api/totaldata`: operator endpoint that provides data about tasks occurring at each location


`/initialize`: creates sample set of data

### Analysis

`/{int}`: GET request to send sample data to Analysis Service
