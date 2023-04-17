# Version Control System

[Original project on Hyperskill](https://hyperskill.org/projects/177?track=18)

The ability to roll back to the previous versions is crucial for software development. In this project, you will get acquainted with the idea of version control and write a simple version control system.  

The ```vcs``` folder is auto initialized when the programm is launched. This folder contains all information required for version control. Program stores commits in ```vcs/commits``` directory (the object database), and gives you back the unique key that now refers to that data object.  

The argument ```--help``` is used to display help information about programm. All vcs commands:  
* __config__ — get a username (a default one or a setted manually by a user).  
* __config {username}__ — set a username.  
* __add__ — get list of tracked files.  
* __add {filename}__ — start tracking specific filename.  
* __reset {filename}__ — undo "add" command for uncommitted changes for specific file.  
* __reset__ — undo "add" command for all uncommitted changes  
* __log__ — Show all the commits (commitID, author, comment) in reverse order.  
* __commit {comment}__ — save changes and get commitID.
* __checkout {commitID}__ — restore a file to specifit commitID version.  
* __delete__ — delete "vcs" directory  
