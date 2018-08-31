# DownloadManager

DownloadManager is a simple file download manager written in java. It downloads parts of a file in different threads and combines them later for efficient download.

DownloadManager fully supports pause/resume of downloads. Similarly, the downloads objects are serialized to disk to make them persistent.
The main GUI is made by the JavaFX framework in the Observer/Observable model for easy extension.


![DownloadManager](https://raw.githubusercontent.com/GnikDroy/DownloadManager/master/screenshots/screenshot.png)


# Building 

The project uses ANT build scripts (Netbeans) to build. If you have NETBEANS, simply import the project and run from there. If you donot, go [here](https://ant.apache.org/) and get yourself a copy of the ANT executable.
To build the project, go inside the project directory and simply run `ant`. ANT must be in your path. The built jar file will be under the dist folder.
Execute the jar file to run the program.

# Installation

__Linux/Windows/Mac__

There is a built .jar file inside the dist folder.
To run the program simply execute `java -jar DownloadManager.jar` in the terminal or command prompt. Java must be in your path for this to work.

**Note:** The file 'history.dat' should also be present in the jar folder. Simply copy the one from the project root to the jar folder. A EMPTY "history.dat" FILE WILL NOT WORK.
