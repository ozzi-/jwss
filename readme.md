# JWSS
Java Web Service Skeleton is my basic skeleton I use for developing applications using RESTful APIs and a SPA frontend.
It relies on a reverse proxy for authentication.

---------------------
Example of the Backend Calls:
![Backend calls](https://i.imgur.com/sU2heMd.png)

---------------------
Example of the Frontend Calls:
![Frontend calls](https://i.imgur.com/U5xi59j.png)

---------------------
Example of the Frontend Calls:
![Frontend](https://i.imgur.com/ZLo8wK9.png)


## Get building
1. Git clone
2. Eclipse -> Import -> Existing Maven Project
3. Edit pom.xml -> groupId & artifactId to match your project name
4. Edit src/helpers/Config.java -> appName to match your project name
5. Edit WebContent/WEB-INF/web.xml -> servlet-name to match your project name

## Configuring
JWSS look for its config on launch, such as DB credentials.

Windows --> %PROGRAMDATA%/{appName}/

Linux --> /opt/{appName}/

macOS --> /var/lib/{appName}/
  
There you will need to but the two files app.json & mail.json, you can find templates under res/example_*.json.

## Get running
Deployment is fairly easy, first though make sure to import the DB schema, you can find it under MySQL Workbench (https://dev.mysql.com/downloads/workbench/).
Now deploy the WAR file you built, logs are in your tomcat logs folder!

You can skip a reverse proxy by adding an entry to your browsers sessionStorage called "impers" and the user name as a value.
Don't forget to insert a user into the JWSS DB, matching your user name.
