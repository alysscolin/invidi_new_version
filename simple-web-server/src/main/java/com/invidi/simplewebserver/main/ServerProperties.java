package com.invidi.simplewebserver.main;


/*This class holds server's information
* port, root, thread number for example
* currently only root information reside
* here.
* */
public final class ServerProperties {
    private ServerProperties() {
    }
    /*not good design here. expose so many level directories. can be improved future
    * modify the project structure, move the resource to upper level*/
    public static String root = "my-web-project/src/main/resources/static";
    public static Integer port = 8080;
}
