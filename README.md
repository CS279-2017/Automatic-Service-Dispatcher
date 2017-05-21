![alt tag](https://github.com/CS279-2017/Automatic-Service-Dispatcher/blob/daily/sam_hurd/OilAndGas-PossibleLogov3.png)
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

Initializing data is done by starting the server and navigating to `/initialize`. This method creates a bunch of random tasks and delegates one to wh1 (password: engineering).

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

### Files

#### Android

1. CompletedTaskListFrag: grabs all previously completed tasks and stores them in a listview
2. FirebaseIDService: handles token refresh for push notifications
3. GlowAPI: handles which endpoints the retrofit can access
4. LocationService: updates user's location
5. LoginActivity: checks for exisiting session, then checks against server. If it does not exist then user can login
6. LoginResult: object representing the json returned by the login request
7. MapViewFragment: shows map view. If an existing task has been started, then the info is inflated
8. MyFirebaseMessagingService: handles what happens when push notification is received
9. NavDrawAct: handles the navigation drawer where you can select fragments to navigate to
10. NextTaskFrag: the screen containing details about the next task the user can accept
11. ProfileFragment: screen containing user info
12. ReviewActivity: not implemented, would be a rating screen for how workers are performing tasks
13. Skill: object representation of a user's skill
14. Task: object representation of task
15. TaskList: Lists of Task objects
16. UserInformation: object representing user details

#### Django

1. android_views.py: the endpoints used in the android application (have to disable csrf and do manual authentication)
2. rest_views.py: not used, but setup for the ContentProvider - comprehensive ReST service
3. website_views.py: website endpoints
4. models.py: the database layout and helper methods
5. urls.py: connecting methods from "views" directory to actual endpoints

#### React

1. actions.js, reducer.js, and stores.js: these follow the typical redux design pattern
2. worker pages: not recently updated, but they contain basic listviews and the ability to finish a task
3. ChartJSChart.js: a template for creating ChartJS charts
4. ControlCenter.js: inflates other pages. This is the main page
5. ControlCenterProvider.js: connects main page to store
6. ManualTaskModal.js: the modal that allows operator to create a task
7. Map.js: the base map with well locations and statuses
8. NavBar.js: the top navigation bar
9. PasscodeModal.js: the modal allowing operator to update the passcodes for each well's gate
10. SideBar2.js: the links the the side bar helping with navigation
11. SiteTable.js: the stats page containing the graphs
12. TaskTrend.js: the template for a single number panel
13. UserPanel.js: the template for a user profile panel

