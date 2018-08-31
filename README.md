# DownloadManager

This is a simple download manager written in java. It downloads parts of a file in different threads and combines them later for efficient download.

It also allows you to pause/resume downloads. Similarly, downloads are persistence. The downloads objects are serialized to disk.
The main GUI is made by the JavaFX framework in the Observer/Observable model for easy extension.

# Building 

The project uses ANT build scripts (Netbeans) to build. If you have NETBEANS simply import the project. If you donot, go [here](https://ant.apache.org/) and get yourself a copy of ANT.
To build the project go inside the project directory and simple run ant . The built jar file will be under the dist folder.
Execute the jar file to run the program.

# Installation

__ Linux/Windows/Mac __

There is a built .jar file inside the dist folder.
To run the program simply execute "java -jar DownloadManager.jar" in the terminal or command prompt. Java must be in your path for this to work.
Also note that the file history.bin should also be present in the folder. Simply copy the one from the project root to the jar file. DONOT CREATE A EMPTY "history.bin" FILE.
