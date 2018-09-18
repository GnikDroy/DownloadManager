# DownloadManager

![codefactor](https://www.codefactor.io/repository/github/gnikdroy/downloadmanager/badge)  
[![Maintainability](https://api.codeclimate.com/v1/badges/98e2ca2a1eaedcd7934d/maintainability)](https://codeclimate.com/github/GnikDroy/DownloadManager/maintainability)

DownloadManager is a simple file download manager written in java. 
It downloads parts of a file in different threads and combines them later for efficient download.

DownloadManager fully supports pause/resume of downloads. Similarly, the downloads objects are serialized to disk to make them persistent.
The main GUI is made by the JavaFX framework in the Observer/Observable model for easy extension.


![DownloadManager](https://raw.githubusercontent.com/GnikDroy/DownloadManager/master/screenshots/screenshot.png)


# Building with gradle

If you do not have gradle go [here](https://gradle.org/install/) to get the latest version of gradle.

Clone the repository in your local system

`git clone https://github.com/GnikDroy/DownloadManager`

To build the project go to project root and run the following command.

`cd DownloadManager`

`gradle build`

A fat jar will be built in `builds/libs` folder.

Execute the jar file by the following command

`java -jar downloadmanager-0.1.0.jar`

# Run after building

__Linux/Windows/Mac__

A built .jar file will be inside the `build/libs` folder. 
To run the program simply execute `java -jar downloadmanager-0.1.0.jar` in the terminal or command prompt. 
Java must be installed and in your path for this to work.
